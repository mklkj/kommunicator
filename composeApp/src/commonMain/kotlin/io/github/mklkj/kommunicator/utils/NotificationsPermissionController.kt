package io.github.mklkj.kommunicator.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
expect fun rememberNotificationsPermissionController(
    onPermissionCheckResult: (Boolean) -> Unit,
): NotificationsPermissionController

interface NotificationsPermissionController {

    val isPermissionGranted: State<Boolean>

    fun askNotificationPermission()
}
