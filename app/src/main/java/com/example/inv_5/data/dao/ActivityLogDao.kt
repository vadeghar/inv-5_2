package com.example.inv_5.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.inv_5.data.entities.ActivityLog
import java.util.Date

@Dao
interface ActivityLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityLog: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activityLogs: List<ActivityLog>)

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC")
    fun getAllActivities(): LiveData<List<ActivityLog>>

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC")
    suspend fun getAllActivitiesList(): List<ActivityLog>

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentActivities(limit: Int): List<ActivityLog>

    @Query("SELECT * FROM activity_log WHERE entityType = :entityType ORDER BY timestamp DESC")
    suspend fun getActivitiesByEntityType(entityType: String): List<ActivityLog>

    @Query("SELECT * FROM activity_log WHERE activityType = :activityType ORDER BY timestamp DESC")
    suspend fun getActivitiesByActivityType(activityType: String): List<ActivityLog>

    @Query("SELECT * FROM activity_log WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getActivitiesByDateRange(startDate: Date, endDate: Date): List<ActivityLog>

    @Query("""
        SELECT * FROM activity_log 
        WHERE description LIKE '%' || :query || '%' 
        OR documentNumber LIKE '%' || :query || '%' 
        OR additionalInfo LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchActivities(query: String): LiveData<List<ActivityLog>>

    @Query("DELETE FROM activity_log WHERE timestamp < :cutoffDate")
    suspend fun deleteOldActivities(cutoffDate: Date): Int

    @Query("DELETE FROM activity_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activity_log")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM activity_log")
    suspend fun getActivityCount(): Int
}
