package com.example.leanmass.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lbm_records")
data class LbmRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val gender: String,       // "M" ou "F"
    val weight: Double,
    val height: Double,
    val lbm: Double,
    val date: String          // format: "yyyy-MM-dd HH:mm"
)
