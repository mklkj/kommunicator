package io.github.mklkj.kommunicator.domain

import co.touchlab.kermit.Logger
import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.mklkj.kommunicator.data.repository.UserRepository
import org.koin.core.annotation.Factory

@Factory
class LogOutUseCase(
    private val userRepository: UserRepository,
) {

    suspend operator fun invoke() {
        NotifierManager.getPushNotifier().deleteMyToken()
        userRepository.logout()
        Logger.i("User logout")
    }
}
