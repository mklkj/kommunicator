package io.github.mklkj.kommunicator.ui.modules.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.ui.modules.registration.RegistrationScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.utils.scaffoldPadding
import io.github.mklkj.kommunicator.ui.widgets.TextInput

object LoginScreen : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val viewModel = getScreenModel<LoginViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sign in") },
                    navigationIcon = {
                        if (navigator.canPop) {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
                            }
                        }
                    },
                )
            }
        ) {
            LoginScreenContent(
                state = state,
                onLoginClick = viewModel::login,
                onOpenRegistration = {
                    navigator.push(RegistrationScreen)
                },
                modifier = Modifier.scaffoldPadding(it)
            )
        }
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    private fun LoginScreenContent(
        state: LoginState,
        onLoginClick: (username: String, password: String) -> Unit,
        onOpenRegistration: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val (first, second) = remember { FocusRequester.createRefs() }

        val username = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }

        Column(
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
        ) {
            TextInput(
                label = "Username",
                textState = username,
                focusRef = first,
                nextFocusRef = second,
            )
            TextInput(
                label = "Password",
                textState = password,
                focusRef = second,
                keyboardType = KeyboardType.Password,
            )
            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage,
                    color = Color.Red,
                )
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onOpenRegistration) {
                Text("Don't have an account? Register here")
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onLoginClick(username.value, password.value) },
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                }
                Text("Login", Modifier.fillMaxWidth())
            }
        }
    }
}
