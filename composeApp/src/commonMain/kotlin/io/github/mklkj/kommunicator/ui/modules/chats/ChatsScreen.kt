package io.github.mklkj.kommunicator.ui.modules.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.ui.modules.conversation.ConversationScreen
import io.github.mklkj.kommunicator.ui.modules.welcome.WelcomeScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.widgets.AppImage

object ChatsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<ChatsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxSize(),
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator()
                    !state.errorMessage.isNullOrBlank() -> Text(
                        text = state.errorMessage.orEmpty(),
                        color = Color.Red,
                    )

                    state.chats.isNotEmpty() -> ChatsScreenContent(
                        viewModel = viewModel,
                        onClick = { navigator.push(ConversationScreen(it.id)) },
                    )

                    else -> Text("There is no chats")
                }
            }
            Button(onClick = {
                viewModel.logout()
                navigator.replaceAll(WelcomeScreen)
            }) {
                Text("Logout")
            }
        }
    }

    @Composable
    private fun ChatsScreenContent(
        viewModel: ChatsViewModel,
        onClick: (Chat) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val uiState by viewModel.state.collectAsStateWithLifecycle()
        LazyColumn(modifier.fillMaxSize()) {
            items(uiState.chats) { chat ->
                ChatItem(
                    item = chat,
                    onClick = onClick,
                )
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
            AppImage(
                url = item.avatarUrl,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name)
                Text(item.lastMessageAuthor + ": " + item.lastMessage + " - " + item.lastMessageTimestamp.time)
            }
        }
    }
}
