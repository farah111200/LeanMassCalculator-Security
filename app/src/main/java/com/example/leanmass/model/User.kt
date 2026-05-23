package com.example.leanmass.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,       // UID Firebase
    val email: String,
    val createdAt: String              // date de création du compte
)
