package com.ebata_shota.baroalitimeter.ui.parts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel

@Composable
fun TemperatureParts(
    uiState: MainViewModel.MainUiState.TemperatureUiState,
    onChangeTextFieldValue: (TextFieldValue) -> Unit,
    onClickTemperature: () -> Unit,
    onClickDoneEditTemperature: () -> Unit,
    onClickCancelEditTemperature: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is MainViewModel.MainUiState.TemperatureUiState.ViewerMode -> {
            ClickableCard(
                text = stringResource(id = R.string.temperature, uiState.temperatureText),
                onClick = onClickTemperature,
                modifier = modifier
            )
        }

        is MainViewModel.MainUiState.TemperatureUiState.EditMode -> {
            EditTextFieldRow(
                textFieldValue = uiState.temperatureTextFieldValue,
                onChangeTextFieldValue = onChangeTextFieldValue,
                onClickDone = onClickDoneEditTemperature,
                onClickCancel = onClickCancelEditTemperature,
                modifier = modifier
            )
        }
    }
}