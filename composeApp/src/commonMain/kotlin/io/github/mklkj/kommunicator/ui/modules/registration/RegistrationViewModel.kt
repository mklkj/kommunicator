package io.github.mklkj.kommunicator.ui.modules.registration

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class RegistrationViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel<RegistrationState>(RegistrationState()) {

    fun signUp(credentials: RegistrationCredentials) {
        if (!isFieldsValid(credentials)) return

        mutableState.update { it.copy(isLoading = true) }
        launch("register_user") {
            runCatching { userRepository.registerUser(credentials) }
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

    private fun isFieldsValid(credentials: RegistrationCredentials): Boolean {
        var isValid = true
        var firstMessage: String? = null

        if (credentials.username.isBlank()) {
            isValid = false
            firstMessage = "Username can't be empty"
        }

        if (isValid && credentials.password.isBlank()) {
            isValid = false
            firstMessage = "Password can't be empty"
        }

        if (isValid && credentials.passwordConfirm != credentials.password) {
            isValid = false
            firstMessage = "Passwords doesn't match"
        }

        if (isValid && "@" !in credentials.email) {
            isValid = false
            firstMessage = "E-mail is invalid"
        }

        if (isValid && credentials.gender == null) {
            isValid = false
            firstMessage = "Gender is required"
        }

        if (isValid && credentials.dateOfBirth == null) {
            isValid = false
            firstMessage = "Date of birth is required"
        }

        mutableState.update {
            it.copy(
                isLoading = false,
                errorMessage = firstMessage ?: it.errorMessage,
            )
        }

        return isValid
    }
}
