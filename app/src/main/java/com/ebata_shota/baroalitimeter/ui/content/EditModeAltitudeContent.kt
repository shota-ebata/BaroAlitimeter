package com.ebata_shota.baroalitimeter.ui.content

import android.hardware.SensorManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeAltitudeContent(
    pressureText: String?,
    defaultAltitudeText: String?,
    temperatureText: String?,
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

        Row(
            modifier = modifier.padding(8.dp)
        ) {
            Card(
                modifier = modifier
                    .width(200.dp),
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
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val focusRequester = remember {
                FocusRequester()
            }
            var textFieldValue by remember {
                val altitudeEditTextValue = defaultAltitudeText.toString()
                mutableStateOf(
                    TextFieldValue(
                        text = altitudeEditTextValue,
                        selection = TextRange(altitudeEditTextValue.length)
                    )
                )
            }
            TextField(
                modifier = modifier
                    .width(150.dp)
                    .focusRequester(focusRequester),
                textStyle = TextStyle(fontSize = 40.sp),
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onClickDone(textFieldValue.text)
                    }
                )
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            Button(
                modifier = modifier.padding(start = 8.dp),
                onClick = {
                    onClickDone(textFieldValue.text)
                },
            ) {
                Text(text = "完了")
            }
        }
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