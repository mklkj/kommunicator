package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

actual val platformModule = module {
    viewModelOf(::ChatsViewModel)
}

@Composable
actual inline fun <reified T : BaseViewModel> injectViewModel(): T = org.koin.androidx.compose.koinViewModel()
