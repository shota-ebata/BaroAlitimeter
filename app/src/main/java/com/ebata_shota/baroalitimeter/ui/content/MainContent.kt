package com.ebata_shota.baroalitimeter.ui.content

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.ui.parts.AltitudeParts
import com.ebata_shota.baroalitimeter.ui.parts.TemperatureParts
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel

@Composable
fun MainContent(
    uiState: MainViewModel.MainUiState.UiState,
    onChangeTemperatureTextFieldValue: (TextFieldValue) -> Unit,
    onClickTemperature: () -> Unit,
    onClickDoneEditTemperature: () -> Unit,
    onClickCancelEditTemperature: () -> Unit,
    onChangeAltitudeTextFieldValue: (TextFieldValue) -> Unit,
    onClickAltitude: () -> Unit,
    onClickDoneEditAltitude: () -> Unit,
    onClickCancelAltitude: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressureText = uiState.pressureText
    val seaLevelPressure = uiState.seaLevelPressureText
    val temperatureUiState = uiState.temperatureUiState
    val altitudeUiSate = uiState.altitudeUiState

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier.padding(8.dp)
        ) {
            Text(
                modifier = modifier,
                fontSize = 40.sp,
                text = stringResource(
                    id = R.string.pressure,
                    pressureText
                ),
            )
        }
        Row(
            modifier = modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        ) {
            Text(
                text = stringResource(
                    id = R.string.sea_level_pressure,
                    seaLevelPressure
                )
            )
        }

        TemperatureParts(
            uiState = temperatureUiState,
            onChangeTextFieldValue = onChangeTemperatureTextFieldValue,
            onClickTemperature = onClickTemperature,
            onClickDoneEditTemperature = onClickDoneEditTemperature,
            onClickCancelEditTemperature = onClickCancelEditTemperature
        )
        AltitudeParts(
            uiState = altitudeUiSate,
            onChangeTextFieldValue = onChangeAltitudeTextFieldValue,
            onClickAltitude = onClickAltitude,
            onClickDoneEditAltitude = onClickDoneEditAltitude,
            onClickCancelAltitude = onClickCancelAltitude
        )
    }
}


@Preview(
    showBackground = true,
    name = "Light Mode",
    widthDp = 360
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    widthDp = 360
)
@Composable
fun MainContentPreview() {
    BaroAlitimeterTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainContent(
                uiState = MainViewModel.MainUiState.UiState(
                    pressureText = "1024.1",
                    seaLevelPressureText = "1023.3",
                    temperatureUiState = MainViewModel.MainUiState.TemperatureUiState.ViewerMode("12"),
                    altitudeUiState = MainViewModel.MainUiState.AltitudeUiState.ViewerMode("24"),
                ),
                onChangeTemperatureTextFieldValue = {},
                onClickTemperature = {},
                onClickDoneEditTemperature = {},
                onClickCancelEditTemperature = {},
                onChangeAltitudeTextFieldValue = {},
                onClickAltitude = {},
                onClickDoneEditAltitude = {},
                onClickCancelAltitude = {}
            )
        }
    }
}