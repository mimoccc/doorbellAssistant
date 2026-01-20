@file:Suppress("unused")

package org.mjdev.phone.helpers

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    uiMode = UI_MODE_TYPE_TELEVISION,
    showBackground = false,
    showSystemUi = false,
    device = Devices.TV_720p,
)
annotation class TvPreview

@Preview(
    name = "Portrait Light",
    group = "device",
    showBackground = false,
    showSystemUi = false,
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 480,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class PreviewPortraitLight

@Preview(
    name = "Portrait Dark",
    group = "device",
    showBackground = false,
    showSystemUi = false,
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 480,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class PreviewPortraitDark

@Preview(
    name = "Landscape Light",
    group = "device",
    device = Devices.AUTOMOTIVE_1024p,
    showBackground = false,
    showSystemUi = false,
    widthDp = 800,
    heightDp = 480,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class PreviewLandscapeLight

@Preview(
    name = "Landscape Dark",
    group = "device",
    device = Devices.AUTOMOTIVE_1024p,
    showBackground = false,
    showSystemUi = false,
    widthDp = 800,
    heightDp = 480,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class PreviewLandscapeDark

@PreviewPortraitLight
@PreviewPortraitDark
@PreviewLandscapeLight
@PreviewLandscapeDark
annotation class Previews
