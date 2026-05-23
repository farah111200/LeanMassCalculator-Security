package com.example.leanmass.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.leanmass.db.AppDatabase
import com.example.leanmass.model.LbmRecord
import com.example.leanmass.repository.FirebaseRepository
import com.example.leanmass.repository.LocalRepository
import com.example.leanmass.utils.LbmCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LbmViewModel(application: Application) : AndroidViewModel(application) {

    private val localRepo: LocalRepository
    private val firebaseRepo = FirebaseRepository()

    init {
        val dao = AppDatabase.getDatabase(application).lbmDao()
        localRepo = LocalRepository(dao)
    }

    fun getRecords(userId: String): LiveData<List<LbmRecord>> =
        localRepo.getAllByUser(userId)

    fun calculate(userId: String, gender: String, weight: Double, height: Double): LbmRecord {
        val lbm = LbmCalculator.calculate(gender, weight, height)
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return LbmRecord(
            userId = userId,
            gender = gender,
            weight = weight,
            height = height,
            lbm = lbm,
            date = date
        )
    }

    fun saveRecord(record: LbmRecord) {
        viewModelScope.launch {
            // Sauvegarde SQLite locale
            localRepo.insert(record)
            // Sauvegarde Firebase cloud
            firebaseRepo.save(record)
        }
    }

    fun deleteRecord(record: LbmRecord) {
        viewModelScope.launch {
            localRepo.delete(record)
            firebaseRepo.delete(record)
        }
    }
}
