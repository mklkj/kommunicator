package io.github.mklkj.kommunicator.ui.modules.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.ui.modules.welcome.WelcomeScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.widgets.AppImage

class AccountScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getScreenModel<AccountViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        when {
            state.isLoading -> Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.errorMessage.isNullOrBlank() -> Box(contentAlignment = Alignment.Center) {
                Text(state.errorMessage.orEmpty(), color = Color.Red)
            }

            else -> AccountContent(
                navigateUp = navigator::pop,
                user = state.user!!,
                onLogout = {
                    viewModel.logout()
                    navigator.replaceAll(WelcomeScreen)
                },
            )
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun AccountContent(
        user: LocalUser,
        navigateUp: () -> Unit,
        onLogout: () -> Unit,
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                AppImage(
                    url = user.avatarUrl,
                    modifier = Modifier
                        .background(Color.DarkGray, CircleShape)
                        .clip(CircleShape)
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@${user.username}",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Logout")
                }
            }
        }
    }
}
