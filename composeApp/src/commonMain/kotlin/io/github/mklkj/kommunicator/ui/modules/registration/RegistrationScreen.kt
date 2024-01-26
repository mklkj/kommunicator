package io.github.mklkj.kommunicator.ui.modules.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.models.UserGender
import io.github.mklkj.kommunicator.ui.modules.login.LoginScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.utils.noRippleClickable
import io.github.mklkj.kommunicator.ui.widgets.RadioButtonWithLabel
import io.github.mklkj.kommunicator.ui.widgets.TextInput
import io.github.mklkj.kommunicator.ui.widgets.scaffoldPadding
import io.github.mklkj.kommunicator.utils.getMillis
import io.github.mklkj.kommunicator.utils.now
import io.github.mklkj.kommunicator.utils.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil

class RegistrationScreen : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() {
        val viewModel = getScreenModel<RegistrationViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val navigator = LocalNavigator.currentOrThrow

        if (state.isRegistered) {
            navigator.replace(LoginScreen())
        } else Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registration") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            }
        ) {
            RegistrationScreenContent(
                state = state,
                onSignUp = viewModel::signUp,
                modifier = Modifier.scaffoldPadding(it)
            )
        }
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
    private fun RegistrationScreenContent(
        state: RegistrationState,
        onSignUp: (RegistrationCredentials) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val (usernameRef, passwordRef, passwordConfirmRef) = remember { FocusRequester.createRefs() }
        val (emailRef, firstNameRef, lastNameRef) = remember { FocusRequester.createRefs() }

        val username = remember { mutableStateOf("") }
        val password = remember { mutableStateOf("") }
        val passwordConfirm = remember { mutableStateOf("") }
        val email = remember { mutableStateOf("") }
        val firstName = remember { mutableStateOf("") }
        val lastName = remember { mutableStateOf("") }

        var gender by remember { mutableStateOf<UserGender?>(null) }
        var dateOfBirth by remember { mutableStateOf<LocalDate?>(null) }
        var isDatePickerShown by remember { mutableStateOf(false) }

        Column(
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
        ) {
            TextInput(
                label = "Username",
                textState = username,
                focusRef = usernameRef,
                nextFocusRef = passwordRef,
            )
            TextInput(
                label = "Password",
                textState = password,
                keyboardType = KeyboardType.Password,
                focusRef = passwordRef,
                nextFocusRef = passwordConfirmRef,
            )
            TextInput(
                label = "Confirm password",
                textState = passwordConfirm,
                keyboardType = KeyboardType.Password,
                focusRef = passwordConfirmRef,
                nextFocusRef = emailRef,
            )
            TextInput(
                label = "E-mail",
                textState = email,
                keyboardType = KeyboardType.Email,
                focusRef = emailRef,
                nextFocusRef = firstNameRef,
            )
            TextInput(
                label = "First name",
                textState = firstName,
                capitalization = KeyboardCapitalization.Words,
                focusRef = firstNameRef,
                nextFocusRef = lastNameRef,
            )
            TextInput(
                label = "Last name",
                textState = lastName,
                capitalization = KeyboardCapitalization.Words,
                focusRef = lastNameRef,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gender: ")

                RadioButtonWithLabel(
                    label = "Male",
                    isSelected = gender == UserGender.MALE,
                    value = UserGender.MALE,
                    onValueSelect = { gender = UserGender.MALE },
                )
                RadioButtonWithLabel(
                    label = "Female",
                    isSelected = gender == UserGender.FEMALE,
                    value = UserGender.FEMALE,
                    onValueSelect = { gender = UserGender.FEMALE },
                )
            }
            OutlinedTextField(
                label = { Text("Date of birth") },
                value = dateOfBirth?.toString().orEmpty(),
                onValueChange = { dateOfBirth = LocalDate.parse(it) },
                singleLine = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    //For Icons
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { isDatePickerShown = true }
            )
            if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage,
                    color = Color.Red,
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSignUp(
                        RegistrationCredentials(
                            username = username.value,
                            password = password.value,
                            passwordConfirm = passwordConfirm.value,
                            email = email.value,
                            firstName = firstName.value,
                            lastName = lastName.value,
                            dateOfBirth = dateOfBirth,
                            gender = gender,
                        )
                    )
                },
                enabled = !state.isLoading,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                }
                Text("Sign up", Modifier.fillMaxWidth())
            }
        }

        // https://medium.com/@rahulchaurasia3592/material3-datepicker-and-datepickerdialog-in-compose-in-android-54ec28be42c3
        val defaultSelection = LocalDate.now().minus(DatePeriod(years = 13))
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (dateOfBirth ?: defaultSelection).getMillis(),
            yearRange = LocalDate.now().minus(DatePeriod(13)).let {
                it.minus(DatePeriod(100)).year..it.year
            },
        )
        if (isDatePickerShown) {
            DatePickerDialog(
                onDismissRequest = { isDatePickerShown = false },
                confirmButton = {
                    Button(
                        onClick = {
                            dateOfBirth = datePickerState.selectedDateMillis?.toLocalDate()
                            isDatePickerShown = false
                        }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { isDatePickerShown = false }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier,
                    dateFormatter = DatePickerFormatter(),
                    dateValidator = {
                        it.toLocalDate().periodUntil(LocalDate.now()).years >= 13
                    },
                    showModeToggle = true,
                )
            }
        }
    }
}
