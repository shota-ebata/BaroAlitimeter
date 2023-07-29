package com.ebata_shota.baroalitimeter.ui.content

import android.hardware.SensorManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.ui.parts.ClickableCard
import com.ebata_shota.baroalitimeter.ui.parts.EditTextFieldRow
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@Composable
fun EditModeTemperature(
    pressureText: String?,
    altitudeText: String?,
    defaultTemperatureText: String?,
    onClickDone: (String) -> Unit,
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

        EditTextFieldRow(
            text = defaultTemperatureText,
            onClickDone = onClickDone
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