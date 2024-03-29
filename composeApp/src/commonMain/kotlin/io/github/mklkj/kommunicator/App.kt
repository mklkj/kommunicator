package io.github.mklkj.kommunicator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.mklkj.kommunicator.ui.modules.homescreen.HomeScreen
import io.github.mklkj.kommunicator.ui.modules.login.LoginScreen
import io.github.mklkj.kommunicator.ui.theme.KommunicatorTheme
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import org.koin.compose.koinInject

@Composable
fun App() {
    LaunchedEffect(true) {
        Application.initialize()
    }

    KommunicatorTheme {
        val viewModel = koinInject<AppViewModel>()
        val isLoggedIn by viewModel.state.collectAsStateWithLifecycle()

        when (isLoggedIn) {
            true -> Navigator(HomeScreen) { CurrentScreen() }
            false -> Navigator(LoginScreen) { CurrentScreen() }
            null -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
