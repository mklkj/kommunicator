package io.github.mklkj.kommunicator.ui.utils

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * @see [https://github.com/adrielcafe/voyager/issues/292]
 */
val LocalNavigatorParent: Navigator
    @Composable get() = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
