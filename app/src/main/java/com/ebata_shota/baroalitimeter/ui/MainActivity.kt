package com.ebata_shota.baroalitimeter.ui


import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ebata_shota.baroalitimeter.ui.content.EditModeAltitudeContent
import com.ebata_shota.baroalitimeter.ui.content.EditModeTemperature
import com.ebata_shota.baroalitimeter.ui.content.ViewerModeContent
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState: MainViewModel.UiState by viewModel.uiState.collectAsStateWithLifecycle()
            BaroAlitimeterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val state = uiState) {
                        is MainViewModel.UiState.Loading -> Unit // FIXME: ローディング中表示があれば実装したい
                        is MainViewModel.UiState.ViewerMode -> {
                            ViewerModeContent(
                                pressureText = state.pressureText,
                                altitudeText = state.altitudeText,
                                temperatureText = state.temperatureText,
                                onClickTemperature = viewModel::changeToEditModeTemperature,
                                onClickAltitude = viewModel::changeToEditModeAltitude
                            )
                        }

                        is MainViewModel.UiState.EditAltitudeMode -> {
                            EditModeAltitudeContent(
                                pressureText = state.pressureText,
                                defaultAltitudeText = state.defaultAltitudeText,
                                temperatureText = state.temperatureText,
                                onClickDone = viewModel::setAltitude
                            )
                        }

                        is MainViewModel.UiState.EditTemperatureMode -> {
                            EditModeTemperature(
                                pressureText = state.pressureText,
                                altitudeText = state.altitudeText,
                                defaultTemperatureText = state.defaultTemperatureText,
                                onClickDone = viewModel::setTemperature
                            )
                        }
                    }

                }
            }
        }
    }
}


@Preview(
    showBackground = true,
    widthDp = 360
)
@Composable
fun ViewerModeContentPreview() {
    BaroAlitimeterTheme {
        ViewerModeContent(
            pressureText = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
            altitudeText = "1000",
            temperatureText = "15.0",
            onClickTemperature = {},
            onClickAltitude = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360
)
@Composable
fun EditModeAltitudeContentPreview() {
    BaroAlitimeterTheme {
        EditModeAltitudeContent(
            pressureText = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
            defaultAltitudeText = "1000",
            temperatureText = "15.0",
            onClickDone = {}
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360
)
@Composable
fun EditModeTemperaturePreview() {
    BaroAlitimeterTheme {
        EditModeTemperature(
            pressureText = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
            altitudeText = "1000",
            defaultTemperatureText = "15.0",
            onClickDone = {}
        )
    }
}