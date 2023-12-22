package io.github.mklkj.kommunicator.ui.modules.chats

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.di.injectViewModel
import io.github.mklkj.kommunicator.ui.widgets.AppImage

@Composable
fun ChatsScreen(viewModel: ChatsViewModel = injectViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(Modifier.fillMaxSize()) {
        items(uiState.chats) { chat ->
            ChatItem(
                item = chat,
                onClick = { println(it.name) }
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
