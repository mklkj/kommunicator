package io.github.mklkj.kommunicator.di

import androidx.compose.runtime.Composable
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsViewModel
import org.koin.compose.koinInject
import org.koin.dsl.module

actual val platformModule = module {
    factory { ChatsViewModel() }
}

@Composable
actual inline fun <reified T : BaseViewModel> injectViewModel(): T = koinInject() // todo!!!
