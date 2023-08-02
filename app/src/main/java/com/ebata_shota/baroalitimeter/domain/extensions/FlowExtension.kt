package com.ebata_shota.baroalitimeter.domain.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> Flow<T>.collect(lifecycleScope: CoroutineScope, collector: FlowCollector<T>) {
    lifecycleScope.launch {
        this@collect.collect(collector)
    }
}