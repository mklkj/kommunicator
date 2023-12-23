package io.github.mklkj.kommunicator.ui.base

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ScreenModel {

    private val jobs = mutableMapOf<String, Job?>()

    protected fun launch(tag: String, block: suspend CoroutineScope.() -> Unit) {
        jobs[tag]?.cancel()
        jobs[tag] = screenModelScope.launch {
            block()
        }
    }
}
