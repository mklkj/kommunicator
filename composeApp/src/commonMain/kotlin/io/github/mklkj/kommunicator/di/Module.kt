package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
}

expect val platformModule: Module

@Composable
expect inline fun <reified T : BaseViewModel> injectViewModel(): T

fun initKoin() {
    startKoin {
        modules(commonModule, platformModule)
    }
}
