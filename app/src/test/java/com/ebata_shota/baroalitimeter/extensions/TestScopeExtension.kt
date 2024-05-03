package com.ebata_shota.baroalitimeter.extensions

import com.ebata_shota.baroalitimeter.domain.model.ContentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

fun TestScope.collectToList(flow: Flow<ContentParams>): MutableList<ContentParams> {
    val result = mutableListOf<ContentParams>()
    backgroundScope.launch(testDispatcher()) {
        flow.collect {
            result.add(it)
        }
    }
    return result
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.testDispatcher() = UnconfinedTestDispatcher(testScheduler)