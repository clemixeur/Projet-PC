package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pctracker.data.db.entity.TotalHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TotalHistoryDao {
    @Insert
    suspend fun insert(entry: TotalHistoryEntity)

    @Query("SELECT * FROM total_history ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<TotalHistoryEntity>>

    @Query("SELECT MIN(timestamp) FROM total_history")
    suspend fun getFirstTimestamp(): Long?

    @Query("SELECT * FROM total_history WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    suspend fun getBetween(start: Long, end: Long): List<TotalHistoryEntity>

    @Query("DELETE FROM total_history WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
