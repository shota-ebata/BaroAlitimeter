package com.ebata_shota.baroalitimeter.domain.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.launch

fun <T> Flow<T>.collect(
    lifecycleScope: CoroutineScope,
    collector: FlowCollector<T>,
) {
    lifecycleScope.launch {
        this@collect.collect(collector)
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T1, T2, T3, T4, T5, T6, R> combineTransform6(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend FlowCollector<R>.(T1, T2, T3, T4, T5, T6) -> Unit,
): Flow<R> = combineTransform(
    flows = arrayOf(flow, flow2, flow3, flow4, flow5, flow6),
    transform = { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6
        )
    }
)
