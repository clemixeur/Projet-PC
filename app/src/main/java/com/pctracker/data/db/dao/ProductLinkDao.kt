package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pctracker.data.db.entity.ProductLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductLinkDao {
    @Query("SELECT * FROM product_links WHERE componentId = :componentId ORDER BY provider ASC")
    fun observeForComponent(componentId: Long): Flow<List<ProductLinkEntity>>

    @Query("SELECT * FROM product_links WHERE enabled = 1")
    suspend fun getAllEnabled(): List<ProductLinkEntity>

    @Query("SELECT * FROM product_links")
    fun observeAll(): Flow<List<ProductLinkEntity>>

    @Query("SELECT * FROM product_links")
    suspend fun getAllOnce(): List<ProductLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: ProductLinkEntity): Long

    @Update
    suspend fun update(link: ProductLinkEntity)

    @Delete
    suspend fun delete(link: ProductLinkEntity)

    @Query("DELETE FROM product_links")
    suspend fun deleteAll()
}
