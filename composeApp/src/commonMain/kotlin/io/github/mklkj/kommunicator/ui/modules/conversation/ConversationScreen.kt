package io.github.mklkj.kommunicator.ui.modules.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            LazyColumn(
                reverseLayout = true,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.details?.messages.orEmpty()) {
                    ChatMessage(it)
                }
            }
        }
    }

    @Composable
    private fun ChatMessage(message: Message, modifier: Modifier = Modifier) {
        val bubbleColor = when {
            message.isUserMessage -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
        }

        Box(
            contentAlignment = when {
                message.isUserMessage -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            },
            modifier = modifier
                .padding(
                    start = if (message.isUserMessage) 50.dp else 0.dp,
                    end = if (message.isUserMessage) 0.dp else 50.dp,
                )
                .fillMaxWidth()
        ) {
            Text(
                text = message.content,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp,
                            topEnd = if (message.isUserMessage) 2.dp else 20.dp,
                            topStart = if (message.isUserMessage) 20.dp else 2.dp
                        )
                    )
                    .background(bubbleColor)
                    .padding(16.dp)
            )
        }
    }
}
