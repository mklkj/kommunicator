package io.github.mklkj.kommunicator.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * @see [https://proandroiddev.com/from-rxjava-to-kotlin-flow-throttling-ed1778847619]
 */
fun <T> Flow<T>.throttleFirst(periodMillis: Long): Flow<T> {
    return flow {
        var lastTime = 0L
        collect { value ->
            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (currentTime - lastTime >= periodMillis) {
                lastTime = currentTime
                emit(value)
            }
        }
    }
}
