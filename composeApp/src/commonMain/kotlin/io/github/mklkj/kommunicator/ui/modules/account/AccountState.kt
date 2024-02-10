package io.github.mklkj.kommunicator.ui.modules.account

import io.github.mklkj.kommunicator.Users

data class AccountState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: Users? = null,
)
