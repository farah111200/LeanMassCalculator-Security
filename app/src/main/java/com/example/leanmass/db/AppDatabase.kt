package com.example.leanmass.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.leanmass.model.LbmRecord
import com.example.leanmass.model.User
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [LbmRecord::class, User::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lbmDao(): LbmDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // MASVS-STORAGE-1 : Chiffrement de la base de données avec SQLCipher
            val passphrase = SQLiteDatabase.getBytes("leanmass_secret_key".toCharArray())
            val factory = SupportFactory(passphrase)
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
  