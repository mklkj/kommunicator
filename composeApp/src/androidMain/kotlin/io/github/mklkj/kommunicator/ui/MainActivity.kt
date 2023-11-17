package io.github.mklkj.kommunicator.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.mklkj.kommunicator.App
import io.github.mklkj.kommunicator.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                App()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    AppTheme {
        App()
    }
}
