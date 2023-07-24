package com.ebata_shota.baroalitimeter.domain.repository

interface PrefRepository {
    // デフォルトは1013.25f
    var seaLevelPressure: Float
    var temperature: Float
}