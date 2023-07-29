package com.ebata_shota.baroalitimeter.di.module

sealed class Result<out V> {
    object Loading: Result<Nothing>()
    data class Success<V>(val value: V): Result<V>()
    data class Failed(val throwable: Throwable?): Result<Nothing>()
}