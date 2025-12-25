package org.mjdev.doorbellassistant.helpers.nsd

import android.net.nsd.NsdManager
import androidx.annotation.IntDef

@Retention
@IntDef(NsdManager.PROTOCOL_DNS_SD)
annotation class ProtocolType