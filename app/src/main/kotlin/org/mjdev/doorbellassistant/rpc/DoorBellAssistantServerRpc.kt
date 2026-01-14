package org.mjdev.doorbellassistant.rpc

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mjdev.doorbellassistant.rpc.CaptureRoute.captureRoute
import org.mjdev.phone.rpc.action.NsdAction
import org.mjdev.phone.rpc.server.NsdServerRpc
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionDetected
import org.mjdev.doorbellassistant.rpc.DoorBellActions.DoorBellActionMotionUnDetected

@Suppress("unused")
@OptIn(ExperimentalCoroutinesApi::class)
class DoorBellAssistantServerRpc(
    context: Context,
    onAction: (NsdAction) -> Unit = {}
) : NsdServerRpc(
    context = context,
    onAction = onAction,
    additionalRoutes = {
        captureRoute()
        createAction<DoorBellActionMotionDetected>()
        createAction<DoorBellActionMotionUnDetected>()
    }
)
