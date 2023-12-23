package io.github.mklkj.kommunicator.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

actual val platformModule: Module = module {
    includes(defaultModule)
}
