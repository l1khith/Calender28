package com.l1khith.calender28.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SparkyDao {
    @Query("SELECT * FROM sparky WHERE id = 1 LIMIT 1")
    fun observeSparky(): Flow<SparkyEntity?>

    @Query("SELECT * FROM sparky WHERE id = 1 LIMIT 1")
    suspend fun getSparky(): SparkyEntity?

    @Query("SELECT * FROM sparky WHERE id = 1 LIMIT 1")
    fun getSparkySync(): SparkyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSparky(sparky: SparkyEntity): Long

    @Query("UPDATE sparky SET name = :newName WHERE id = 1")
    suspend fun renameSparky(newName: String): Int
}
