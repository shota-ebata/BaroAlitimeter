package com.ebata_shota.baroalitimeter.ui.parts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.viewmodel.MainViewModel

@Composable
fun AltitudeParts(
    uiState: MainViewModel.MainUiState.AltitudeUiState,
    events: AltitudePartsEvents,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is MainViewModel.MainUiState.AltitudeUiState.ViewerMode -> {
            Row(
                modifier = modifier.padding(8.dp)
            ) {
                ClickableCard(
                    text = stringResource(id = R.string.altitude, uiState.altitudeText),
                    onClick = events::onClickAltitude,
                )
            }
        }

        is MainViewModel.MainUiState.AltitudeUiState.EditMode -> {
            EditTextFieldRow(
                textFieldValue = uiState.altitudeTextFieldValue,
                onChangeTextFieldValue = events::onChangeAltitudeTextFieldValue,
                onClickDone = events::onClickDoneEditAltitude,
                onClickCancel = events::onClickCancelAltitude
            )
        }
    }
}

interface AltitudePartsEvents {
    fun onChangeAltitudeTextFieldValue(textFieldValue: TextFieldValue)
    fun onClickAltitude()
    fun onClickDoneEditAltitude()
    fun onClickCancelAltitude()
}
