package com.example.inv_5.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inv_5.bluetooth.BluetoothScannerManager
import com.example.inv_5.databinding.ActivityBluetoothScannerSettingsBinding

class BluetoothScannerSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothScannerSettingsBinding
    private lateinit var scannerManager: BluetoothScannerManager
    
    private lateinit var pairedDevicesAdapter: BluetoothDeviceAdapter
    private lateinit var availableDevicesAdapter: BluetoothDeviceAdapter
    
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    // Permission launchers
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            loadPairedDevices()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (bluetoothAdapter?.isEnabled == true) {
            checkPermissionsAndInitialize()
        } else {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothScannerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Initialize scanner manager
        scannerManager = BluetoothScannerManager.getInstance(this)

        // Setup RecyclerViews
        pairedDevicesAdapter = BluetoothDeviceAdapter { device -> connectToDevice(device) }
        availableDevicesAdapter = BluetoothDeviceAdapter { device -> connectToDevice(device) }
        
        binding.pairedDevicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BluetoothScannerSettingsActivity)
            adapter = pairedDevicesAdapter
        }
        
        binding.availableDevicesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BluetoothScannerSettingsActivity)
            adapter = availableDevicesAdapter
        }

        // Setup listeners
        binding.scanButton.setOnClickListener { startScanning() }
        binding.disconnectButton.setOnClickListener { disconnectScanner() }
        
        // Load settings
        loadSettings()
        
        // Setup scanner listeners
        setupScannerListeners()

        // Check Bluetooth and permissions
        if (!scannerManager.isBluetoothAvailable()) {
            showBluetoothDisabledDialog()
        } else {
            checkPermissionsAndInitialize()
        }
    }

    private fun showBluetoothDisabledDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth Disabled")
            .setMessage("Bluetooth is required for scanner connectivity. Would you like to enable it?")
            .setPositiveButton("Enable") { _, _ ->
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetoothLauncher.launch(enableBtIntent)
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkPermissionsAndInitialize() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            loadPairedDevices()
        } else {
            bluetoothPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevices() {
        val pairedDevices = scannerManager.getPairedDevices()
        
        if (pairedDevices.isEmpty()) {
            binding.noPairedDevicesText.visibility = View.VISIBLE
            binding.pairedDevicesRecyclerView.visibility = View.GONE
        } else {
            binding.noPairedDevicesText.visibility = View.GONE
            binding.pairedDevicesRecyclerView.visibility = View.VISIBLE
            pairedDevicesAdapter.setDevices(pairedDevices)
        }
    }

    private fun startScanning() {
        if (!scannerManager.hasBluetoothPermissions()) {
            checkPermissionsAndInitialize()
            return
        }

        binding.scanButton.isEnabled = false
        binding.scanProgressBar.visibility = View.VISIBLE
        binding.noAvailableDevicesText.visibility = View.GONE
        availableDevicesAdapter.clear()

        val started = scannerManager.startDiscovery()
        if (!started) {
            Toast.makeText(this, "Failed to start scanning", Toast.LENGTH_SHORT).show()
            binding.scanButton.isEnabled = true
            binding.scanProgressBar.visibility = View.GONE
            binding.noAvailableDevicesText.visibility = View.VISIBLE
        } else {
            // Auto-stop scanning after 12 seconds
            binding.root.postDelayed({
                stopScanning()
            }, 12000)
        }
    }

    private fun stopScanning() {
        scannerManager.stopDiscovery()
        binding.scanButton.isEnabled = true
        binding.scanProgressBar.visibility = View.GONE
        
        if (availableDevicesAdapter.itemCount == 0) {
            binding.noAvailableDevicesText.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        if (!scannerManager.hasBluetoothPermissions()) {
            Toast.makeText(this, "Missing Bluetooth permissions", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
        scannerManager.connectToDevice(device)
        
        // Save scanner preference
        saveDevicePreference(device)
    }

    private fun disconnectScanner() {
        scannerManager.disconnect()
        updateConnectionStatus(false, null)
        
        // Clear saved preference
        clearDevicePreference()
    }

    private fun setupScannerListeners() {
        // Connection listener
        scannerManager.addConnectionListener { connected, message ->
            runOnUiThread {
                updateConnectionStatus(connected, message)
                message?.let {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Discovery listener
        scannerManager.addDiscoveryListener { device ->
            runOnUiThread {
                availableDevicesAdapter.addDevice(device)
                binding.noAvailableDevicesText.visibility = View.GONE
            }
        }

        // Scan listener (for testing)
        scannerManager.addScanListener { barcode ->
            runOnUiThread {
                Toast.makeText(this, "Scanned: $barcode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateConnectionStatus(connected: Boolean, message: String?) {
        if (connected) {
            binding.connectionStatusText.text = "Connected"
            binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.connectedDeviceText.visibility = View.VISIBLE
            binding.connectedDeviceText.text = message ?: "Connected to scanner"
            binding.disconnectButton.visibility = View.VISIBLE
        } else {
            binding.connectionStatusText.text = "Not Connected"
            binding.connectionStatusText.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.connectedDeviceText.visibility = View.GONE
            binding.disconnectButton.visibility = View.GONE
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("scanner_settings", MODE_PRIVATE)
        binding.autoConnectSwitch.isChecked = prefs.getBoolean("auto_connect", false)
        binding.beepOnScanSwitch.isChecked = prefs.getBoolean("beep_on_scan", true)

        // Save settings on change
        binding.autoConnectSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_connect", isChecked).apply()
        }

        binding.beepOnScanSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("beep_on_scan", isChecked).apply()
        }

        // Auto-connect if enabled
        if (binding.autoConnectSwitch.isChecked) {
            val deviceAddress = prefs.getString("device_address", null)
            if (deviceAddress != null && scannerManager.hasBluetoothPermissions()) {
                val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
                device?.let { connectToDevice(it) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun saveDevicePreference(device: BluetoothDevice) {
        val prefs = getSharedPreferences("scanner_settings", MODE_PRIVATE)
        prefs.edit().apply {
            putString("device_address", device.address)
            putString("device_name", device.name ?: "Unknown")
            apply()
        }
    }

    private fun clearDevicePreference() {
        val prefs = getSharedPreferences("scanner_settings", MODE_PRIVATE)
        prefs.edit().remove("device_address").remove("device_name").apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerManager.stopDiscovery()
        // Don't disconnect here - keep connection alive for app use
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
