package dev.ghostbyte.nowandagain

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dev.ghostbyte.nowandagain.data.SettingsRepository
import dev.ghostbyte.nowandagain.data.ThemeMode
import dev.ghostbyte.nowandagain.notifications.Notifications
import dev.ghostbyte.nowandagain.ui.AppNavigation
import dev.ghostbyte.nowandagain.ui.theme.NowAndAgainTheme
import dev.ghostbyte.nowandagain.widget.NextTasksWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Channel name and description follow the app language
        Notifications.createChannel(this)

        // After a language change the activity is recreated with a saved state
        if (savedInstanceState != null) {
            lifecycleScope.launch { NextTasksWidget.refresh(applicationContext) }
        }

        val repo = SettingsRepository.get(applicationContext)

        setContent {
            val settings by repo.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT, Color.TRANSPARENT
                    ) { dark },
                )
                onDispose {}
            }

            NowAndAgainTheme(darkTheme = dark) { AppNavigation() }
        }
    }
}
