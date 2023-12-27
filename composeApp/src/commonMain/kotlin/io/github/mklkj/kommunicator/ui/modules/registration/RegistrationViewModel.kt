package io.github.mklkj.kommunicator.ui.modules.registration

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class RegistrationViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel<RegistrationState>(RegistrationState()) {

    fun signUp(username: String, password: String) {
        val isNotValid = username.isBlank() || password.isBlank()
        mutableState.update {
            it.copy(
                isLoading = false,
                errorMessage = when {
                    isNotValid -> "All field are required"
                    else -> it.errorMessage
                }
            )
        }
        if (isNotValid) return

        launch("register_user") {
            runCatching { userRepository.registerUser(username, password) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess {
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = true,
                            errorMessage = null,
                        )
                    }
                }
        }
    }
}
