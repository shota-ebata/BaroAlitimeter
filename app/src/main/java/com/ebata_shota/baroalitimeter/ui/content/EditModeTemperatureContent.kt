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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.ui.parts.ClickableCard
import com.ebata_shota.baroalitimeter.ui.parts.EditTextFieldRow
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@Composable
fun EditModeTemperature(
    pressureText: String,
    seaLevelPressure: String,
    temperatureTextFieldValue: TextFieldValue,
    updateTemperatureTextFieldValue: (TextFieldValue) -> Unit,
    altitudeText: String,
    onClickDone: () -> Unit,
    onClickCancel: () -> Unit,
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
                text = "$pressureText hPa",
            )
        }

        Row(
            modifier = modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        ) {
            Text(text = "海面気圧 $seaLevelPressure hPa")
        }

        EditTextFieldRow(
            textFieldValue = temperatureTextFieldValue,
            updateTextFieldValue = updateTemperatureTextFieldValue,
            onClickDone = onClickDone,
            onClickCancel = onClickCancel
        )

        Row(
            modifier = modifier.padding(8.dp)
        ) {
            ClickableCard(
                text = "$altitudeText m",
                onClick = null,
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
fun EditModeTemperaturePreview() {
    BaroAlitimeterTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            EditModeTemperature(
                pressureText = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
                seaLevelPressure = SensorManager.PRESSURE_STANDARD_ATMOSPHERE.toString(),
                temperatureTextFieldValue = TextFieldValue("15"),
                updateTemperatureTextFieldValue = {},
                altitudeText = "1000",
                onClickDone = {},
                onClickCancel = {}
            )
        }
    }
}