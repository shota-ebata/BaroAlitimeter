package com.ebata_shota.baroalitimeter.ui.content

import android.hardware.SensorManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerModeContent(
    pressureText: String?,
    altitudeText: String?,
    temperatureText: String?,
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
                text = "$pressureText hPa",
            )
        }
        Row(
            modifier = modifier.padding(8.dp)
        ) {
            Card(
                modifier = modifier
                    .width(200.dp),
                onClick = onClickTemperature,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = modifier.padding(16.dp),
                        fontSize = 40.sp,
                        text = "$temperatureText ℃"
                    )
                }
            }
        }
        Row(
            modifier = modifier.padding(8.dp)
        ) {
            Card(
                modifier = modifier
                    .width(200.dp),
                onClick = onClickAltitude,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = modifier.padding(16.dp),
                        fontSize = 40.sp,
                        text = "$altitudeText m",
                    )
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