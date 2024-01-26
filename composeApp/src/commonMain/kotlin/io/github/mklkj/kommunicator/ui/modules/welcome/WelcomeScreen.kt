package io.github.mklkj.kommunicator.ui.modules.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.ui.modules.login.LoginScreen
import io.github.mklkj.kommunicator.ui.modules.registration.RegistrationScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

object WelcomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        WelcomeScreenContent(
            onLogin = { navigator.push(LoginScreen()) },
            onRegister = { navigator.push(RegistrationScreen()) },
        )
    }

    @Composable
    @OptIn(ExperimentalResourceApi::class)
    private fun WelcomeScreenContent(
        onLogin: () -> Unit,
        onRegister: () -> Unit,
    ) {
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource("compose-multiplatform.xml"),
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )

                Button(onClick = onLogin) {
                    Text("Sign in", Modifier.fillMaxWidth())
                }
                Button(onClick = onRegister) {
                    Text("Register", Modifier.fillMaxWidth())
                }
            }
        }
    }
}
