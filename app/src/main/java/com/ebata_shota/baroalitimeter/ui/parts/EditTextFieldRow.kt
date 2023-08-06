package com.ebata_shota.baroalitimeter.ui.parts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTextFieldRow(
    text: String?,
    onClickDone: (String) -> Unit,
    onClickCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            modifier = modifier
                .padding(end = 8.dp)
                .height(48.dp),
            onClick = onClickCancel,
        ) {
            Text(text = "キャンセル")
        }
        val focusRequester = remember {
            FocusRequester()
        }
        var textFieldValue by remember {
            val altitudeEditTextValue = text.toString()
            mutableStateOf(
                TextFieldValue(
                    text = altitudeEditTextValue,
                    selection = TextRange(altitudeEditTextValue.length)
                )
            )
        }
        OutlinedTextField(
            modifier = modifier
                .weight(weight = 1.0f)
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
            modifier = modifier
                .padding(start = 8.dp)
                .height(48.dp),
            onClick = {
                onClickDone(textFieldValue.text)
            },
        ) {
            Text(text = "完了")
        }
    }
}

@Preview
@Composable
fun EditTextFieldPreview() {
    BaroAlitimeterTheme {
        EditTextFieldRow(
            text = "1000",
            onClickDone = {},
            onClickCancel = {}
        )
    }
}