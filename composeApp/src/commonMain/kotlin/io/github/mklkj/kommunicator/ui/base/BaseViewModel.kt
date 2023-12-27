package io.github.mklkj.kommunicator.ui.base

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.crashkios.crashlytics.CrashlyticsKotlin
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseViewModel<S>(initialState: S) : StateScreenModel<S>(initialState) {

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        proceedError(throwable)
    }

    private val childrenJobs = mutableMapOf<String, Pair<Boolean, Job>>() // Pair(isObserver, job)
    private val _isAnyJobActive = MutableStateFlow(false)
    val isAnyJobActive = _isAnyJobActive.asStateFlow()

    protected fun proceedError(throwable: Throwable?) {
        if (!logError(throwable)) return
    }

    private fun logError(throwable: Throwable?): Boolean {
        Logger.e("Error occurred", throwable ?: return false)
        return throwable !is CancellationException
    }

    protected fun launch(
        tag: String,
        cancelExisting: Boolean = true,
        flowObserver: Boolean = false,
        block: suspend CoroutineScope.() -> Unit
    ) {
        if (!cancelExisting && isJobRunning(tag)) {
            return Logger.e("Job $tag already running")
        }

        Logger.d("Job $tag initialized")
        childrenJobs[tag]?.second?.cancel()
        if (!flowObserver) {
            _isAnyJobActive.value = true
        }
        childrenJobs[tag] = flowObserver to screenModelScope.launch(errorHandler) {
            block()
        }
        childrenJobs[tag]?.second?.invokeOnCompletion {
            if (!flowObserver) {
                _isAnyJobActive.value = isAnyJobPending()
            }
        }
    }

    private fun isAnyJobPending(): Boolean {
        return childrenJobs.any { (_, info) ->
            !info.first && info.second.isActive
        }
    }

    private fun isJobRunning(name: String): Boolean {
        val job = childrenJobs[name] ?: return false
        return job.second.isActive
    }

    protected fun cancelJobs(vararg names: String) {
        names.ifEmpty { childrenJobs.keys.toTypedArray() }.forEach {
            childrenJobs[it]?.second?.cancel()
        }
    }
}
