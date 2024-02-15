package io.github.mklkj.kommunicator.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun rememberNotificationsPermissionController(
    onPermissionCheckResult: (Boolean) -> Unit,
): NotificationsPermissionController {
    val isPermissionGranted = remember { mutableStateOf(true) }

    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    LaunchedEffect(Unit) {
        notificationCenter.getNotificationSettingsWithCompletionHandler {
            Logger.i("Notification settings status: ${it?.authorizationStatus}")
            isPermissionGranted.value = it?.authorizationStatus == UNAuthorizationStatusAuthorized
            onPermissionCheckResult(isPermissionGranted.value)
        }
    }

    return object : NotificationsPermissionController {
        override val isPermissionGranted: State<Boolean> = isPermissionGranted
        override fun askNotificationPermission() {
            val options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge

            notificationCenter.requestAuthorizationWithOptions(options) { isGranted, _ ->
                isPermissionGranted.value = isGranted
            }
        }
    }
}
