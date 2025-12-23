package org.mjdev.doorbellassistant.helpers

import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.GREEN_DOMINATED_EXAMPLE

@Preview(
    uiMode = UI_MODE_TYPE_TELEVISION,
    showBackground = true,
    device = Devices.TV_720p,
    showSystemUi = false,
    backgroundColor = 0xFF444444,
    wallpaper = GREEN_DOMINATED_EXAMPLE,
)
annotation class TvPreview

@Preview(
    name = "Portrait",
    group = "device",
    showBackground = true,
    device = Devices.PIXEL_7,
    backgroundColor = 0xFF444444,
    widthDp = 480,
    heightDp = 800,
)
annotation class PreviewPortrait

@Preview(
    name = "Landscape",
    group = "device",
    device = Devices.AUTOMOTIVE_1024p,
    showBackground = true,
    backgroundColor = 0xFF444444,
    widthDp = 800,
    heightDp = 480,
)
annotation class PreviewLandscape

@PreviewPortrait
@PreviewLandscape
annotation class Previews
