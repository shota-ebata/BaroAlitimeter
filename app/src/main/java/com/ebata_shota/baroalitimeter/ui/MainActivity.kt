package com.ebata_shota.baroalitimeter.ui


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ebata_shota.baroalitimeter.domain.extensions.collect
import com.ebata_shota.baroalitimeter.ui.content.MainContent
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXME: もう少しうまく隠せないか？
        val onBackPressedCallback = object : OnBackPressedCallback(enabled = false) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressedCallback()
            }
        }
        onBackPressedDispatcher.addCallback(owner = this, onBackPressedCallback)

        viewModel.uiState.collect(lifecycleScope) { uiState ->
            onBackPressedCallback.isEnabled =
                uiState is MainViewModel.UiState.EditAltitudeMode || uiState is MainViewModel.UiState.EditTemperatureMode
        }

        setContent {
            val uiState: MainViewModel.UiState by viewModel.uiState.collectAsStateWithLifecycle()
            BaroAlitimeterTheme {
                MainContent(
                    uiState = uiState,
                    onClickTemperature = viewModel::changeModeToEditTemperature,
                    onClickAltitude = viewModel::changeModeToEditAltitude,
                    onClickCancelTemperature = viewModel::cancelEditTemperature,
                    onClickCancelAltitude = viewModel::cancelEditAltitude,
                    setAltitude = viewModel::setAltitude, // TODO: setTemperatureと書くべきか、onDoneEditTemperatureと書くべきか？
                    setTemperature = viewModel::setTemperature, // TODO: setAltitudeと書くべきか、onDoneEditAltitudeと書くべきか？
                    undoAltitude = viewModel::undoAltitude,
                    undoTemperature = viewModel::undoTemperature,
                    onDismissedAltitudeSnackbar = viewModel::onDismissedAltitudeSnackbar,
                    onDismissedTemperatureSnackBar = viewModel::onDismissedTemperatureSnackBar,
                )
            }
        }
    }
}
