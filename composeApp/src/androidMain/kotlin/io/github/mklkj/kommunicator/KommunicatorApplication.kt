package io.github.mklkj.kommunicator

import android.app.Application
import io.github.mklkj.kommunicator.di.initKoin

class KommunicatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
