package com.ebata_shota.baroalitimeter.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.extensions.collect
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.screen.MainScreen
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT

        // FIXME: もう少しうまく隠せないか？
        val onBackPressedCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressedCallback()
            }
        }
        onBackPressedDispatcher.addCallback(owner = this, onBackPressedCallback)

        R.string.next
        R.string.next
        R.drawable.icon_palette_24

        R.drawable.ic_launcher_foreground

        viewModel.modeState.collect(lifecycleScope) { mode ->
            onBackPressedCallback.isEnabled =
                mode == MainViewModel.Mode.EditTemperature || mode == MainViewModel.Mode.EditAltitude
        }

        setContent {
            val themeMode: ThemeMode? by viewModel.themeState.collectAsStateWithLifecycle()
            themeMode?.let { // FIXME: nullableなのなーんかイケてない
                BaroAlitimeterTheme(
                    darkTheme = when (it) {
                        ThemeMode.LIGHT -> false
                        ThemeMode.DARK -> true
                        ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    }
                ) {
                    MainScreen(
                        selectedThemeMode = it
                    )
                }
            }
        }
    }
}
