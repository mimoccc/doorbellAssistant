/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.helpers

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.mjdev.phone.vector.ImageVectorProvider.createVectorPainter
import kotlin.reflect.KProperty

class ImageVectorDelegate(
    private var vector: ImageVector
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): Painter = createVectorPainter(vector)

    companion object {
        operator fun ImageVector.provideDelegate(
            thisRef: Any?,
            property: KProperty<*>
        ): ImageVectorDelegate = ImageVectorDelegate(this)
    }
}
