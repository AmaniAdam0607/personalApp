package org.dromio.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.rotate(degrees: Float) = this.then(
    graphicsLayer(
        rotationZ = degrees
    )
)
