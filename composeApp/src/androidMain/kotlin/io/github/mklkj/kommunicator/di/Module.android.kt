package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import org.koin.ksp.generated.defaultModule

actual val platformModule = defaultModule

@Composable
actual inline fun <reified T : BaseViewModel> injectViewModel(): T = org.koin.androidx.compose.koinViewModel()
