package com.ebata_shota.baroalitimeter.ui.content

import android.content.res.Configuration
import android.hardware.SensorManager
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.ui.parts.ClickableCard
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@Composable
fun ViewerModeContent(
    pressureText: String,
    seaLevelPressure: String,
    altitudeText: String,
    temperatureText: String,
    onClickTemperature: () -> Unit,
    onClickAltitude: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = stringResource(id = R.string.pressure, pressureText),
            )
        }
        Row(
            modifier = modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        ) {
            Text(text = stringResource(id = R.string.sea_level_pressure, seaLevelPressure))
        }
        Row(
            modifier = modifier.padding(8.dp)
        ) {
            ClickableCard(
                text = stringResource(id = R.string.temperature, temperatureText),
                onClick = onClickTemperature,
            )
        }
        Row(
            modifier = modifier.padding(8.dp)
        ) {
            ClickableCard(
                text = stringResource(id = R.string.altitude, altitudeText),
                onClick = onClickAltitude,
            )
        }
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
fun ViewerModeContentPreview() {
    BaroAlitimeterTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ViewerModeContent(
                pressureText = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
                seaLevelPressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
                altitudeText = "1000",
                temperatureText = "15.0",
                onClickTemperature = {},
                onClickAltitude = {}
            )
        }
    }
}