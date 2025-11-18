package com.example.inv_5.utils

import android.content.Context
import android.os.Environment
import androidx.room.Room
import com.example.inv_5.data.database.AppDatabase
import com.example.inv_5.data.database.DatabaseProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object DatabaseBackupManager {
    
    private const val DB_NAME = "app_database"
    private const val BACKUP_EXTENSION = ".invdb"
    
    /**
     * Backup the database to Downloads folder
     * @return File object representing the backup file
     */
    fun backupDatabase(context: Context): File {
        // Close database before backup
        val db = DatabaseProvider.getInstance(context)
        db.close()
        
        // Get the database file
        val dbFile = context.getDatabasePath(DB_NAME)
        
        if (!dbFile.exists()) {
            throw IllegalStateException("Database file does not exist")
        }
        
        // Create backup filename with timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFileName = "INV5_Backup_${timestamp}${BACKUP_EXTENSION}"
        
        // Get Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val backupFile = File(downloadsDir, backupFileName)
        
        // Copy database file to backup location
        FileInputStream(dbFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Reopen database
        DatabaseProvider.getInstance(context)
        
        return backupFile
    }
    
    /**
     * Restore database from backup file
     * @param context Application context
     * @param backupFile The backup file to restore from
     */
    fun restoreDatabase(context: Context, backupFile: File) {
        if (!backupFile.exists()) {
            throw IllegalArgumentException("Backup file does not exist")
        }
        
        if (!backupFile.name.endsWith(BACKUP_EXTENSION)) {
            throw IllegalArgumentException("Invalid backup file format. File must have $BACKUP_EXTENSION extension")
        }
        
        // Close and delete existing database
        val db = DatabaseProvider.getInstance(context)
        db.close()
        
        // Get the database file path
        val dbFile = context.getDatabasePath(DB_NAME)
        
        // Delete existing database files (including -wal and -shm files)
        dbFile.delete()
        File(dbFile.parent, "$DB_NAME-wal").delete()
        File(dbFile.parent, "$DB_NAME-shm").delete()
        
        // Copy backup file to database location
        FileInputStream(backupFile).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Reinitialize database
        DatabaseProvider.getInstance(context)
    }
    
    /**
     * Get list of backup files from Downloads folder
     * @return List of backup files sorted by date (newest first)
     */
    fun getBackupFiles(): List<File> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            return emptyList()
        }
        
        return downloadsDir.listFiles { file ->
            file.name.endsWith(BACKUP_EXTENSION) && file.name.startsWith("INV5_Backup_")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Validate if a file is a valid database backup
     * @param file The file to validate
     * @return true if valid, false otherwise
     */
    fun isValidBackupFile(file: File): Boolean {
        return file.exists() && 
               file.name.endsWith(BACKUP_EXTENSION) && 
               file.length() > 0
    }
    
    /**
     * Format file size for display
     */
    fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.2f KB", kb)
            else -> "$sizeInBytes bytes"
        }
    }
    
    /**
     * Format backup file date for display
     */
    fun formatBackupDate(file: File): String {
        val date = Date(file.lastModified())
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return format.format(date)
    }
}
