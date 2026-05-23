package com.example.leanmass.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.leanmass.model.LbmRecord

@Dao
interface LbmDao {

    @Query("SELECT * FROM lbm_records WHERE userId = :uid ORDER BY date DESC")
    fun getAllByUser(uid: String): LiveData<List<LbmRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LbmRecord)

    @Delete
    suspend fun delete(record: LbmRecord)

    @Query("DELETE FROM lbm_records WHERE userId = :uid")
    suspend fun deleteAllByUser(uid: String)
}
