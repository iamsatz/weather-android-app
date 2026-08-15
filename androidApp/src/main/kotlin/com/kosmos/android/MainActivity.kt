package com.kosmos.android

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kosmos.android.i18n.LocalAppLocale
import com.kosmos.android.ui.navigation.KosmosNavHost
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme
import com.kosmos.shared.mode.UserMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        pendingViewModel?.setLocationPermission(granted)
        if (granted) {
            requestNotificationPermissionIfNeeded()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private var pendingViewModel: HomeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: HomeViewModel = viewModel()
            val appLocale by vm.appLocale.collectAsState()
            val userMode by vm.userMode.collectAsState()

            LaunchedEffect(vm) {
                pendingViewModel = vm
                vm.registerPermissionRequester { requestLocationPermission() }
                val hasLocation = hasLocationPermission()
                if (hasLocation) {
                    vm.bootstrap(true)
                    requestNotificationPermissionIfNeeded()
                } else {
                    requestLocationPermission()
                }
                vm.handleWidgetIntent(intent)
            }

            LaunchedEffect(vm) {
                while (true) {
                    delay(15 * 60 * 1000L)
                    vm.refreshOnForeground()
                }
            }

            LaunchedEffect(Unit) {
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightNavigationBars = true
            }

            CompositionLocalProvider(LocalAppLocale provides appLocale) {
                KosmosTheme(
                    darkTheme = false,
                    elderMode = userMode == UserMode.ELDER,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        KosmosNavHost(viewModel = vm)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pendingViewModel?.let {
            syncLocationPermission(it)
            it.refreshOnForeground()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingViewModel?.handleWidgetIntent(intent)
    }

    private fun syncLocationPermission(viewModel: HomeViewModel) {
        viewModel.recheckLocationPermission(hasLocationPermission())
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (notifications != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
