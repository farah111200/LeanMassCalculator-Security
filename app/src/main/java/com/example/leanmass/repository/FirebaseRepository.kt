package com.example.leanmass.repository

import com.example.leanmass.model.LbmRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("lbm_records")

    suspend fun save(record: LbmRecord) {
        val data = hashMapOf(
            "userId"  to record.userId,
            "gender"  to record.gender,
            "weight"  to record.weight,
            "height"  to record.height,
            "lbm"     to record.lbm,
            "date"    to record.date
        )
        collection.add(data).await()
    }

    suspend fun delete(record: LbmRecord) {
        val query = collection
            .whereEqualTo("userId", record.userId)
            .whereEqualTo("date", record.date)
            .get().await()
        for (doc in query.documents) {
            doc.reference.delete().await()
        }
    }

    fun getAll(userId: String, onResult: (List<LbmRecord>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    LbmRecord(
                        userId = doc.getString("userId") ?: "",
                        gender = doc.getString("gender") ?: "",
                        weight = doc.getDouble("weight") ?: 0.0,
                        height = doc.getDouble("height") ?: 0.0,
                        lbm    = doc.getDouble("lbm") ?: 0.0,
                        date   = doc.getString("date") ?: ""
                    )
                } ?: emptyList()
                onResult(list)
            }
    }
}
