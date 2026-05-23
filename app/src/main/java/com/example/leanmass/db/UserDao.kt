package com.example.leanmass.db

import androidx.room.*
import com.example.leanmass.model.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getByUid(uid: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Delete
    suspend fun delete(user: User)
}
