package io.github.mklkj.kommunicator

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Factory

@Factory
class AppViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel<Boolean?>(null) {

    init {
        launch("load_user_info", isFlowObserver = true) {
            userRepository.isUserLoggedIn()
                .catch { proceedError(it) }
                .onEach { mutableState.value = it }
                .collect()
        }
    }
}
