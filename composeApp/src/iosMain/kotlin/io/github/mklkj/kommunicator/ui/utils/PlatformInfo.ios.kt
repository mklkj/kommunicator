package io.github.mklkj.kommunicator.ui.utils

import platform.UIKit.UIDevice

class PlatformInfoIos : PlatformInfo {
    override val deviceId: String? = UIDevice.currentDevice.identifierForVendor?.UUIDString
    override val deviceName: String = buildString {
        append(UIDevice.currentDevice.systemName())
        append(" ")
        append(UIDevice.currentDevice.systemVersion)
    }
}
