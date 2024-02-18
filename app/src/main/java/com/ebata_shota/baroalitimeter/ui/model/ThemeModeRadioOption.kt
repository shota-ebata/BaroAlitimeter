package com.ebata_shota.baroalitimeter.ui.model

import com.ebata_shota.baroalitimeter.R
import com.ebata_shota.baroalitimeter.domain.model.RadioOption
import com.ebata_shota.baroalitimeter.domain.model.content.ThemeMode

enum class ThemeModeRadioOption(
    val themeMode: ThemeMode,
    override val optionNameResId: Int,
) : RadioOption {
    LIGHT(ThemeMode.LIGHT, R.string.theme_light),
    DARK(ThemeMode.DARK, R.string.theme_dark),
    SYSTEM(ThemeMode.SYSTEM, R.string.theme_system);

    override val key: String = themeMode.valueName

    companion object {
        fun of(themeMode: ThemeMode): ThemeModeRadioOption {
            return values().find { it.themeMode == themeMode } ?: throw IllegalAccessException("ThemeMode is Illegal type")
        }
    }
}
