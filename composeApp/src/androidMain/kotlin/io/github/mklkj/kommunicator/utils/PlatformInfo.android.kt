package io.github.mklkj.kommunicator.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import android.provider.Settings.Secure.ANDROID_ID
import io.github.mklkj.kommunicator.ui.utils.PlatformInfo

class PlatformInfoAndroid(context: Context) : PlatformInfo {

    @SuppressLint("HardwareIds")
    override val deviceId: String? = Secure.getString(context.contentResolver, ANDROID_ID)
    override val deviceName: String = buildString {
        append(android.os.Build.MANUFACTURER)
        append(" ")
        append(android.os.Build.MODEL)
    }
}
