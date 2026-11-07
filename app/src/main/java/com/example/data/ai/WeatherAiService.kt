package com.example.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

class WeatherAiService {

    // Lazy initialization of the Gemini model as required by Firebase AI Logic
    private val model: GenerativeModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Generates a streaming intelligent briefing and activity advice for the current weather.
     */
    fun streamWeatherBriefing(
        cityName: String,
        currentTemp: Double,
        condition: String,
        maxTemp: Double,
        minTemp: Double,
        rainProb: Int,
        windSpeed: Double,
        uvIndex: Double
    ): Flow<String> {
        val prompt = """
            Tu es un météorologue expert et un assistant météo bienveillant et concis.
            Voici les conditions météorologiques actuelles pour $cityName :
            - Conditions : $condition
            - Température actuelle : ${currentTemp.toInt()}°C (Max : ${maxTemp.toInt()}°C, Min : ${minTemp.toInt()}°C)
            - Probabilité de pluie : $rainProb%
            - Vitesse du vent : ${windSpeed.toInt()} km/h
            - Indice UV : ${uvIndex.toInt()}

            Fournis un bulletin court (environ 3 à 4 phrases rythmées) en français incluant :
            1. Un résumé express du ressenti de la journée.
            2. Des recommandations concrètes sur la tenue vestimentaire adaptée (manteau, parapluie, lunettes...).
            3. Des conseils d'activités (sport extérieur, promenade, vigilance vent/pluie).
            Reste naturel, direct, engageant et élégant sans fioritures excessives.
        """.trimIndent()

        return model.generateContentStream(prompt)
            .mapNotNull { it.text }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Answers any user question about weather, outfit, or outdoor planning based on current context.
     */
    fun streamWeatherAnswer(
        userQuestion: String,
        weatherContext: String
    ): Flow<String> {
        val prompt = """
            Tu es l'assistant météo IA de l'application Météo.
            Voici le contexte météo actuel :
            $weatherContext

            L'utilisateur te pose la question suivante :
            "$userQuestion"

            Réponds de façon précise, bienveillante et concise (2 à 4 phrases) en français, en te basant sur les données météorologiques réelles fournies ci-dessus.
        """.trimIndent()

        return model.generateContentStream(prompt)
            .mapNotNull { it.text }
            .flowOn(Dispatchers.IO)
    }
}
