package org.mjdev.phone.ui.theme.base

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class PhoneShapes(
    var callerShape: Shape = RoundedCornerShape(16.dp),
    var calleeShape: Shape = RoundedCornerShape(16.dp),
    var controlsShape: Shape = CircleShape,
    var labelsShape: Shape = CircleShape,
    var headerLogoShape: Shape = CircleShape,
    var headerIconShape: Shape = CircleShape,
    var callControlButtonShape: Shape = CircleShape,
    var settingsControlButtonShape: Shape = CircleShape,
    var deviceLogoShape: Shape = CircleShape,
)
