package com.pctracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pctracker.data.db.entity.VoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoucherDao {
    @Query("SELECT * FROM voucher WHERE id = ${VoucherEntity.SINGLETON_ID}")
    fun observe(): Flow<VoucherEntity?>

    @Query("SELECT * FROM voucher WHERE id = ${VoucherEntity.SINGLETON_ID}")
    suspend fun get(): VoucherEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(voucher: VoucherEntity)

    @Update
    suspend fun update(voucher: VoucherEntity)
}
