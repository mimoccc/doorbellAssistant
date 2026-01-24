/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.doorbellassistant.helpers.video

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLDisplay
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Process
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal class ChromaRenderThread(
    private val outputSurface: Surface,
    outputWidth: Int,
    outputHeight: Int,
    private val onInputSurfaceReady: (Surface) -> Unit
) : Thread("ChromaRenderThread") {

    @Volatile
    var inputSurface: Surface? = null
        private set

    private val running = AtomicBoolean(true)

    @Volatile
    private var outW = outputWidth

    @Volatile
    private var outH = outputHeight

    @Volatile
    private var videoW = 0

    @Volatile
    private var videoH = 0

    @Volatile
    private var keyR = 1f

    @Volatile
    private var keyG = 0.58f

    @Volatile
    private var keyB = 0f

    @Volatile
    private var threshold = 0.15f

    @Volatile
    private var softness = 0.06f

    fun setKeyColor(
        r: Float,
        g: Float,
        b: Float
    ) {
        keyR = r
        keyG = g
        keyB = b
    }

    fun setThreshold(
        t: Float
    ) {
        threshold = t.coerceIn(0f, 1f)
    }

    fun setSoftness(
        s: Float
    ) {
        softness = s.coerceIn(0f, 1f)
    }

    fun setOutputSize(
        w: Int,
        h: Int
    ) {
        outW = w
        outH = h
    }

    fun setVideoSize(
        w: Int,
        h: Int
    ) {
        videoW = w
        videoH = h
    }

    fun shutdown() {
        running.set(false)
    }

    override fun run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY)

        val egl = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val vers = IntArray(2)

        EGL14.eglInitialize(egl, vers, 0, vers, 1)

        val config = chooseEglConfig(egl)

        val ctx = EGL14.eglCreateContext(
            egl,
            config,
            EGL14.EGL_NO_CONTEXT,
            intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            ),
            0
        )

        val eglSurface = EGL14.eglCreateWindowSurface(
            egl,
            config,
            outputSurface,
            intArrayOf(EGL14.EGL_NONE),
            0
        )

        EGL14.eglMakeCurrent(egl, eglSurface, eglSurface, ctx)

        val oesTexId = createOesTexture()

        val inputSt = SurfaceTexture(oesTexId).apply {
            setOnFrameAvailableListener {
                frameAvailable.set(true)
            }
        }

        inputSurface = Surface(inputSt).also(onInputSurfaceReady)

        val program = buildProgram(VS, FS_OES_CHROMA)

        val aPos = GLES20.glGetAttribLocation(program, "aPosition")
        val aUv = GLES20.glGetAttribLocation(program, "aTexCoord")

        val uMvp = GLES20.glGetUniformLocation(program, "uMvp")
        val uTex = GLES20.glGetUniformLocation(program, "uTexture")
        val uKey = GLES20.glGetUniformLocation(program, "uKeyColor")
        val uTh = GLES20.glGetUniformLocation(program, "uThreshold")
        val uSoft = GLES20.glGetUniformLocation(program, "uSoftness")

        val quad = fullQuad()
        val mvp = FloatArray(16)

        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        while (running.get()) {
            if (!frameAvailable.getAndSet(false)) {
                try {
                    sleep(4)
                } catch (_: InterruptedException) {
                }
                continue
            }

            inputSt.updateTexImage()

            val ow = outW.coerceAtLeast(1)
            val oh = outH.coerceAtLeast(1)

            GLES20.glViewport(0, 0, ow, oh)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            Matrix.setIdentityM(mvp, 0)
            applyFitScale(
                mvp = mvp,
                outW = ow,
                outH = oh,
                videoW = videoW,
                videoH = videoH
            )

            GLES20.glUseProgram(program)

            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTexId)
            GLES20.glUniform1i(uTex, 0)

            GLES20.glUniform3f(uKey, keyR, keyG, keyB)
            GLES20.glUniform1f(uTh, threshold)
            GLES20.glUniform1f(uSoft, softness)

            quad.position(0)
            GLES20.glEnableVertexAttribArray(aPos)
            GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 16, quad)

            quad.position(2)
            GLES20.glEnableVertexAttribArray(aUv)
            GLES20.glVertexAttribPointer(aUv, 2, GLES20.GL_FLOAT, false, 16, quad)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisableVertexAttribArray(aPos)
            GLES20.glDisableVertexAttribArray(aUv)

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

            EGL14.eglSwapBuffers(egl, eglSurface)
        }

        inputSurface?.release()
        inputSt.release()
        outputSurface.release()

        EGL14.eglMakeCurrent(
            egl,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )

        EGL14.eglDestroySurface(egl, eglSurface)
        EGL14.eglDestroyContext(egl, ctx)
        EGL14.eglTerminate(egl)
    }

    private fun applyFitScale(
        mvp: FloatArray,
        outW: Int,
        outH: Int,
        videoW: Int,
        videoH: Int
    ) {
        if (videoW <= 0 || videoH <= 0) return

        val viewAspect = outW.toFloat() / outH.toFloat()
        val videoAspect = videoW.toFloat() / videoH.toFloat()

        val scaleX: Float
        val scaleY: Float

        if (viewAspect > videoAspect) {
            scaleX = videoAspect / viewAspect
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = viewAspect / videoAspect
        }

        Matrix.scaleM(mvp, 0, scaleX, scaleY, 1f)
    }

    private val frameAvailable = AtomicBoolean(false)

    private fun chooseEglConfig(
        display: EGLDisplay
    ): EGLConfig {
        val configs = arrayOfNulls<EGLConfig>(1)
        val num = IntArray(1)

        val attribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 0,
            EGL14.EGL_STENCIL_SIZE, 0,
            EGL14.EGL_NONE
        )

        check(
            EGL14.eglChooseConfig(display, attribs, 0, configs, 0, 1, num, 0) && num[0] > 0
        )

        return configs[0]!!
    }

    private fun createOesTexture(): Int {
        val tex = IntArray(1)

        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        return tex[0]
    }

    private fun fullQuad(): FloatBuffer {
        val data = floatArrayOf(
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f,
            -1f, 1f, 0f, 0f,
            1f, 1f, 1f, 0f
        )

        return ByteBuffer
            .allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }
    }

    private fun buildProgram(
        vs: String,
        fs: String
    ): Int {
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)

        val p = GLES20.glCreateProgram()
        GLES20.glAttachShader(p, v)
        GLES20.glAttachShader(p, f)
        GLES20.glLinkProgram(p)

        val ok = IntArray(1)
        GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0)
        check(ok[0] == GLES20.GL_TRUE) { GLES20.glGetProgramInfoLog(p) }

        GLES20.glDeleteShader(v)
        GLES20.glDeleteShader(f)

        return p
    }

    private fun compile(
        type: Int,
        src: String
    ): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)

        val ok = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0)
        check(ok[0] == GLES20.GL_TRUE) { GLES20.glGetShaderInfoLog(s) }

        return s
    }

    private companion object {

        private const val VS = """
attribute vec4 aPosition;
attribute vec2 aTexCoord;

uniform mat4 uMvp;

varying vec2 vTexCoord;

void main() {
    gl_Position = uMvp * aPosition;
    vTexCoord = aTexCoord;
}
"""

        private const val FS_OES_CHROMA = """
#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTexCoord;

uniform samplerExternalOES uTexture;
uniform vec3 uKeyColor;
uniform float uThreshold;
uniform float uSoftness;

void main() {
    vec4 c = texture2D(uTexture, vTexCoord);
    float d = distance(c.rgb, uKeyColor);
    float keep = smoothstep(uThreshold, uThreshold + uSoftness, d);
    gl_FragColor = vec4(c.rgb, c.a * keep);
}
"""
    }
}
