package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ScreenState
import com.example.ui.AuthScreen
import com.example.ui.PendingApprovalScreen
import com.example.ui.RiderDashboardScreen
import com.example.ui.RiderViewModel
import com.example.ui.VerificationScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.FoodRiderTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

open class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", "Firebase init error: ${e.message}", e)
        }

        setContent {
            FoodRiderTheme {
                val viewModel: RiderViewModel = viewModel()
                val screenState by viewModel.screenState.collectAsState()

                // Request location permissions for live GPS tracking in Italy
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                    if (fineGranted || coarseGranted) {
                        Log.i("MainActivity", "Location permission granted. Starting live tracking.")
                        viewModel.locationStreamingManager.startLocationUpdates()
                    }
                }

                LaunchedEffect(Unit) {
                    val hasFine = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasFine) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        viewModel.locationStreamingManager.startLocationUpdates()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    when (screenState) {
                        ScreenState.AUTH -> AuthScreen(viewModel)
                        ScreenState.VERIFICATION -> VerificationScreen(viewModel)
                        ScreenState.PENDING_APPROVAL -> PendingApprovalScreen(viewModel)
                        ScreenState.DASHBOARD -> RiderDashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}
