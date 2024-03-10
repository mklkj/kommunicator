package org.koin.ksp.generated

import org.koin.core.module.Module

// https://github.com/google/ksp/issues/929#issuecomment-1826321787
@Suppress("UnusedReceiverParameter", "unused")
val Any.module: Module
    get() = throw RuntimeException("Koin module was not generated. Add ksp for all your targets")
