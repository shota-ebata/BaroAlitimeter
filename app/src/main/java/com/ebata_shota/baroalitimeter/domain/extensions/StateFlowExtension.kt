package com.ebata_shota.baroalitimeter.domain.extensions

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T> StateFlow<T>.collect(lifecycleScope: LifecycleCoroutineScope, collector: FlowCollector<T>) {
    lifecycleScope.launch {
        this@collect.collect(collector)
    }
}