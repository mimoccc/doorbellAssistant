/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.phone.data.User
import org.mjdev.phone.extensions.ComposeExt.currentUser
import org.mjdev.phone.extensions.ComposeExt.currentWifiIP
import org.mjdev.phone.extensions.ContextExt.saveSystemProfile
import org.mjdev.phone.extensions.CustomExt.isPreview
import org.mjdev.phone.extensions.ModifierExt.applyIf
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.nsd.device.NsdType
import org.mjdev.phone.nsd.device.rememberNsdDeviceList
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneStrings

@Suppress("DEPRECATION", "ParamsComparedByRef")
@OptIn(ExperimentalCoroutinesApi::class)
@Previews
@Composable
fun NsdList(
    modifier: Modifier = Modifier,
    onCallClick: (NsdDevice) -> Unit = {},
    types: List<NsdType> = NsdType.entries,
) = PhoneTheme {
    val currentIP = currentWifiIP()
    var lastUpdated by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val user: User? by currentUser(lastUpdated)
    val devices: State<List<NsdDevice>> = if (isPreview) previewDevices()
    else rememberNsdDeviceList(
        types = types,
        filter = { s ->
            val serviceIP = s.host?.hostAddress ?: ""
            currentIP.contentEquals(serviceIP).not()
        }
    )
    var isUserDetailsEditorNeeded by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)
    ) {
        TopBarNsd(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            user = user,
            onUserPicClick = {
                isUserDetailsEditorNeeded = true
            }
        )
        TextLabel(
            modifier = Modifier
                .padding(bottom = 8.dp, top = 8.dp, start = 8.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.Bold,
            label = phoneStrings.labelListDevices,
            fontSize = 20.sp
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(
                items = devices.value,
                key = { d -> d.address ?: "-" }
            ) { device ->
                NsdItem(
                    device = device,
                    onCallClick = { onCallClick(device) },
                    showCallButton = true,
                )
            }
        }
    }
    UserEditDataSheet(
        modifier = Modifier.applyIf(isPreview) {
            alpha(0.5f)
        },
        isNeeded = isUserDetailsEditorNeeded,
        user = user,
        onSave = { userName, userPic ->
            saveSystemProfile(userName, userPic)
            lastUpdated = System.currentTimeMillis()
        },
        onDismiss = {
            isUserDetailsEditorNeeded = false
        }
    )
}

private fun previewDevices() = (1..32).map { i ->
    NsdDevice().apply { address = "192.168.1.$i" }
}.let { list ->
    mutableStateOf(list)
}
