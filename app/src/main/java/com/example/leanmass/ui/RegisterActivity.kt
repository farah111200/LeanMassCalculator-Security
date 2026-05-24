package com.example.leanmass.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmass.MainActivity
import com.example.leanmass.databinding.ActivityRegisterBinding
import com.example.leanmass.viewmodel.AuthState
import com.example.leanmass.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // MASVS-PLATFORM-3 : Empêcher les captures d'écran sur écran sensible
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() || confirm.isEmpty() ->
                    Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                password != confirm ->
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                !isPasswordStrong(password) ->
                    Toast.makeText(
                        this,
                        "8 caractères min, 1 majuscule, 1 chiffre, 1 caractère spécial",
                        Toast.LENGTH_LONG
                    ).show()
                else -> authViewModel.register(email, password)
            }
        }

        binding.tvLogin.setOnClickListener { finish() }

        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> binding.btnRegister.isEnabled = false
                is AuthState.Success -> {
                    Toast.mak