/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.data

import android.os.Build
import org.mjdev.phone.helpers.json.Serializable
import org.mjdev.phone.nsd.device.INsdDetail

// todo safe map
@Serializable
class DeviceDetails(
    map: MutableMap<String, String> = HashMap<String, String>()
) : INsdDetail(map) {
    var type: String by this
    var board: String by this
    var brand: String by this
    var display: String by this
    var model: String by this
    var manufacturer: String by this
    var product: String by this
//    var isTablet: Boolean by this
//    var isMobile: Boolean by this
//    var isTV: Boolean by this
//    var displaySize: IntSize by this
//    var dpi: Float by this

    companion object {
        val THIS = DeviceDetails().apply {
            type = Build.TYPE
            board = Build.BOARD
            brand = Build.BRAND
            display = Build.DISPLAY
            model = Build.MODEL
            manufacturer = Build.MANUFACTURER
            product = Build.PRODUCT
//            isTablet = false
//            isMobile = false
//            isTV = false
//            displaySize = IntSize.Zero
//            dpi = 0f
        }
    }
}