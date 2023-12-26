package io.github.mklkj.kommunicator.ui.modules.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = getScreenModel<LoginViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        if (state.isLoggedIn) {
            LocalNavigator.currentOrThrow.replaceAll(ChatsScreen)
        } else LoginScreenContent(
            state = state,
            onLoginClick = { username, password ->
                viewModel.login(username, password)
            },
        )
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    private fun LoginScreenContent(
        state: LoginState,
        onLoginClick: (username: String, password: String) -> Unit,
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val (first, second) = remember { FocusRequester.createRefs() }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                label = { Text("Username") },
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(first)
                    .focusProperties { next = second }
            )
            OutlinedTextField(
                label = { Text("Password") },
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(second)
            )
            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage,
                    color = Color.Red,
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = { onLoginClick(username, password) },
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
