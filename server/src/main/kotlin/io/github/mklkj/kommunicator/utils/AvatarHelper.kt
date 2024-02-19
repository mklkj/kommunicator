package io.github.mklkj.kommunicator.utils

import org.koin.core.annotation.Singleton
import kotlin.math.abs

@Singleton
class AvatarHelper {

    private val backgroundColors = listOf(
        "000033",
        "003333",
        "0099cc",
        "00cc33",
        "3300ff",
        "666600",
        "669933",
        "990099",
        "996666",
        "cc0000",
        "cc6633",
    )

    private val color = "fff"

    fun getUserAvatar(firstName: String, lastName: String, customName: String? = null): String {
        return getAvatar(customName ?: "$firstName $lastName")
    }

    fun getGroupAvatar(name: String): String = getAvatar(name)

    private fun getAvatar(name: String): String {
        return "https://ui-avatars.com/api/?background=${getColor(name)}&color=$color&name=$name"
    }

//    fun getGravatarAvatar(email: String): String {
//        return "https://gravatar.com/avatar/${md5(email)}"
//    }

    private fun getColor(name: String): String {
        return backgroundColors[abs(name.hashCode() % backgroundColors.size)]
    }
}
