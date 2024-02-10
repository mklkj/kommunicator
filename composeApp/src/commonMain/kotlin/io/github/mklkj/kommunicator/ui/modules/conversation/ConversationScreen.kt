package io.github.mklkj.kommunicator.ui.modules.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.utils.scaffoldPadding
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
        val chatListState = rememberLazyListState()

        LaunchedEffect(state.messages) {
            chatListState.animateScrollToItem(0)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.chat?.customName.orEmpty()) },
                    navigationIcon = {
                        IconButton(onClick = { navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(Modifier.scaffoldPadding(paddingValues)) {
                LazyColumn(
                    state = chatListState,
                    reverseLayout = true,
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.messages, key = { it.id.toString() }) {
                        ChatMessage(it)
                    }
                }
                ChatInput(
                    isLoading = state.isLoading,
                    onSendClick = { viewModel.sendMessage(chatId, it) },
                    modifier = Modifier.fillMaxWidth().imePadding()
                )
            }
        }
    }

    @Composable
    private fun ChatMessage(message: LocalMessage, modifier: Modifier = Modifier) {
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

    @Composable
    private fun ChatInput(
        isLoading: Boolean,
        onSendClick: (MessageRequest) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var messageId by remember { mutableStateOf(UUID()) }
        var content by remember { mutableStateOf("") }

        TextField(
            value = content,
            onValueChange = { content = it },
            maxLines = 3,
            placeholder = { Text(text = "Type a message...") },
            trailingIcon = {
                Button(
                    onClick = {
                        onSendClick(MessageRequest(messageId, content))
                        content = ""
                        messageId = UUID()
                    },
                    enabled = content.isNotBlank() && !isLoading,
                    content = {
                        if (isLoading) CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        ) else Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.rotate(-90.0F).size(20.dp),
                        )
                    },
                    shape = RoundedCornerShape(30),
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            },
            modifier = modifier,
            shape = RoundedCornerShape(
                topEnd = 24.dp,
                topStart = 24.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp,
            ),
        )
    }
}
