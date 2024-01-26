package io.github.mklkj.kommunicator.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun TextInput(
    label: String,
    textState: MutableState<String>,
    focusRef: FocusRequester,
    nextFocusRef: FocusRequester? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
) {

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        label = { Text(label) },
        value = textState.value,
        onValueChange = {
            textState.value = it
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            imeAction = imeAction,
            keyboardType = keyboardType,
        ),
        visualTransformation = when {
            passwordVisible || keyboardType != KeyboardType.Password -> VisualTransformation.None
            else -> PasswordVisualTransformation()
        },
        trailingIcon = if (keyboardType == KeyboardType.Password) {
            {
                val image = when {
                    passwordVisible -> Icons.Filled.Visibility
                    else -> Icons.Filled.VisibilityOff
                }

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRef)
            .apply {
                if (nextFocusRef != null) {
                    focusProperties { next = nextFocusRef }
                }
            }
    )
}
