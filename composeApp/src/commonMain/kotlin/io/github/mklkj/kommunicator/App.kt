package io.github.mklkj.kommunicator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsScreen
import io.github.mklkj.kommunicator.ui.modules.welcome.WelcomeScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            val viewModel = koinInject<AppViewModel>()
            val isLoggedIn by viewModel.state.collectAsStateWithLifecycle()

            val startScreen = when (isLoggedIn) {
                true -> ChatsScreen
                false -> WelcomeScreen
                null -> return@MaterialTheme Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }

            Navigator(startScreen) {
                CurrentScreen()
            }
        }
    }
}
