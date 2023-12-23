package io.github.mklkj.kommunicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsScreen
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            Navigator(ChatsScreen) {
                CurrentScreen()
            }
        }
    }
}
