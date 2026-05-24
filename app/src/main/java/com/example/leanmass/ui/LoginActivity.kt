package com.example.leanmass.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmass.MainActivity
import com.example.leanmass.databinding.ActivityLoginBinding
import com.example.leanmass.viewmodel.AuthState
import com.example.leanmass.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // MASVS-PLATFORM-3 : Empêcher les captures d'écran sur écran sensible
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si déjà connecté, aller directement à MainActivity
        if (authViewModel.isLoggedIn()) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.btnLogin.isEnabled = false
                is AuthState.Success -> goToMain()
                is AuthState.Error 