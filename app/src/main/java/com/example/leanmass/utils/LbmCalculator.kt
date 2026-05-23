package com.example.leanmass.utils

object LbmCalculator {

    // Seuils configurables (méthode de Boer)
    const val THRESHOLD_MALE = 38.0
    const val THRESHOLD_FEMALE = 24.0

    /**
     * Calcule la masse maigre (LBM) selon la méthode de Boer
     * @param gender "M" pour homme, "F" pour femme
     * @param weight poids en kg
     * @param height taille en cm
     */
    fun calculate(gender: String, weight: Double, height: Double): Double {
        return if (gender == "M") {
            (0.407 * weight) + (0.267 * height) - 19.2
        } else {
            (0.252 * weight) + (0.473 * height) - 48.3
        }
    }

    /**
     * Vérifie si le résultat est dans les normes
     */
    fun isSatisfactory(gender: String, lbm: Double): Boolean {
        val threshold = if (gender == "M") THRESHOLD_MALE else THRESHOLD_FEMALE
        return lbm >= threshold
    }
}
