package com.ebata_shota.baroalitimeter.ui.parts

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.ui.theme.BaroAlitimeterTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditTextFieldRow(
    textFieldValue: TextFieldValue,
    onChangeTextFieldValue: (TextFieldValue) -> Unit,
    onClickDone: () -> Unit,
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
            Text(text = stringResource(id = R.string.cancel))
        }
        val focusRequester = remember {
            FocusRequester()
        }
        OutlinedTextField(
            modifier = modifier
                .weight(weight = 1.0f)
                .focusRequester(focusRequester),
            textStyle = TextStyle(fontSize = 40.sp),
            value = textFieldValue,
            onValueChange = onChangeTextFieldValue,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onClickDone() }
            )
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        Button(
            modifier = modifier
                .padding(start = 8.dp)
                .height(48.dp),
            onClick = onClickDone,
        ) {
            Text(text = stringResource(id = R.string.done))
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
fun EditTextFieldPreview() {
    BaroAlitimeterTheme {
        Surface {
            EditTextFieldRow(
                textFieldValue = TextFieldValue("1000"),
                onChangeTextFieldValue = {},
                onClickDone = {},
                onClickCancel = {}
            )
        }
    }
}
