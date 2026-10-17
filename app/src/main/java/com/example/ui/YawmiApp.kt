package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.example.ui.components.YawmiBackground
import com.example.ui.navigation.Screen
import com.example.ui.navigation.YawmiBottomBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.FocusModeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ScheduleWeekScreen
import com.example.ui.screens.SessionEditScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.viewmodel.YawmiViewModel

enum class AppDestination {
    SPLASH,
    ONBOARDING,
    MAIN_TABS,
    SESSION_EDIT,
    ABOUT,
    FOCUS_MODE
}

@Composable
fun YawmiApp(
    viewModel: YawmiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOnboardingDone by viewModel.isOnboardingCompleted.collectAsState()

    var currentDestination by remember {
        mutableStateOf(if (isOnboardingDone) AppDestination.MAIN_TABS else AppDestination.SPLASH)
    }
    var currentTab by remember { mutableStateOf(Screen.TODAY.route) }

    // Session edit params
    var editingSessionId by remember { mutableStateOf<Long?>(null) }

    // Focus mode params
    var focusSessionId by remember { mutableStateOf<Long?>(null) }
    var focusTopicTitle by remember { mutableStateOf("مذاكرة: الكيمياء · الباب الثاني") }

    // Gentle notification permission prompt
    var showNotificationRationale by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                showNotificationRationale = true
            }
        }
    }

    // Force Arabic RTL for the entire app layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        YawmiBackground(modifier = modifier) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    AnimatedVisibility(
                        visible = currentDestination == AppDestination.MAIN_TABS,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        YawmiBottomBar(
                            currentRoute = currentTab,
                            onNavigate = { route -> currentTab = route }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentDestination) {
                        AppDestination.SPLASH -> {
                            SplashScreen(
                                onStartOnboarding = {
                                    currentDestination = AppDestination.ONBOARDING
                                }
                            )
                        }

                        AppDestination.ONBOARDING -> {
                            BackHandler {
                                if (!isOnboardingDone) {
                                    currentDestination = AppDestination.SPLASH
                                } else {
                                    currentDestination = AppDestination.MAIN_TABS
                                }
                            }
                            OnboardingScreen(
                                viewModel = viewModel,
                                onComplete = { currentDestination = AppDestination.MAIN_TABS }
                            )
                        }

                        AppDestination.MAIN_TABS -> {
                            when (currentTab) {
                                Screen.TODAY.route -> {
                                    TodayScreen(
                                        viewModel = viewModel,
                                        onNavigateToNewSession = { sId ->
                                            editingSessionId = sId
                                            currentDestination = AppDestination.SESSION_EDIT
                                        },
                                        onNavigateToAbout = {
                                            currentDestination = AppDestination.ABOUT
                                        },
                                        onNavigateToFocus = { sId, topic ->
                                            focusSessionId = sId
                                            focusTopicTitle = topic
                                            viewModel.startFocusSession(sId, topic, 25)
                                            currentDestination = AppDestination.FOCUS_MODE
                                        }
                                    )
                                }

                                Screen.SCHEDULE.route -> {
                                    ScheduleWeekScreen(
                                        viewModel = viewModel,
                                        onNavigateToEditSession = { sId ->
                                            editingSessionId = sId
                                            currentDestination = AppDestination.SESSION_EDIT
                                        }
                                    )
                                }

                                Screen.NOTES.route -> {
                                    NotesScreen(viewModel = viewModel)
                                }

                                Screen.STATS.route -> {
                                    StatsScreen(viewModel = viewModel)
                                }
                            }
                        }

                        AppDestination.SESSION_EDIT -> {
                            BackHandler {
                                currentDestination = AppDestination.MAIN_TABS
                            }
                            SessionEditScreen(
                                sessionId = editingSessionId,
                                viewModel = viewModel,
                                onDismiss = {
                                    currentDestination = AppDestination.MAIN_TABS
                                }
                            )
                        }

                        AppDestination.ABOUT -> {
                            BackHandler {
                                currentDestination = AppDestination.MAIN_TABS
                            }
                            AboutScreen(
                                onNavigateBack = {
                                    currentDestination = AppDestination.MAIN_TABS
                                }
                            )
                        }

                        AppDestination.FOCUS_MODE -> {
                            BackHandler {
                                currentDestination = AppDestination.MAIN_TABS
                            }
                            FocusModeScreen(
                                sessionId = focusSessionId,
                                topicTitle = focusTopicTitle,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    currentDestination = AppDestination.MAIN_TABS
                                }
                            )
                        }
                    }
                }
            }

            // Notification permission gentle dialog
            if (showNotificationRationale) {
                AlertDialog(
                    onDismissRequest = { showNotificationRationale = false },
                    title = {
                        Text(
                            text = "تنبيهات مواعيدك",
                            color = InkPrimary
                        )
                    },
                    text = {
                        Text(
                            text = "لتنبيهك قبل مواعيد دروسك والمدرسة والمذاكرة بـ ١٠ دقائق، يحتاج تطبيق يومي إلى إذن الإشعارات.",
                            color = InkSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showNotificationRationale = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        ) {
                            Text("السماح بالتنبيهات", color = InkPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNotificationRationale = false }) {
                            Text("لاحقاً", color = InkSecondary)
                        }
                    }
                )
            }
        }
    }
}
