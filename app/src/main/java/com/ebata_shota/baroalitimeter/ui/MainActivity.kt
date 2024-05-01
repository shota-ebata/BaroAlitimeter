package com.ebata_shota.baroalitimeter.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.ebata_shota.baroalitimeter.domain.extensions.collect
import com.ebata_shota.baroalitimeter.ui.screen.MainScreen
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

        viewModel.modeState.collect(lifecycleScope) { mode ->
            onBackPressedCallback.isEnabled =
                mode == MainViewModel.Mode.EditTemperature || mode == MainViewModel.Mode.EditAltitude
        }

        setContent {
            MainScreen()
        }
    }
}
