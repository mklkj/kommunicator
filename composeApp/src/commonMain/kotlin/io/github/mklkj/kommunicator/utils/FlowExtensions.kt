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
        var lastvalue: T? = null
        collect { value ->
            val currentTime = Clock.System.now().toEpochMilliseconds()
            val isItTImeForNewEmission = currentTime - lastTime >= periodMillis
            if (isItTImeForNewEmission || lastvalue != value) {
                lastTime = currentTime
                emit(value)
            }
            lastvalue = value
        }
    }
}
