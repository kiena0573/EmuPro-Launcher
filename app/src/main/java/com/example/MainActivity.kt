package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.MainViewModel

import android.graphics.Color
import androidx.activity.SystemBarStyle

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        
        val db = AppDatabase.getDatabase(this)
        val repo = GameRepository(db.gameDao())
        
        setContent {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repo) as T
                }
            }
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            
            // Load settings on startup
            LaunchedEffect(Unit) {
                mainViewModel.loadSettings(this@MainActivity)
            }
            
            val appColor by mainViewModel.appColor.collectAsState()
            
            MyApplicationTheme(
                appColor = appColor
            ) {
                AppNavigation(mainViewModel = mainViewModel)
            }
        }
    }
}

