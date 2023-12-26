package io.github.mklkj.kommunicator

import io.github.mklkj.kommunicator.di.initKoin
import android.app.Application as AndroidApplication

class KommunicatorApplication : AndroidApplication() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
