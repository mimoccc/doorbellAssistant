package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

// todo external definition
data class PhonePaddings(
    var callerPadding: PaddingValues = PaddingValues(0.dp),
    var calleePadding: PaddingValues = PaddingValues(end = 16.dp, bottom = 16.dp),
    var controlsPadding: PaddingValues = PaddingValues(16.dp),
)