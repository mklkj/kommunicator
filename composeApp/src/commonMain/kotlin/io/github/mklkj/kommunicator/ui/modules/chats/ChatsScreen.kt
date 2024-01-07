package io.github.mklkj.kommunicator.ui.modules.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.ui.modules.account.AccountScreen
import io.github.mklkj.kommunicator.ui.modules.conversation.ConversationScreen
import io.github.mklkj.kommunicator.ui.modules.welcome.WelcomeScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.widgets.AppImage
import io.github.mklkj.kommunicator.utils.timeAgoSince

object ChatsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ChatsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        when {
            !state.isLoggedIn -> navigator.replaceAll(WelcomeScreen)
            state.isLoading -> CircularProgressIndicator()
            !state.errorMessage.isNullOrBlank() -> Text(
                text = state.errorMessage.orEmpty(),
                color = Color.Red,
            )

            state.chats.isNotEmpty() -> ChatsScreenContent(
                viewModel = viewModel,
                onChatClick = { navigator.push(ConversationScreen(it.id)) },
                onAccountClick = { navigator.push(AccountScreen()) }
            )

            else -> Text("There is no chats")
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun ChatsScreenContent(
        viewModel: ChatsViewModel,
        onChatClick: (Chat) -> Unit,
        onAccountClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val uiState by viewModel.state.collectAsStateWithLifecycle()

        Column {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppImage(
                            url = uiState.userAvatarUrl.orEmpty(),
                            modifier = Modifier
                                .background(Color.DarkGray, CircleShape)
                                .clip(CircleShape)
                                .size(48.dp)
                                .clickable { onAccountClick() }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text("Czaty")
                    }
                },
                modifier = Modifier.height(80.dp)
            )
            LazyColumn(modifier.fillMaxSize()) {
                items(uiState.chats) { chat ->
                    ChatItem(
                        item = chat,
                        onClick = onChatClick,
                    )
                }
            }
        }
    }

    @Composable
    fun ChatItem(item: Chat, onClick: (Chat) -> Unit) {
        Row(
            Modifier
                .clickable(onClick = { onClick(item) })
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box {
                AppImage(
                    url = item.avatarUrl,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Box(
                    Modifier
                        .size(20.dp)
                        .background(if (item.isActive) Color.Green else Color.Gray, CircleShape)
                        .padding(5.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = item.name,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (item.isUnread) FontWeight.Bold else null,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Text(
                        text = item.lastMessageAuthor + ": " + item.lastMessage,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (item.isUnread) FontWeight.Bold else null,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(15.dp)
                    ) {
                        Box(
                            Modifier
                                .size(2.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                        )
                    }
                    Text(
                        text = timeAgoSince(item.lastMessageTimestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (item.isUnread) FontWeight.Bold else null,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
