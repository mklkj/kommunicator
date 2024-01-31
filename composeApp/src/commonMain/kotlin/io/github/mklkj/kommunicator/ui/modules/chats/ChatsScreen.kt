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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.ui.modules.account.AccountScreen
import io.github.mklkj.kommunicator.ui.modules.conversation.ConversationScreen
import io.github.mklkj.kommunicator.ui.modules.welcome.WelcomeScreen
import io.github.mklkj.kommunicator.ui.utils.LocalNavigatorParent
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.widgets.AppImage
import io.github.mklkj.kommunicator.ui.widgets.PullRefreshIndicator
import io.github.mklkj.kommunicator.ui.widgets.pullRefresh
import io.github.mklkj.kommunicator.ui.widgets.rememberPullRefreshState
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import nl.jacobras.humanreadable.HumanReadable

internal object ChatsScreen : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Chats",
                    icon = icon,
                )
            }
        }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val navigator = LocalNavigatorParent
        val viewModel = navigator.getNavigatorScreenModel<ChatsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val pullRefreshState = rememberPullRefreshState(state.isLoading, viewModel::onRefresh)

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AppImage(
                                url = state.userAvatarUrl.orEmpty(),
                                modifier = Modifier
                                    .background(Color.DarkGray, CircleShape)
                                    .clip(CircleShape)
                                    .size(48.dp)
                                    .clickable { navigator.push(AccountScreen()) }
                            )
                            Spacer(Modifier.width(16.dp))
                            Text("Czaty")
                        }
                    },
                    modifier = Modifier.height(80.dp)
                )
            }
        ) { paddingValues ->
            LaunchedEffect(state.error) {
                if (state.error != null) {
                    scope.launch {
                        val action = snackbarHostState.showSnackbar(
                            message = state.error?.message.toString(),
                            actionLabel = "Retry",
                        )
                        if (action == SnackbarResult.ActionPerformed) {
                            viewModel.onRefresh()
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    !state.isLoggedIn -> navigator.replaceAll(WelcomeScreen)

                    state.isLoading && state.chats.isEmpty() -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        CircularProgressIndicator()
                    }

                    state.error != null && state.chats.isEmpty() -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = state.error?.message.toString(),
                            color = Color.Red,
                        )
                    }

                    state.chats.isEmpty() -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    ) {
                        Text("There is no chats")
                    }

                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.chats) { chat ->
                            ChatItem(
                                item = chat,
                                onClick = { navigator.push(ConversationScreen(it.id)) },
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    refreshing = state.isLoading,
                    state = pullRefreshState,
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
                    text = item.name ?: "Brak nazwy",
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (item.isUnread) FontWeight.Bold else null,
                )
                Spacer(Modifier.height(4.dp))

                if (item.lastMessage != null) Row(
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
                        text = HumanReadable.timeAgo(
                            item.lastMessageTimestamp ?: Clock.System.now()
                        ),
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
