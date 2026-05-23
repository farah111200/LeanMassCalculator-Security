package com.example.leanmass.repository

import androidx.lifecycle.LiveData
import com.example.leanmass.db.LbmDao
import com.example.leanmass.model.LbmRecord

class LocalRepository(private val dao: LbmDao) {

    fun getAllByUser(uid: String): LiveData<List<LbmRecord>> = dao.getAllByUser(uid)

    suspend fun insert(record: LbmRecord) = dao.insert(record)

    suspend fun delete(record: LbmRecord) = dao.delete(record)
}
