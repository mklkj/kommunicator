package io.github.mklkj.kommunicator

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController

@Suppress("unused")
fun MainViewController() = ComposeUIViewController(
    configure = {
        // fixes IME padding on iOS
        // https://github.com/JetBrains/compose-multiplatform/issues/4016#issuecomment-1924022813
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
    content = {
        App()
    },
)
