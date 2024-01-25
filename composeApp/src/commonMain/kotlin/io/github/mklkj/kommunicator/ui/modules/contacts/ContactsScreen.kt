package io.github.mklkj.kommunicator.ui.modules.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.getNavigatorScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.ui.modules.contacts.add.ContactAddScreen
import io.github.mklkj.kommunicator.ui.modules.conversation.ConversationScreen
import io.github.mklkj.kommunicator.ui.utils.LocalNavigatorParent
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.widgets.AppImage

internal object ContactsScreen : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Contacts",
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigatorParent
        val viewModel = navigator.getNavigatorScreenModel<ContactsViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        if (state.createdChat != null) {
            navigator.push(ConversationScreen(state.createdChat!!))
            viewModel.onConversationOpened()
        } else Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(ContactAddScreen()) },
                ) {
                    Icon(Icons.Filled.Add, "Add contact")
                }
            },
            floatingActionButtonPosition = FabPosition.End,
        ) {
            when {
                state.isLoading -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CircularProgressIndicator()
                }

                !state.errorMessage.isNullOrBlank() -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(text = state.errorMessage.orEmpty(), color = Color.Red)
                }

                state.contacts.isEmpty() -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = "You have no contacts!")
                }

                else -> ContactsContent(
                    contacts = state.contacts,
                    onContactClick = { viewModel.onCreateChat(it) },
                )
            }
        }
    }

    @Composable
    private fun ContactsContent(
        contacts: List<LocalContact>,
        onContactClick: (LocalContact) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts) {
                ContactItem(
                    item = it,
                    onClick = onContactClick,
                )
            }
        }
    }

    @Composable
    private fun ContactItem(item: LocalContact, onClick: (LocalContact) -> Unit) {
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
                    text = "${item.firstName} ${item.lastName}",
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@${item.username}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
