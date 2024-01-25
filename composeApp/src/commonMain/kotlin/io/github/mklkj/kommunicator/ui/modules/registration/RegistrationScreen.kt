package io.github.mklkj.kommunicator.ui.modules.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.mklkj.kommunicator.data.models.UserGender
import io.github.mklkj.kommunicator.ui.modules.login.LoginScreen
import io.github.mklkj.kommunicator.ui.utils.collectAsStateWithLifecycle
import io.github.mklkj.kommunicator.ui.utils.noRippleClickable
import io.github.mklkj.kommunicator.utils.getMillis
import io.github.mklkj.kommunicator.utils.now
import io.github.mklkj.kommunicator.utils.toLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.periodUntil

@OptIn(ExperimentalMaterial3Api::class)
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
                modifier = Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    @Composable
    @OptIn(ExperimentalComposeUiApi::class)
    private fun RegistrationScreenContent(
        state: RegistrationState,
        onSignUp: (RegistrationCredentials) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordConfirm by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        val (usernameRef, passwordRef, passwordConfirmRef) = remember { FocusRequester.createRefs() }
        val (emailRef, firstNameRef, lastNameRef) = remember { FocusRequester.createRefs() }
        var gender by remember { mutableStateOf<UserGender?>(null) }
        var dateOfBirth by remember { mutableStateOf<LocalDate?>(null) }
        var isDatePickerShown by remember { mutableStateOf(false) }

        Column(
            modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                label = { Text("Username") },
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(usernameRef)
                    .focusProperties { next = passwordRef }
            )
            OutlinedTextField(
                label = { Text("Password") },
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordRef)
                    .focusProperties { next = passwordConfirmRef }
            )
            OutlinedTextField(
                label = { Text("Confirm password") },
                value = passwordConfirm,
                onValueChange = { passwordConfirm = it },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordConfirmRef)
                    .focusProperties { next = emailRef }
            )
            OutlinedTextField(
                label = { Text("E-mail") },
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailRef)
                    .focusProperties { next = firstNameRef }
            )
            OutlinedTextField(
                label = { Text("First name") },
                value = firstName,
                onValueChange = { firstName = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(firstNameRef)
                    .focusProperties { next = lastNameRef }
            )
            OutlinedTextField(
                label = { Text("Last name") },
                value = lastName,
                onValueChange = { lastName = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(lastNameRef)
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gender: ")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.noRippleClickable {
                        gender = UserGender.MALE
                    }
                ) {
                    RadioButton(
                        selected = gender == UserGender.MALE,
                        onClick = { gender = UserGender.MALE },
                    )
                    Text(
                        text = "Male",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.noRippleClickable {
                        gender = UserGender.FEMALE
                    }
                ) {
                    RadioButton(
                        selected = gender == UserGender.FEMALE,
                        onClick = { gender = UserGender.FEMALE },
                    )
                    Text(
                        text = "Female",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
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
            Button(
                onClick = {
                    onSignUp(
                        RegistrationCredentials(
                            username = username,
                            password = password,
                            passwordConfirm = passwordConfirm,
                            email = email,
                            firstName = firstName,
                            lastName = lastName,
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
