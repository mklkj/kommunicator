package io.github.mklkj.kommunicator.ui.modules.account

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.domain.LogOutUseCase
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class AccountViewModel(
    private val userRepository: UserRepository,
    private val logOutUseCase: LogOutUseCase,
) : BaseViewModel<AccountState>(AccountState()) {

    init {
        loadData()
    }

    private fun loadData() {
        launch("load_account", cancelExisting = false) {
            runCatching { userRepository.getCurrentUser() }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            errorMessage = error.message,
                            isLoading = false,
                        )
                    }
                }
                .onSuccess { user ->
                    mutableState.update {
                        it.copy(
                            errorMessage = null,
                            isLoading = false,
                            user = user,
                        )
                    }
                }
        }
    }

    fun logout() {
        launch("logout_user", cancelExisting = false) {
            logOutUseCase()
        }
    }
}
