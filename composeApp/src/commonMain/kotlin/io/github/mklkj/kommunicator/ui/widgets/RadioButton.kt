package io.github.mklkj.kommunicator.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mklkj.kommunicator.ui.utils.noRippleClickable

@Composable
fun <T> RadioButtonWithLabel(
    label: String,
    isSelected: Boolean,
    value: T,
    onValueSelect: (T) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.noRippleClickable {
            onValueSelect(value)
        }
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onValueSelect(value) },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
        )
    }
}
