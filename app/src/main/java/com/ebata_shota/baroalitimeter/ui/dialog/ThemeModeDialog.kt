package com.ebata_shota.baroalitimeter.ui.dialog

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode
import com.ebata_shota.baroalitimeter.ui.content.RadioListContent
import com.ebata_shota.baroalitimeter.ui.model.ThemeModeRadioOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeModeDialog(
    selected: ThemeMode,
    event: ThemeModeDialogEvent,
    modifier: Modifier = Modifier,
) {

    BasicAlertDialog(
        onDismissRequest = { event.onDismissThemeModeDialog() },
        modifier = modifier
    ) {
        Surface {
            RadioListContent(
                radioOptions = ThemeModeRadioOption.values(),
                selectedOption = ThemeModeRadioOption.of(selected),
                onOptionSelected = { themeModeRadioOption ->
                    event.onClickThemeMode(themeModeRadioOption.themeMode)
                }
            )
        }
    }
}

interface ThemeModeDialogEvent {
    fun onClickThemeMode(themeMode: ThemeMode)

    fun onDismissThemeModeDialog()
}