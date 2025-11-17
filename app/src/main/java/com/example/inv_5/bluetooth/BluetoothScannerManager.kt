package com.example.inv_5.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.util.UUID

/**
 * Singleton manager for Bluetooth barcode scanner connectivity.
 * Handles device discovery, pairing, connection, and barcode data reception.
 * Supports SPP (Serial Port Profile) Bluetooth scanners.
 */
@SuppressLint("MissingPermission")
class BluetoothScannerManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothScannerMgr"
        
        // SPP UUID for most Bluetooth barcode scanners
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        
        @Volatile
        private var instance: BluetoothScannerManager? = null

        fun getInstance(context: Context): BluetoothScannerManager {
            return instance ?: synchronized(this) {
                instance ?: BluetoothScannerManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var readThread: Thread? = null
    private var isConnected = false
    
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    // Callbacks
    private val scanListeners = mutableListOf<(String) -> Unit>()
    private val connectionListeners = mutableListOf<(Boolean, String?) -> Unit>()
    private val discoveryListeners = mutableListOf<(BluetoothDevice) -> Unit>()
    
    // Barcode buffer for parsing
    private val barcodeBuffer = StringBuilder()
    
    // BroadcastReceiver for Bluetooth device discovery
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let {
                        if (!discoveredDevices.contains(it)) {
                            discoveredDevices.add(it)
                            notifyDiscoveryListeners(it)
                            Log.d(TAG, "Found device: ${it.name} [${it.address}]")
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Discovery finished. Found ${discoveredDevices.size} devices")
                }
            }
        }
    }

    /**
     * Check if Bluetooth is supported and enabled on this device
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

    /**
     * Check if app has necessary Bluetooth permissions
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 11 and below
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Get list of already paired Bluetooth devices
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            return emptyList()
        }
        return bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * Start discovering Bluetooth devices
     */
    fun startDiscovery(): Boolean {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) {
            Log.w(TAG, "Cannot start discovery: missing permissions or Bluetooth unavailable")
            return false
        }

        discoveredDevices.clear()
        
        // Register discovery receiver
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)
        
        // Cancel any ongoing discovery and start fresh
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        
        val started = bluetoothAdapter.startDiscovery()
        Log.d(TAG, "Discovery started: $started")
        return started
    }

    /**
     * Stop discovering Bluetooth devices
     */
    fun stopDiscovery() {
        if (!hasBluetoothPermissions() || bluetoothAdapter == null) return
        
        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            context.unregisterReceiver(discoveryReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered, ignore
        }
        Log.d(TAG, "Discovery stopped")
    }

    /**
     * Connect to a Bluetooth scanner device
     */
    fun connectToDevice(device: BluetoothDevice) {
        if (!hasBluetoothPermissions()) {
            notifyConnectionListeners(false, "Missing Bluetooth permissions")
            return
        }

        // Disconnect if already connected
        disconnect()

        // Stop discovery before connecting (improves connection stability)
        stopDiscovery()

        Thread {
            try {
                Log.d(TAG, "Connecting to ${device.name} [${device.address}]")
                
                // Create RFCOMM socket using SPP UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                
                // Blocking call to connect
                bluetoothSocket?.connect()
                
                if (bluetoothSocket?.isConnected == true) {
                    inputStream = bluetoothSocket?.inputStream
                    isConnected = true
                    
                    // Start reading thread
                    startReadingData()
                    
                    mainHandler.post {
                        notifyConnectionListeners(true, "Connected to ${device.name}")
                    }
                    Log.i(TAG, "Successfully connected to ${device.name}")
                } else {
                    mainHandler.post {
                        notifyConnectionListeners(false, "Failed to connect")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Connection failed", e)
                mainHandler.post {
                    notifyConnectionListeners(false, "Connection error: ${e.message}")
                }
                disconnect()
            }
        }.start()
    }

    /**
     * Disconnect from the currently connected scanner
     */
    fun disconnect() {
        isConnected = false
        
        // Stop reading thread
        readThread?.interrupt()
        readThread = null
        
        // Close streams and socket
        try {
            inputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection", e)
        }
        
        inputStream = null
        bluetoothSocket = null
        barcodeBuffer.clear()
        
        Log.d(TAG, "Disconnected from scanner")
    }

    /**
     * Check if currently connected to a scanner
     */
    fun isConnected(): Boolean = isConnected && bluetoothSocket?.isConnected == true

    /**
     * Start reading data from the scanner
     */
    private fun startReadingData() {
        readThread = Thread {
            val buffer = ByteArray(1024)
            
            while (isConnected && !Thread.currentThread().isInterrupted) {
                try {
                    val bytesRead = inputStream?.read(buffer) ?: -1
                    if (bytesRead > 0) {
                        val data = String(buffer, 0, bytesRead)
                        processReceivedData(data)
                    }
                } catch (e: IOException) {
                    if (isConnected) {
                        Log.e(TAG, "Error reading data", e)
                        mainHandler.post {
                            notifyConnectionListeners(false, "Connection lost")
                            disconnect()
                        }
                    }
                    break
                }
            }
        }
        readThread?.start()
    }

    /**
     * Process received data and extract barcodes
     * Most scanners send: [Prefix] + Barcode + [Suffix]
     * Common patterns:
     * - Barcode + CR/LF (\r\n)
     * - Barcode + CR (\r)
     * - Barcode + LF (\n)
     * - Barcode + TAB (\t)
     */
    private fun processReceivedData(data: String) {
        barcodeBuffer.append(data)
        
        // Check for common terminators
        val terminators = listOf("\r\n", "\r", "\n", "\t")
        
        for (terminator in terminators) {
            val bufferString = barcodeBuffer.toString()
            if (bufferString.contains(terminator)) {
                // Extract barcode (everything before the terminator)
                val parts = bufferString.split(terminator, limit = 2)
                val barcode = parts[0].trim()
                
                if (barcode.isNotEmpty()) {
                    Log.d(TAG, "Barcode scanned: $barcode")
                    mainHandler.post {
                        notifyScanListeners(barcode)
                    }
                }
                
                // Keep any remaining data in buffer
                barcodeBuffer.clear()
                if (parts.size > 1) {
                    barcodeBuffer.append(parts[1])
                }
                break
            }
        }
        
        // Clear buffer if it gets too large (prevent memory issues)
        if (barcodeBuffer.length > 500) {
            Log.w(TAG, "Buffer overflow, clearing: ${barcodeBuffer.toString()}")
            barcodeBuffer.clear()
        }
    }

    /**
     * Register a listener for barcode scan events
     */
    fun addScanListener(listener: (String) -> Unit) {
        if (!scanListeners.contains(listener)) {
            scanListeners.add(listener)
        }
    }

    /**
     * Remove a scan listener
     */
    fun removeScanListener(listener: (String) -> Unit) {
        scanListeners.remove(listener)
    }

    /**
     * Register a listener for connection state changes
     */
    fun addConnectionListener(listener: (Boolean, String?) -> Unit) {
        if (!connectionListeners.contains(listener)) {
            connectionListeners.add(listener)
        }
    }

    /**
     * Remove a connection listener
     */
    fun removeConnectionListener(listener: (Boolean, String?) -> Unit) {
        connectionListeners.remove(listener)
    }

    /**
     * Register a listener for device discovery
     */
    fun addDiscoveryListener(listener: (BluetoothDevice) -> Unit) {
        if (!discoveryListeners.contains(listener)) {
            discoveryListeners.add(listener)
        }
    }

    /**
     * Remove a discovery listener
     */
    fun removeDiscoveryListener(listener: (BluetoothDevice) -> Unit) {
        discoveryListeners.remove(listener)
    }

    private fun notifyScanListeners(barcode: String) {
        scanListeners.forEach { it.invoke(barcode) }
    }

    private fun notifyConnectionListeners(connected: Boolean, message: String?) {
        connectionListeners.forEach { it.invoke(connected, message) }
    }

    private fun notifyDiscoveryListeners(device: BluetoothDevice) {
        discoveryListeners.forEach { it.invoke(device) }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopDiscovery()
        disconnect()
        scanListeners.clear()
        connectionListeners.clear()
        discoveryListeners.clear()
    }
}
