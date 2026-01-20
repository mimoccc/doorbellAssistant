/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.nsd.manager

import android.net.nsd.NsdManager
import androidx.annotation.IntDef

@Retention(AnnotationRetention.SOURCE)
@IntDef(NsdManager.PROTOCOL_DNS_SD)
annotation class ProtocolType
