package io.github.mklkj.kommunicator.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    includes(koinDefaultModule)
}

expect val koinDefaultModule: Module
