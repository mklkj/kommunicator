package io.github.mklkj.kommunicator.ui.modules.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.SelectParticipantsWithLastReadMessage
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.ws.ConnectionStatus
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.utils.scaffoldPadding
import io.github.mklkj.kommunicator.ui.widgets.AppImage
import io.github.mklkj.kommunicator.ui.widgets.DotsTyping
import io.github.mklkj.kommunicator.utils.format
import kotlinx.uuid.UUID
import kotlinx.uuid.toUUID

class ConversationScreen(private val chatId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ConversationViewModel>()
        LaunchedEffect(viewModel) {
            viewModel.loadData(
                // workaround https://github.com/hfhbd/kotlinx-uuid/pull/282
                chatId = chatId.toUUID(),
            )
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

        LaunchedEffect(state.typingParticipants) {
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
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            var revealedMessageId by remember { mutableStateOf<UUID?>(null) }

            Column(
                modifier = Modifier
                    .scaffoldPadding(paddingValues)
                    .fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = state.connectionStatus != ConnectionStatus.Connected,
                ) {
                    Text(
                        text = state.connectionStatus.toString(),
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                            .background(
                                when (state.connectionStatus) {
                                    ConnectionStatus.NotConnected -> Color(0xFF37474F)
                                    ConnectionStatus.Connecting -> Color(0xFFFFAB00)
                                    ConnectionStatus.Connected -> Color(0xFF64DD17)
                                    is ConnectionStatus.Error -> Color(0xFFBF360C)
                                }
                            )
                            .padding(vertical = 5.dp)
                    )
                }
                LazyColumn(
                    state = chatListState,
                    reverseLayout = true,
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.typingParticipants.toList(), key = { it.id.toString() }) {
                        AvatarWithItem(it.avatarUrl, false) {
                            BubbleTyping()
                        }
                    }
                    items(state.messages, key = { it.id.toString() }) {
                        AvatarWithItem(it.avatarUrl, it.isUserMessage) {
                            BubbleMessageWithDate(
                                message = it,
                                isDetailsRevealed = revealedMessageId == it.id,
                                userReads = state.lastReadMessages.filter { lastRead ->
                                    lastRead.messageId == it.id
                                },
                                onClick = {
                                    revealedMessageId = when (it.id) {
                                        revealedMessageId -> null
                                        else -> it.id
                                    }
                                },
                            )
                        }
                    }
                }
                ChatInput(
                    isLoading = state.isLoading,
                    onTyping = viewModel::onTyping,
                    onSendClick = viewModel::sendMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                )
            }
        }
    }

    @Composable
    private fun AvatarWithItem(
        avatarUrl: String?,
        isUserMessage: Boolean,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            if (avatarUrl != null && !isUserMessage) {
                AppImage(
                    url = avatarUrl,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                )
            }
            content()
        }
    }

    @Composable
    private fun BubbleTyping(modifier: Modifier = Modifier) {
        val bubbleColor = MaterialTheme.colorScheme.surface
        val shape = RoundedCornerShape(
            bottomStart = 20.dp,
            bottomEnd = 20.dp,
            topEnd = 20.dp,
            topStart = 2.dp
        )
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = modifier
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.primaryContainer, shape)
                .clip(shape)
                .background(bubbleColor)
                .padding(16.dp)
        ) {
            DotsTyping(
                numberOfDots = 3,
                dotSize = 6.dp,
                spaceBetween = 6.dp,
                dotColor = MaterialTheme.colorScheme.onSurface,
                delayUnit = 500,
            )
        }
    }

    @Composable
    private fun BubbleMessageWithDate(
        message: LocalMessage,
        isDetailsRevealed: Boolean,
        userReads: List<SelectParticipantsWithLastReadMessage>,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            AnimatedVisibility(isDetailsRevealed) {
                Text(message.createdAt.format())
            }
            Column(Modifier.fillMaxWidth()) {
                BubbleMessage(
                    message = message,
                    onClick = onClick,
                )
                AnimatedVisibility(isDetailsRevealed && userReads.isNotEmpty()) {
                    Text(
                        text = "Read by: " + userReads
                            .joinToString { it.customName ?: it.firstname },
                        textAlign = if (message.isUserMessage) TextAlign.End else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .padding(bottom = 4.dp, end = 8.dp)
                        .fillMaxWidth()
                ) {
                    userReads.forEach {
                        AppImage(
                            url = it.avatarUrl,
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BubbleMessage(
        message: LocalMessage,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
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
            val shape = RoundedCornerShape(
                bottomStart = 20.dp,
                bottomEnd = 20.dp,
                topEnd = if (message.isUserMessage) 2.dp else 20.dp,
                topStart = if (message.isUserMessage) 20.dp else 2.dp
            )
            Text(
                text = message.content,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.secondaryContainer, shape)
                    .clip(shape)
                    .clickable { onClick() }
                    .background(bubbleColor)
                    .padding(16.dp)
            )
        }
    }

    @Composable
    private fun ChatInput(
        isLoading: Boolean,
        onTyping: (String) -> Unit,
        onSendClick: (MessageRequest) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var messageId by remember { mutableStateOf(UUID()) }
        var content by remember { mutableStateOf("") }

        TextField(
            value = content,
            onValueChange = {
                onTyping(it)
                content = it
            },
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
