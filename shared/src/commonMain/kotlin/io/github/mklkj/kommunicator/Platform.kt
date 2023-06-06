package io.github.mklkj.kommunicator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform