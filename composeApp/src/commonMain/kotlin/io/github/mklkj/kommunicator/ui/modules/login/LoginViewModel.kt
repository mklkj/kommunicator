package io.github.mklkj.kommunicator.ui.modules.login

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class LoginViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel<LoginState>(LoginState()) {

    fun login(username: String, password: String) {
        if (!isFieldsValid(username, password)) {
            return mutableState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "All field are required",
                )
            }
        }

        launch("login_user", cancelExisting = false) {
            mutableState.update {
                it.copy(isLoading = true)
            }
            runCatching { userRepository.loginUser(username, password) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess {
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    private fun isFieldsValid(username: String, password: String): Boolean {
        var isValid = true

        if (username.isBlank()) {
            isValid = false
        }
        if (password.isBlank()) {
            isValid = false
        }

        return isValid
    }
}
