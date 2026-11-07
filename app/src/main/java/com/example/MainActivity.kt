package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.WeatherRepository
import com.example.ui.WeatherScreen
import com.example.ui.WeatherViewModel
import com.example.ui.WeatherViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure App Check with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)

        enableEdgeToEdge()

        // Create Android notification channel
        NotificationHelper.createNotificationChannel(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = WeatherRepository(database.cityDao())
        val factory = WeatherViewModelFactory(repository, applicationContext)

        setContent {
            MyApplicationTheme {
                val weatherViewModel: WeatherViewModel = viewModel(factory = factory)
                WeatherScreen(viewModel = weatherViewModel)
            }
        }
    }
}
