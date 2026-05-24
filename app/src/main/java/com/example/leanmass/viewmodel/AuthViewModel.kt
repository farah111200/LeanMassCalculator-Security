package com.example.leanmass.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.leanmass.db.AppDatabase
import com.example.leanmass.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val firestore = FirebaseFirestore.getInstance()
    private var failedAttempts = 0
    private var lockoutEndTime = 0L

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun register(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                viewModelScope.launch {
                    // ✅ Sauvegarder dans SQLite
                    userDao.insert(User(uid = uid, email = email, createdAt = date))

                    // ✅ Sauvegarder dans Firestore
                    firestore.collection("users").document(uid).set(
                        hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "createdAt" to date
                        )
                    )
                }
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Erreur inconnue")
            }
    }

    fun login(email: String, password: String) {
        if (System.currentTimeMillis() < lockoutEndTime) {
            val remaining = (lockoutEndTime - System.currentTimeMillis()) / 1000
            _authState.value = AuthState.Error("Trop de tentatives. Réessayez dans $remaining secondes.")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                failedAttempts = 0
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                viewModelScope.launch {
                    // ✅ Mettre à jour dans SQLite
                    userDao.insert(User(uid = uid, email = email, createdAt = date))

                    // ✅ Mettre à jour dans Firestore
                    firestore.collection("users").document(uid).set(
                        hashMapOf(
                            "uid" to uid,
                            "email" to email,
                            "createdAt" to date
                        )
                    )
                }
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
   