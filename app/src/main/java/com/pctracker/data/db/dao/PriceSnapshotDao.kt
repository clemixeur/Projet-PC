package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pctracker.data.db.entity.PriceSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceSnapshotDao {
    @Insert
    suspend fun insert(snapshot: PriceSnapshotEntity): Long

    @Insert
    suspend fun insertAll(snapshots: List<PriceSnapshotEntity>)

    /** Most recent snapshot per (component, provider), used to build the current comparison table. */
    @Query(
        """
        SELECT ps.* FROM price_snapshots ps
        INNER JOIN (
            SELECT componentId, provider, MAX(timestamp) AS maxTs
            FROM price_snapshots
            GROUP BY componentId, provider
        ) latest
        ON ps.componentId = latest.componentId
        AND ps.provider = latest.provider
        AND ps.timestamp = latest.maxTs
        """
    )
    fun observeLatestPerComponentAndProvider(): Flow<List<PriceSnapshotEntity>>

    @Query("SELECT * FROM price_snapshots WHERE componentId = :componentId ORDER BY timestamp ASC")
    fun observeHistoryForComponent(componentId: Long): Flow<List<PriceSnapshotEntity>>

    @Query("SELECT * FROM price_snapshots WHERE timestamp >= :sinceEpochMillis AND rawPrice IS NOT NULL")
    suspend fun getSuccessfulSince(sinceEpochMillis: Long): List<PriceSnapshotEntity>

    @Query("DELETE FROM price_snapshots WHERE timestamp < :beforeEpochMillis")
    suspend fun deleteOlderThan(beforeEpochMillis: Long)
}
