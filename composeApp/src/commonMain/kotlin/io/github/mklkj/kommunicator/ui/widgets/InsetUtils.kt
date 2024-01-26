package io.github.mklkj.kommunicator.ui.widgets

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * @see [https://issuetracker.google.com/issues/249727298#comment8]
 */
@Stable
@OptIn(ExperimentalLayoutApi::class)
fun Modifier.scaffoldPadding(paddingValues: PaddingValues) = this
    .padding(paddingValues)
    .consumeWindowInsets(paddingValues)
    .systemBarsPadding()
