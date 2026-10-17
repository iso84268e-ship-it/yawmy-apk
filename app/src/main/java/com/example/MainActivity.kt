package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.YawmiApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.YawmiViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as YawmiApplication
        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return YawmiViewModel(
                        repository = app.repository,
                        preferencesRepository = app.preferencesRepository,
                        alarmScheduler = app.alarmScheduler
                    ) as T
                }
            }
        )[YawmiViewModel::class.java]

        setContent {
            MyApplicationTheme {
                YawmiApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
