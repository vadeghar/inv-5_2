package com.example.inv_5.ui.settings

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.inv_5.databinding.FragmentSettingsBinding
import com.example.inv_5.utils.DatabaseBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var restoreFilePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permission required for file operations", Toast.LENGTH_LONG).show()
            }
        }

        // File picker launcher for restore
        restoreFilePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleRestoreFileSelection(uri)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupClickListeners()

        return root
    }

    private fun setupClickListeners() {
        // Scanner Settings
        binding.btnBluetoothScanner.setOnClickListener {
            openBluetoothScannerSettings()
        }

        // Database Backup & Restore
        binding.btnBackupDatabase.setOnClickListener {
            backupDatabase()
        }

        binding.btnRestoreDatabase.setOnClickListener {
            openRestoreFilePicker()
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uses MANAGE_EXTERNAL_STORAGE
            if (Environment.isExternalStorageManager()) {
                true
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(intent)
                false
            }
        } else {
            // Android 10 and below
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                permissionLauncher.launch(permission)
                false
            }
        }
    }

    private fun openBluetoothScannerSettings() {
        val intent = android.content.Intent(requireContext(), com.example.inv_5.ui.scanner.BluetoothScannerSettingsActivity::class.java)
        startActivity(intent)
    }

    // Database Backup & Restore Functions
    private fun backupDatabase() {
        if (!checkAndRequestPermissions()) {
            Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnBackupDatabase.isEnabled = false
        binding.btnBackupDatabase.text = "Backing up..."

        lifecycleScope.launch {
            try {
                val backupFile = withContext(Dispatchers.IO) {
                    DatabaseBackupManager.backupDatabase(requireContext())
                }

                Toast.makeText(
                    requireContext(),
                    "Database backed up successfully!\nFile: ${backupFile.name}\nLocation: Downloads folder",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Backup failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                binding.btnBackupDatabase.isEnabled = true
                binding.btnBackupDatabase.text = "Backup Database"
            }
        }
    }

    private fun openRestoreFilePicker() {
        if (!checkAndRequestPermissions()) {
            Toast.makeText(requireContext(), "Please grant storage permission", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/octet-stream", "*/*"))
        }

        try {
            restoreFilePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening file picker: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleRestoreFileSelection(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Copy selected file to cache directory for validation
                val tempFile = withContext(Dispatchers.IO) {
                    val cacheDir = requireContext().cacheDir
                    val tempFile = File(cacheDir, "temp_restore.invdb")
                    
                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile
                }

                // Validate the file
                if (!DatabaseBackupManager.isValidBackupFile(tempFile)) {
                    tempFile.delete()
                    Toast.makeText(
                        requireContext(),
                        "Invalid backup file. Please select a .invdb file",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                // Show file details and confirmation dialog
                val fileSize = DatabaseBackupManager.formatFileSize(tempFile.length())
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Restore Database")
                    .setMessage(
                        "Are you sure you want to restore?\n\n" +
                        "File: ${tempFile.name}\n" +
                        "Size: $fileSize\n\n" +
                        "⚠️ WARNING: This will replace all current data!"
                    )
                    .setPositiveButton("OK") { _, _ ->
                        performRestore(tempFile)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        tempFile.delete()
                        dialog.dismiss()
                    }
                    .setOnDismissListener {
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                    }
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error reading file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun performRestore(backupFile: File) {
        binding.btnRestoreDatabase.isEnabled = false
        binding.btnRestoreDatabase.text = "Restoring..."

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    DatabaseBackupManager.restoreDatabase(requireContext(), backupFile)
                }

                Toast.makeText(
                    requireContext(),
                    "Database restored successfully!\nApp will restart...",
                    Toast.LENGTH_LONG
                ).show()

                // Restart the app to reload the restored database
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val intent = requireActivity().packageManager.getLaunchIntentForPackage(requireContext().packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        requireActivity().finish()
                        Runtime.getRuntime().exit(0)
                    }
                }, 2000)

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Restore failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                backupFile.delete()
                binding.btnRestoreDatabase.isEnabled = true
                binding.btnRestoreDatabase.text = "Restore Database"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
