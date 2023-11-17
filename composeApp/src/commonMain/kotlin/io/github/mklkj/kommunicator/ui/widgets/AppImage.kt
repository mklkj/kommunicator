package io.github.mklkj.kommunicator.ui.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun AppImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    KamelImage(
        resource = asyncPainterResource(url),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
