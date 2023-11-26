package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

actual val platformModule: Module = module {
    includes(defaultModule)
}

@Composable
actual inline fun <reified T : BaseViewModel> injectViewModel(): T {
    return koinViewModel()
}
