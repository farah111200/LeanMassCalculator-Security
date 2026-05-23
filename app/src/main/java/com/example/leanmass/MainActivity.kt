package com.example.leanmass

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.leanmass.databinding.ActivityMainBinding
import com.example.leanmass.ui.HistoryActivity
import com.example.leanmass.ui.LoginActivity
import com.example.leanmass.utils.LbmCalculator
import com.example.leanmass.viewmodel.AuthViewModel
import com.example.leanmass.viewmodel.LbmViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val lbmViewModel: LbmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifier que l'utilisateur est connecté
        if (!authViewModel.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Spinner genre
        val genders = listOf("Homme", "Femme")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter

        // Bouton Calculer
        binding.btnCalculate.setOnClickListener {
            val weightStr = binding.etWeight.text.toString().trim()
            val heightStr = binding.etHeight.text.toString().trim()

            // Vérification champs vides
            if (weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir poids et taille", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightStr.toDoubleOrNull()
            val height = heightStr.toDoubleOrNull()

            // Validation des valeurs
            if (weight == null || weight <= 0) {
                Toast.makeText(this, "Poids invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (height == null || height <= 0) {
                Toast.makeText(this, "Taille invalide", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Déterminer le genre
            val gender = if (binding.spinnerGender.selectedItemPosition == 0) "M" else "F"

            // ✅ Calcul LBM selon la méthode de Boer
            // Homme : LBM = (0.407 × Poids) + (0.267 × Taille) − 19.2
            // Femme : LBM = (0.252 × Poids) + (0.473 × Taille) − 48.3
            val lbm = LbmCalculator.calculate(gender, weight, height)

            // ❌ Résultat physiquement impossible : LBM > poids total
            if (lbm >= weight) {
                binding.layoutResult.visibility = View.VISIBLE
                binding.ivResultIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                binding.tvLbmValue.text = "—"
                binding.tvResultLabel.text = "Valeurs incompatibles ❌"
                binding.tvResultLabel.setTextColor(getColor(android.R.color.holo_red_dark))
                binding.layoutAdvice.visibility = View.VISIBLE
                binding.tvAdvice.text = "La masse maigre calculée (${String.format("%.2f", lbm)} kg) dépasse votre poids total ($weight kg), ce qui est physiquement impossible.\n\n" +
                        "Veuillez vérifier vos valeurs :\n" +
                        "• Poids trop faible pour votre taille\n" +
                        "• IMC actuel : ${String.format("%.1f", weight / ((height/100) * (height/100)))} (norme : 18.5 – 25)\n\n" +
                        "La formule de Boer s'applique à des personnes avec un poids dans les normes."
                return@setOnClickListener
            }

            // Vérification de satisfaction selon les normes
            // Homme LBM ≥ 38 kg / Femme LBM ≥ 24 kg
            val isSatisfactory = LbmCalculator.isSatisfactory(gender, lbm)

            // Afficher le bloc résultat
            binding.layoutResult.visibility = View.VISIBLE
            binding.tvLbmValue.text = String.format("%.2f kg", lbm)

            if (isSatisfactory) {
                // ✅ Résultat dans les normes
                binding.tvResultLabel.text = "Résultat satisfaisant ✅"
                binding.tvResultLabel.setTextColor(getColor(android.R.color.holo_green_dark))
                binding.ivResultIcon.setImageResource(android.R.drawable.checkbox_on_background)
                binding.layoutAdvice.visibility = View.GONE

            } else {
                // ⚠️ Résultat hors normes + conseils
                binding.tvResultLabel.text = "Résultat à surveiller ⚠️"
                binding.tvResultLabel.setTextColor(getColor(android.R.color.holo_orange_dark))
                binding.ivResultIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                binding.layoutAdvice.visibility = View.VISIBLE
                binding.tvAdvice.text = getAdvice(gender, lbm)
            }

            // Bouton Enregistrer
            binding.btnSave.setOnClickListener {
                val userId = authViewModel.currentUserId() ?: run {
                    Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val record = lbmViewModel.calculate(userId, gender, weight, height)
                lbmViewModel.saveRecord(record)
                Toast.makeText(this, "Résultat enregistré !", Toast.LENGTH_SHORT).show()
            }
        }

        // Historique
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Déconnexion
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Retourne des conseils personnalisés selon le genre et le résultat LBM
     */
    private fun getAdvice(gender: String, lbm: Double): String {
        val threshold = if (gender == "M") LbmCalculator.THRESHOLD_MALE else LbmCalculator.THRESHOLD_FEMALE
        val deficit = threshold - lbm

        return buildString {
            appendLine("Votre masse maigre est en dessous de la norme de ${threshold} kg (déficit : ${String.format("%.1f", deficit)} kg).")
            appendLine()
            appendLine("• 🏋️ Exercice physique : pratiquez la musculation 2 à 3 fois par semaine pour développer la masse musculaire.")
            appendLine()
            appendLine("• 🥩 Alimentation : augmentez votre apport en protéines (viande, poisson, œufs, légumineuses) — visez 1.6 à 2g par kg de poids corporel.")
            appendLine()
            appendLine("• 😴 Sommeil : dormez 7 à 9 heures par nuit, la récupération musculaire se fait pendant le sommeil.")
            appendLine()
            if (gender == "M") {
                append("• 👨 Pour un homme, une LBM ≥ 38 kg est recommandée. Consultez un nutritionniste sportif pour un programme adapté.")
            } else {
                append("• 👩 Pour une femme, une LBM ≥ 24 kg est recommandée. Un suivi médical ou sportif peut vous aider à progresser.")
            }
        }
    }
}
