package io.github.mklkj.kommunicator.ui.modules.chats

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ChatsViewModel : BaseViewModel() {

    val chats = listOf(
        Chat(
            isUnread = false,
            lastMessage = "himenaeos",
            lastMessageAuthor = "ridens",
            name = "Edmond Hobbs",
            avatarUrl = "https://placehold.co/64x64/orange/white.jpg",
            lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
        ),
        Chat(
            isUnread = false,
            lastMessage = "aenean",
            lastMessageAuthor = "aptent",
            name = "Alexander Benton",
            avatarUrl = "https://placehold.co/64x64/green/black.png",
            lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
        ),
    )
}
