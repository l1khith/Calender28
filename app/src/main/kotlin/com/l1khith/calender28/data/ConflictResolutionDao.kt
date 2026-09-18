package com.l1khith.calender28.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recorded conflict resolutions.
 */
@Dao
interface ConflictResolutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResolution(resolution: ConflictResolutionEntity)

    @Query("SELECT * FROM conflict_resolutions WHERE id = :id LIMIT 1")
    suspend fun getResolutionById(id: String): ConflictResolutionEntity?

    @Query("SELECT * FROM conflict_resolutions ORDER BY resolved_at_ms DESC")
    fun getAllResolutions(): Flow<List<ConflictResolutionEntity>>

    @Query("SELECT * FROM conflict_resolutions ORDER BY resolved_at_ms DESC LIMIT :limit")
    suspend fun getRecentResolutions(limit: Int = 20): List<ConflictResolutionEntity>

    @Query("DELETE FROM conflict_resolutions WHERE id = :id")
    suspend fun deleteResolutionById(id: String)
}
