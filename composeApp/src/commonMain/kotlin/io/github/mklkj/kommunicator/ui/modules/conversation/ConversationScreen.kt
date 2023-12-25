package io.github.mklkj.kommunicator.ui.modules.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import kotlinx.uuid.UUID

class ConversationScreen(private val chatId: UUID) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ConversationViewModel>()
        LaunchedEffect(viewModel) {
            viewModel.loadData(chatId)
        }
        ConversationScreenContent(
            viewModel = viewModel,
            navigateUp = navigator::pop,
        )
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ConversationScreenContent(
        viewModel: ConversationViewModel,
        navigateUp: () -> Unit,
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()

        Column {
            TopAppBar(
                title = { Text(state.details?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = { navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.details?.messages.orEmpty()) {
                    ChatMessage(it)
                }
            }
        }
    }

    @Composable
    private fun ChatMessage(message: Message) {
        Text(
            text = message.content,
            modifier = Modifier
                .padding(10.dp)
                .background(Color.Green)
                .padding(10.dp)
        )
    }
}
