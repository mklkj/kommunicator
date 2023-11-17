package io.github.mklkj.kommunicator.ui.modules.chats

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.ui.theme.AppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Preview(showBackground = true)
@Composable
private fun ChatsItemPreview() {
    AppTheme {
        ChatItem(
            onClick = {},
            item = Chat(
                avatarUrl = "https://search.yahoo.com/search?p=cu",
                isUnread = false,
                lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                lastMessage = "ullamcorper",
                lastMessageAuthor = "facilis",
                name = "Loyd Gibson"
            ),
        )
    }
}
