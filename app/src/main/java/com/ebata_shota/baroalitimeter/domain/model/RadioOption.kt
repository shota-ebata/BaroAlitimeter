package com.ebata_shota.baroalitimeter.domain.model

import androidx.annotation.StringRes

interface RadioOption {
    val key: String
    @get:StringRes
    val optionNameResId: Int
}
