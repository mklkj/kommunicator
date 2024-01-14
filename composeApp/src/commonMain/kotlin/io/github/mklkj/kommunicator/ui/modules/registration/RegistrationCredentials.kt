package io.github.mklkj.kommunicator.ui.modules.registration

import io.github.mklkj.kommunicator.data.models.UserGender
import kotlinx.datetime.LocalDate

data class RegistrationCredentials(
    val username: String,
    val password: String,
    val passwordConfirm: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate?,
    val gender: UserGender?,
)
