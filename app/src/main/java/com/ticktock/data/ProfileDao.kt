package com.ticktock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProfileDao {
    @Query("SELECT * FROM time_alert_profiles ORDER BY id ASC")
    suspend fun getAll(): List<TimeAlertProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: TimeAlertProfile): Long

    @Delete
    suspend fun delete(profile: TimeAlertProfile)

    @Query("SELECT * FROM time_alert_profiles WHERE id = :id")
    suspend fun getById(id: Long): TimeAlertProfile?
}
