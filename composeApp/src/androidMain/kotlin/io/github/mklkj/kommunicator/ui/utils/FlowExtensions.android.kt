package io.github.mklkj.kommunicator.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle as collectAsStateWithLifecycleAndroid
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

@Composable
actual fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    context: CoroutineContext,
): State<T> = collectAsStateWithLifecycleAndroid(context = context)
