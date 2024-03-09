package io.github.mklkj.kommunicator.ui.modules.homescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import io.github.mklkj.kommunicator.ui.modules.chats.ChatsScreen
import io.github.mklkj.kommunicator.ui.modules.contacts.ContactsScreen
import io.github.mklkj.kommunicator.ui.utils.scaffoldPadding

object HomeScreen : Screen {

    private const val tabNavigatorId = "tab_navigator"

    @Composable
    override fun Content() {
        TabNavigator(ChatsScreen, key = tabNavigatorId) { tabNavigator ->
            Scaffold(
                content = {
                    Box(Modifier.scaffoldPadding(it)) {
                        CurrentTab()
                    }
                },
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(tabNavigator, ChatsScreen)
                        TabNavigationItem(tabNavigator, ContactsScreen)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tabNavigator: TabNavigator, tab: Tab) {
        NavigationBarItem(
            selected = tabNavigator.current == tab,
            onClick = { tabNavigator.current = tab },
            label = { Text(tab.options.title) },
            icon = {
                Icon(
                    painter = tab.options.icon!!,
                    contentDescription = tab.options.title,
                )
            },
        )
    }
}
