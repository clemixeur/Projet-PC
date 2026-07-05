package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pctracker.data.db.entity.ComponentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComponentDao {
    @Query("SELECT * FROM components ORDER BY position ASC")
    fun observeAll(): Flow<List<ComponentEntity>>

    @Query("SELECT * FROM components ORDER BY position ASC")
    suspend fun getAll(): List<ComponentEntity>

    @Query("SELECT * FROM components WHERE id = :id")
    suspend fun getById(id: Long): ComponentEntity?

    @Query("SELECT COUNT(*) FROM components")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(components: List<ComponentEntity>)
}
