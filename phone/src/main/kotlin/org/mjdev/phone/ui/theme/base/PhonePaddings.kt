/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

data class PhonePaddings(
    var callerPadding: PaddingValues = PaddingValues(0.dp),
    var calleePadding: PaddingValues = PaddingValues(end = 16.dp, bottom = 16.dp),
    var controlsPadding: PaddingValues = PaddingValues(16.dp),
)