package io.github.mklkj.kommunicator.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

@Composable
actual fun rememberNotificationsPermissionController(
    onPermissionCheckResult: (Boolean) -> Unit,
): NotificationsPermissionController {
    val context = LocalContext.current

    val isPermissionGranted = remember { mutableStateOf(context.hasNotificationPermission()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionCheckResult(isGranted)
        isPermissionGranted.value = isGranted
    }

    return object : NotificationsPermissionController {
        override val isPermissionGranted: State<Boolean> = isPermissionGranted
        override fun askNotificationPermission() = launcher.launch(POST_NOTIFICATIONS)
    }
}

private fun Context.hasNotificationPermission() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        val result = ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
        result == PackageManager.PERMISSION_GRANTED
    }

    else -> true
}
