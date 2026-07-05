package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pctracker.data.db.entity.ScrapeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrapeLogDao {
    @Insert
    suspend fun insert(log: ScrapeLogEntity)

    @Query("SELECT * FROM scrape_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<ScrapeLogEntity>>

    @Query("DELETE FROM scrape_logs WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
