package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import org.koin.compose.rememberKoinInject
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    includes(koinDefaultModule)
}

@Composable
actual inline fun <reified T : BaseViewModel> injectViewModel(): T {
    // todo: is this really the best option?
    return rememberKoinInject()
}

expect val koinDefaultModule: Module
