package io.github.mklkj.kommunicator.ui.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @see [https://gist.github.com/EugeneTheDev/a27664cb7e7899f964348b05883cbccd]
 */
@Composable
fun DotsTyping(
    numberOfDots: Int = 3,
    dotSize: Dp = 6.dp,
    spaceBetween: Dp = 6.dp,
    dotColor: Color = Color.Blue,
    delayUnit: Int = 200,
    duration: Int = numberOfDots * delayUnit,
) {
    val maxOffset = (numberOfDots * 2).toFloat()

    val offsets = (0 until numberOfDots).map {
        animateOffsetWithDelay(
            delay = it * delayUnit,
            maxOffset = maxOffset,
            duration = duration,
            delayUnit = delayUnit,
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = maxOffset.dp)
    ) {
        offsets.forEach {
            Dot(
                offset = it.value,
                dotSize = dotSize,
                dotColor = dotColor,
            )
            Spacer(Modifier.width(spaceBetween))
        }
    }
}

@Composable
fun Dot(offset: Float, dotSize: Dp, dotColor: Color) {
    val modifier = Modifier
        .size(dotSize)
        .offset(y = -offset.dp)
        .background(
            color = dotColor,
            shape = CircleShape,
        )
    Spacer(modifier)
}

@Composable
fun animateOffsetWithDelay(
    delay: Int,
    maxOffset: Float,
    duration: Int,
    delayUnit: Int
) = rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(animation = keyframes {
        durationMillis = (duration * 1.5f).toInt()
        0f at delay using FastOutSlowInEasing
        maxOffset at delay + delayUnit using FastOutSlowInEasing
        0f at delay + (duration / 2)
    })
)
