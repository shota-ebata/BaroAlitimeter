package com.ebata_shota.baroalitimeter.ui.parts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel

@Composable
fun TemperatureParts(
    uiState: MainViewModel.ContentUiState.TemperatureUiState,
    events: TemperaturePartsEvents,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is MainViewModel.ContentUiState.TemperatureUiState.ViewerMode -> {
            ClickableCard(
                text = stringResource(id = R.string.temperature, uiState.temperatureText),
                onClick = events::onClickTemperature,
                modifier = modifier
            )
        }

        is MainViewModel.ContentUiState.TemperatureUiState.EditMode -> {
            EditTextFieldRow(
                textFieldValue = uiState.temperatureTextFieldValue,
                onChangeTextFieldValue = events::onChangeTemperatureTextFieldValue,
                onClickDone = events::onClickDoneEditTemperature,
                onClickCancel = events::onClickCancelEditTemperature,
                modifier = modifier
            )
        }
    }
}

/**
 * Composable関数が深くなるごとに、だんだん細かいイベントをすべて引数に設定するのは手間になってくる。
 * それを解消するために、Eventをinterfaceにまとめてみた。
 */
interface TemperaturePartsEvents {
    fun onChangeTemperatureTextFieldValue(textFieldValue: TextFieldValue)
    fun onClickTemperature()
    fun onClickDoneEditTemperature()
    fun onClickCancelEditTemperature()
}
