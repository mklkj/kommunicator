package io.github.mklkj.kommunicator.ui.modules.registration

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class RegistrationViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    fun signUp(username: String, password: String) {
        val isNotValid = username.isBlank() || password.isBlank()
        _state.update {
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess {
                    _state.update {
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
