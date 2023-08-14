package com.ebata_shota.baroalitimeter.domain.model.content

import com.ebata_shota.baroalitimeter.domain.model.RadioOption

enum class ThemeMode(
    override val valueName: String,
) : RadioOption {
    LIGHT("LIGHT"),
    DARK("DARK"),
    SYSTEM("SYSTEM")
}