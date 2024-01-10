package io.github.mklkj.kommunicator.ui.modules.contacts.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import io.github.mklkj.kommunicator.ui.utils.LocalNavigatorParent
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle

class ContactAddScreen : Screen {

    @Composable
    @ExperimentalMaterial3Api
    override fun Content() {
        val navigator = LocalNavigatorParent
        val viewModel = getScreenModel<ContactAddViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        if (state.isSuccess) {
            navigator.pop()
        } else Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add a contact") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            }
        ) { paddingValues ->
            Column(Modifier.padding(paddingValues).padding(16.dp)) {
                var username by remember { mutableStateOf("") }
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!state.errorMessage.isNullOrBlank()) {
                    Text(text = state.errorMessage.orEmpty(), color = Color.Red)
                }
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            viewModel.addContact(username)
                        },
                        enabled = !state.isLoading,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text("Add")
                    }
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
