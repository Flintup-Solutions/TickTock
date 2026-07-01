package com.ticktock

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ticktock.permissions.PermissionHelper
import com.ticktock.ui.MainScreen
import com.ticktock.ui.MainViewModel
import com.ticktock.ui.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private var viewModel: MainViewModel? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel?.refreshPermissionState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TickTockApp

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFE94560),
                    background = Color(0xFF1A1A2E),
                    surface = Color(0xFF16213E),
                    onBackground = Color(0xFFF5F5F5),
                    onSurface = Color(0xFFF5F5F5),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: MainViewModel = viewModel(
                        factory = MainViewModelFactory(app.repository, applicationContext),
                    )
                    viewModel = vm
                    MainScreen(viewModel = vm)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestNotificationPermissionIfNeeded()
        viewModel?.onAppResumed()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
