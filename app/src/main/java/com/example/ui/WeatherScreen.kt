package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.WeatherCodeMapper
import com.example.data.model.WeatherConditionCategory
import com.example.ui.components.AlertBanner
import com.example.ui.components.AlertsSheet
import com.example.ui.components.CityManagerSheet
import com.example.ui.components.CitySearchSheet
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.HourlyForecastCard
import com.example.ui.components.WeatherAtmosphere
import com.example.ui.components.WeatherDetailsGrid
import com.example.ui.components.WeatherHero
import com.example.ui.components.glassCard
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSearchSheet by remember { mutableStateOf(false) }
    var showCityManagerSheet by remember { mutableStateOf(false) }
    var showAlertsSheet by remember { mutableStateOf(false) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Permission launcher for Notifications on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.testNotification(context)
            coroutineScope.launch { snackbarHostState.showSnackbar("Notification de test envoyée !") }
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar("Permission de notification refusée") }
        }
    }

    val requestNotificationAndTest: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.testNotification(context)
                coroutineScope.launch { snackbarHostState.showSnackbar("Notification de test envoyée !") }
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.testNotification(context)
            coroutineScope.launch { snackbarHostState.showSnackbar("Notification de test envoyée !") }
        }
    }

    // Permission launcher for Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    coroutineScope.launch {
                        var cityName = "Ma position"
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            }
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "Ma position"
                            }
                        } catch (e: Exception) {
                            // Geocoder fallback
                        }
                        viewModel.setLocation(location.latitude, location.longitude, cityName)
                        snackbarHostState.showSnackbar("Position détectée : $cityName")
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Position non disponible, activez le GPS")
                    }
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Permission de localisation refusée")
            }
        }
    }

    val onUseLocationRequested: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    coroutineScope.launch {
                        var cityName = "Ma position"
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = withContext(Dispatchers.IO) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            }
                            if (!addresses.isNullOrEmpty()) {
                                cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "Ma position"
                            }
                        } catch (e: Exception) {
                            // Fallback
                        }
                        viewModel.setLocation(location.latitude, location.longitude, cityName)
                        showSearchSheet = false
                        snackbarHostState.showSnackbar("Position détectée : $cityName")
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Position non disponible, vérifiez le GPS")
                    }
                }
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val currentConditionCategory = state.weather?.current?.let {
        WeatherCodeMapper.getWeatherInfo(it.weatherCode, it.isDay == 1).category
    } ?: WeatherConditionCategory.CLEAR
    val isDay = (state.weather?.current?.isDay ?: 1) == 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // iOS Glass Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassCard(
                            shape = RoundedCornerShape(30.dp),
                            baseAlpha = 0.22f,
                            borderAlpha = 0.45f,
                            elevation = 12.dp
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Saved Cities Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { showCityManagerSheet = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("open_city_manager_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatListBulleted,
                                contentDescription = "Mes lieux",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lieux (${state.savedCities.size})",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                        }

                        // Search Button
                        IconButton(
                            onClick = { showSearchSheet = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("open_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Rechercher une ville",
                                tint = Color.White
                            )
                        }

                        // Alerts Center Button with badge
                        IconButton(
                            onClick = { showAlertsSheet = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("bottom_alerts_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = if (state.alerts.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Centre d'alertes",
                                    tint = if (state.alerts.isNotEmpty()) Color(0xFFFDE047) else Color.White
                                )
                                if (state.alerts.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                    )
                                }
                            }
                        }

                        // GPS Quick Button
                        IconButton(
                            onClick = onUseLocationRequested,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .testTag("quick_gps_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Ma position",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        WeatherAtmosphere(
            category = currentConditionCategory,
            isDay = isDay,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top Prominent Glass Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassCard(
                                shape = RoundedCornerShape(22.dp),
                                baseAlpha = 0.18f,
                                borderAlpha = 0.4f
                            )
                            .clickable { showSearchSheet = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("top_search_bar"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Rechercher une ville ou un code postal...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onUseLocationRequested,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Position GPS",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Quick saved cities horizontal chip strip for instant switching
                if (state.savedCities.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.savedCities, key = { it.id }) { city ->
                            val isSelected = Math.abs(city.latitude - state.currentCity.latitude) < 0.05 &&
                                    Math.abs(city.longitude - state.currentCity.longitude) < 0.05

                            Box(
                                modifier = Modifier
                                    .glassCard(
                                        shape = RoundedCornerShape(18.dp),
                                        baseAlpha = if (isSelected) 0.35f else 0.12f,
                                        borderAlpha = if (isSelected) 0.65f else 0.20f
                                    )
                                    .clickable { viewModel.selectSavedCity(city) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("quick_city_chip_${city.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (city.isCurrentLocation) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = Color.White
                                        )
                                    )
                                    if (city.cachedTemp != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${city.cachedTemp.toInt()}°",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.9f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .glassCard(
                                        shape = RoundedCornerShape(18.dp),
                                        baseAlpha = 0.12f,
                                        borderAlpha = 0.25f
                                    )
                                    .clickable { showSearchSheet = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter un lieu",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Active Alert Banner if alerts are present
                AlertBanner(
                    alerts = state.alerts,
                    onClick = { showAlertsSheet = true },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                // Main Scrollable Weather Content
                if (state.isLoading && state.weather == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chargement de la météo pour ${state.currentCity.name}...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                        }
                    }
                } else if (state.errorMessage != null && state.weather == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Oups !",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.errorMessage ?: "Impossible de récupérer les données météo.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.refreshWeather() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Réessayer", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            WeatherHero(
                                city = state.currentCity,
                                weather = state.weather,
                                isFavorite = state.isFavorite,
                                isRefreshing = state.isRefreshing,
                                tempUnit = state.tempUnit,
                                lastUpdated = state.lastUpdated,
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                onRefresh = { viewModel.refreshWeather() },
                                onToggleTempUnit = { viewModel.toggleTempUnit() },
                                onOpenAlerts = { showAlertsSheet = true },
                                alertsCount = state.alerts.size
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HourlyForecastCard(
                                hourly = state.weather?.hourly,
                                tempUnit = state.tempUnit,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            DailyForecastCard(
                                daily = state.weather?.daily,
                                tempUnit = state.tempUnit,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            WeatherDetailsGrid(
                                current = state.weather?.current,
                                daily = state.weather?.daily,
                                tempUnit = state.tempUnit,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Search Sheet (Worldwide Cities and Postal Codes)
    if (showSearchSheet) {
        CitySearchSheet(
            query = state.searchQuery,
            results = state.searchResults,
            isSearching = state.isSearching,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onClearQuery = { viewModel.clearSearch() },
            onSelectResult = { result ->
                viewModel.selectSearchResult(result)
                showSearchSheet = false
            },
            onSelectPreset = { preset ->
                viewModel.selectCity(preset)
                showSearchSheet = false
            },
            onUseLocation = onUseLocationRequested,
            onDismiss = {
                showSearchSheet = false
                viewModel.clearSearch()
            }
        )
    }

    // Modal City Manager Sheet
    if (showCityManagerSheet) {
        CityManagerSheet(
            savedCities = state.savedCities,
            currentCity = state.currentCity,
            tempUnit = state.tempUnit,
            onSelectCity = { city ->
                viewModel.selectSavedCity(city)
                showCityManagerSheet = false
            },
            onDeleteCity = { city ->
                viewModel.deleteCity(city)
            },
            onOpenSearch = {
                showCityManagerSheet = false
                showSearchSheet = true
            },
            onDismiss = { showCityManagerSheet = false }
        )
    }

    // Modal Alerts Sheet
    if (showAlertsSheet) {
        AlertsSheet(
            activeAlerts = state.alerts,
            settings = state.alertSettings,
            cityName = state.currentCity.name,
            onUpdateSettings = { viewModel.updateAlertSettings(it) },
            onTestNotification = requestNotificationAndTest,
            onDismiss = { showAlertsSheet = false }
        )
    }
}
