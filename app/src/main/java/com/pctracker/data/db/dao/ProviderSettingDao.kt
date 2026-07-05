package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pctracker.data.db.entity.ProviderSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderSettingDao {
    @Query("SELECT * FROM provider_settings")
    fun observeAll(): Flow<List<ProviderSettingEntity>>

    @Query("SELECT * FROM provider_settings")
    suspend fun getAll(): List<ProviderSettingEntity>

    @Query("SELECT COUNT(*) FROM provider_settings")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(settings: List<ProviderSettingEntity>)

    @Update
    suspend fun update(setting: ProviderSettingEntity)
}
