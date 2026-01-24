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

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import org.mjdev.phone.data.User
import org.mjdev.phone.extensions.ComposeExt.rememberImageBitmapFromUri
import org.mjdev.phone.helpers.Previews
import org.mjdev.phone.ui.theme.base.PhoneTheme
import org.mjdev.phone.ui.theme.base.phoneColors
import org.mjdev.phone.ui.theme.base.phoneIcons
import org.mjdev.phone.ui.theme.base.phoneShapes

@Suppress("ParamsComparedByRef")
@Previews
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDataSheet(
    modifier: Modifier = Modifier,
    user: User? = null,
    isNeeded: Boolean = user == null,
    onDismiss: () -> Unit = {},
    onSave: Context.(name: String?, photoUri: Uri?) -> Unit = { _, _ -> },
) = PhoneTheme {
    val context = LocalContext.current
    if (isNeeded) {
        var userName: String by rememberSaveable(user, user?.name) {
            mutableStateOf(user?.name ?: "")
        }
        var photoUri: Uri by rememberSaveable(user, user?.photoUri) {
            mutableStateOf(user?.photoUri?.toUri() ?: Uri.EMPTY)
        }
        val pickPhoto = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                photoUri = uri ?: Uri.EMPTY
                onSave(context, userName, photoUri)
            }
        )
        DismissableModalSheet(
            modifier = modifier.background(Color.Transparent),
            onDismissRequest = onDismiss,
            containerColor = phoneColors.colorBackground,
            tonalElevation = 5.dp,
            scrimColor = phoneColors.colorScrim,
        ) {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp)
            ) {
                val bitmap by rememberImageBitmapFromUri(photoUri)
                if (bitmap != null) {
                    Image(
                        painter = bitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = phoneColors.colorIconsBackground,
                                shape = phoneShapes.headerLogoShape
                            )
                            .border(
                                2.dp,
                                phoneColors.colorCallerIconBorder,
                                phoneShapes.headerLogoShape
                            )
                            .clip(phoneShapes.headerLogoShape)
                            .padding(2.dp),
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = phoneColors.colorIconsBackground,
                                shape = phoneShapes.headerLogoShape
                            )
                            .border(
                                2.dp,
                                phoneColors.colorCallerIconBorder,
                                phoneShapes.headerLogoShape
                            )
                            .clip(phoneShapes.headerLogoShape),
                        contentDescription = "",
                        painter = phoneIcons.userAccountIcon,
                        tint = phoneColors.colorIconTint,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        contentColor = phoneColors.colorControlsButtonEnabledIcon,
                        containerColor = phoneColors.colorButtonColor
                    ),
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text("Select picture")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = userName,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = phoneColors.colorText,
                        unfocusedTextColor = phoneColors.colorText,
                        focusedLabelColor = phoneColors.colorText,
                        unfocusedLabelColor = phoneColors.colorText,
                        focusedContainerColor = phoneColors.colorScrim,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = phoneColors.colorLabelsBackground,
                        unfocusedIndicatorColor = phoneColors.colorLabelsBackground,
                        focusedPlaceholderColor = phoneColors.colorText,
                        unfocusedPlaceholderColor = phoneColors.colorText,
                    ),
                    onValueChange = { value ->
                        userName = value
                        onSave(context, value, photoUri)
                    },
                    singleLine = true,
                    label = {
                        Text("Name")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        contentColor = phoneColors.colorControlsButtonEnabledIcon,
                        containerColor = phoneColors.colorButtonColor
                    ),
//                    enabled = userName.isNotBlank(),
                    onClick = {
                        onSave(context, userName.trim(), photoUri)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}


