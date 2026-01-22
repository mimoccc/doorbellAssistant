/*
 * Copyright (c) Milan Jurkulák 2026.
 * Contact:
 * e: mimoccc@gmail.com
 * e: mj@mjdev.org
 * w: https://mjdev.org
 * w: https://github.com/mimoccc
 * w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
 */

package org.mjdev.phone.stream

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import org.mjdev.phone.exception.CallManagerException
import org.mjdev.phone.nsd.device.NsdDevice
import org.mjdev.phone.rpc.server.INsdServerRPC
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

// todo multiple clients & bussy
@Suppress("DEPRECATION", "USELESS_CAST")
class CallManager(
    private val context: Context,
    private val nsdRpcServer: INsdServerRPC?,
    private val callee: NsdDevice,
    private val caller: NsdDevice,
    private val isCaller: Boolean = false,
    private val enableIntelVp8Encoder: Boolean = true,
    private val enableH264HighProfile: Boolean = true,
    private val onLocalTrackReady: CallManager.(VideoTrack) -> Unit = {},
    private val onRemoteTrackReady: CallManager.(VideoTrack) -> Unit = {},
    private val onFailure: (Throwable) -> Unit = { e ->
        Log.e(TAG, "Failed to start UDP signaling", e)
    },
    private val onAcceptCall: CallManager.(String) -> Unit = {},
    private val onCallEnded: CallManager.(CallEndReason) -> Unit = {},
    private val onCallStarted: CallManager.(SessionDescription) -> Unit = {},
) : PeerConnection.Observer, ICallManager {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    val eglBaseContext: EglBase.Context?
        get() = eglBase.eglBaseContext

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val eglBase by lazy {
        EglBase.create()
    }
    private val decoderFactory by lazy {
        DefaultVideoDecoderFactory(eglBaseContext)
    }
    private val encoderFactory by lazy {
        DefaultVideoEncoderFactory(
            eglBaseContext,
            enableIntelVp8Encoder,
            enableH264HighProfile
        )
    }
    private val iceServers by lazy {
        listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
    }
    private val rtcConfig by lazy {
        PeerConnection.RTCConfiguration(iceServers).apply {
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "ICE candidate generated: ${candidate.sdp}")
        nsdRpcServer?.sendIceCandidate(callee, candidate)
        nsdRpcServer?.sendIceCandidate(caller, candidate)
    }

    override fun onTrack(transceiver: RtpTransceiver) {
        val track = transceiver.receiver.track()
        Log.d(TAG, "onTrack: track=$track, kind=${track?.kind()}")
        when (track) {
            is VideoTrack -> {
                remoteVideoTrack = track
                Log.d(TAG, "Remote video track ready: ${track.id()}")
                onRemoteTrackReady(track)
            }
            is AudioTrack -> {
                Log.d(TAG, "Remote audio track ready: ${track.id()}")
            }
        }
    }

    override fun onAddTrack(
        receiver: RtpReceiver,
        streams: Array<MediaStream>
    ) {
        val track = receiver.track()
        Log.d(TAG, "onAddTrack: track=$track, kind=${track?.kind()}, streams=${streams.size}")
        when (track) {
            is VideoTrack -> {
                remoteVideoTrack = track
                Log.d(TAG, "Remote video track ready via onAddTrack: ${track.id()}")
                onRemoteTrackReady(track)
            }
            is AudioTrack -> {
                Log.d(TAG, "Remote audio track ready via onAddTrack: ${track.id()}")
                // Audio tracks are automatically played by WebRTC
            }
        }
    }

    override fun onIceCandidatesRemoved(p0: Array<IceCandidate>?) {
    }

    override fun onSignalingChange(state: PeerConnection.SignalingState) {
        Log.d(TAG, "Signaling state: $state")
    }

    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
        Log.d(TAG, "ICE connection state: $state")
    }

    override fun onIceConnectionReceivingChange(receiving: Boolean) {
        Log.d(TAG, "ICE connection receiving: $receiving")
    }

    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
        Log.d(TAG, "ICE gathering state: $state")
    }

    override fun onRemoveStream(stream: MediaStream) {
        Log.d(TAG, "Stream removed: ${stream.id}")
    }

    override fun onDataChannel(channel: DataChannel) {
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "Renegotiation needed")
    }

    override fun onAddStream(stream: MediaStream) {
        Log.d(TAG, "Stream added: ${stream.id}, videoTracks=${stream.videoTracks.size}, audioTracks=${stream.audioTracks.size}")
        // Process audio tracks from stream
        stream.audioTracks.forEach { audioTrack ->
            Log.d(TAG, "Processing audio track from stream: ${audioTrack.id()}, enabled=${audioTrack.enabled()}")
        }
        stream.videoTracks.forEach { videoTrack ->
            Log.d(TAG, "Processing video track from stream: ${videoTrack.id()}, enabled=${videoTrack.enabled()}")
        }
    }

    fun initialize() {
        nsdRpcServer?.registerCallManager(this)
        initializePeerConnectionFactory()
        createPeerConnection()
        createLocalMediaStream()
        configureAudio()
    }

    override fun handleOfferReceived(sdp: String) {
        if (!isCaller) {
            Log.d(TAG, "Callee: Offer received")
            peerConnection?.setRemoteDescription(
                object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Callee: Remote offer set, creating answer")
                        createAnswer()
                    }

                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        Log.d(TAG, "Callee: Remote offer set, created answer ok.")
                    }

                    override fun onCreateFailure(sdp: String?) {
                        onFailure(CallManagerException("Callee: Failed to set remote answer: $sdp"))
                    }

                    override fun onSetFailure(sdp: String?) {
                        onFailure(CallManagerException("Callee: Failed to set remote answer: $sdp"))
                    }
                },
                SessionDescription(SessionDescription.Type.OFFER, sdp)
            )
        } else {
            Log.d(TAG, "Caller: Ignoring offer from peer")
        }
    }

    override fun handleAnswerReceived(sdp: String) {
        if (isCaller) {
            Log.d(TAG, "Caller: Answer received")
            peerConnection?.setRemoteDescription(
                object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Caller: Remote answer set successfully.")
                    }

                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        Log.d(TAG, "Create remote answer success, $sdp.")
                    }

                    override fun onCreateFailure(err: String?) {
                        onFailure(CallManagerException("Caller: Failed to set remote answer: $err"))
                    }

                    override fun onSetFailure(err: String?) {
                        onFailure(CallManagerException("Caller: Failed to set remote answer: $err"))
                    }
                },
                SessionDescription(SessionDescription.Type.ANSWER, sdp)
            )
        } else {
            Log.d(TAG, "Callee: Ignoring answer from peer")
        }
    }

    override fun handleAcceptReceived(sdp: String) {
        onAcceptCall(sdp)
    }

    override fun handleIceCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String) {
        Log.d(TAG, "Received ICE candidate: sdpMid=$sdpMid, sdpMLineIndex=$sdpMLineIndex")
        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        peerConnection?.addIceCandidate(candidate)
        Log.d(TAG, "ICE candidate added to peer connection")
    }

    override fun handleDismissReceived(reason: CallEndReason) {
        onCallEnded(reason)
    }

    override fun handleCallStarted(
        caller: NsdDevice,
        callee: NsdDevice
    ) {
        Log.d(TAG, "handleCallStarted: isCaller=$isCaller, caller=${caller.address}, callee=${callee.address}")
        if (isCaller) {
            Log.d(TAG, "Caller: Creating offer")
            createOffer()
        } else {
            Log.d(TAG, "Callee: Waiting for offer before creating answer")
        }
    }

    private fun initializePeerConnectionFactory() {
        Log.d(TAG, "Initializing PeerConnectionFactory")
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .createInitializationOptions()
        )
        val options = PeerConnectionFactory.Options().apply {
            // Disable audio processing that might cause issues
            // networkIgnoreMask = 0
        }
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setOptions(options)
            .createPeerConnectionFactory()
        Log.d(TAG, "PeerConnectionFactory initialized successfully")
    }

    private fun createPeerConnection() {
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, this)
        Log.d(TAG, "Peer connection created: $peerConnection")
    }

    private fun createLocalMediaStream() {
        Log.d(TAG, "Creating local media stream")
        audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", audioSource)
        videoCapturer = createVideoCapturer()
        videoSource = peerConnectionFactory?.createVideoSource(videoCapturer?.isScreencast ?: false)
        localVideoTrack = peerConnectionFactory?.createVideoTrack("video_track", videoSource)
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)
        localVideoTrack?.let { track ->
            Log.d(TAG, "Local video track created: ${track.id()}, enabled=${track.enabled()}")
            onLocalTrackReady(track)
            peerConnection?.addTrack(track, listOf("local_stream"))
        }
        localAudioTrack?.let { track ->
            Log.d(TAG, "Local audio track created: ${track.id()}, enabled=${track.enabled()}")
            peerConnection?.addTrack(track, listOf("local_stream"))
        }
        Log.d(TAG, "Local media stream created successfully")
    }

    private fun configureAudio() {
        Log.d(TAG, "Configuring audio for VoIP call")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    Log.d(TAG, "Audio focus changed: $focusChange")
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            Log.d(TAG, "Audio focus gained")
                        }
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            Log.d(TAG, "Audio focus lost permanently")
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            Log.d(TAG, "Audio focus lost temporarily")
                        }
                    }
                }
                .build()
            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            Log.d(TAG, "Audio focus request result: $result")
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN
            )
            Log.d(TAG, "Audio focus request result (legacy): $result")
        }
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        Log.d(TAG, "Audio mode set to: ${audioManager.mode}, Speaker: ${audioManager.isSpeakerphoneOn}")
    }

    private fun restoreAudio() {
        Log.d(TAG, "Restoring audio settings")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
                Log.d(TAG, "Audio focus abandoned")
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
            Log.d(TAG, "Audio focus abandoned (legacy)")
        }
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = false
    }

    private fun createVideoCapturer(): CameraVideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        deviceNames.firstOrNull { enumerator.isFrontFacing(it) }?.let { deviceName ->
            return enumerator.createCapturer(deviceName, null)
        }
        deviceNames.firstOrNull { enumerator.isBackFacing(it) }?.let { deviceName ->
            return enumerator.createCapturer(deviceName, null)
        }
        return null
    }

    private fun createOffer() {
        Log.d(TAG, "Caller: Starting to create offer")
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Caller: Offer created successfully")
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                nsdRpcServer?.sendOffer(callee, sdp.description)
                Log.d(TAG, "Caller: Offer sent to ${callee.address}")
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Caller: Failed to create offer: $error")
            }

            override fun onSetFailure(error: String) {
                Log.e(TAG, "Caller: Failed to set local description: $error")
            }
        }, MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        })
    }

    private fun createAnswer() {
        Log.d(TAG, "Callee: Starting to create answer")
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Callee: Answer created successfully")
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                nsdRpcServer?.sendAnswer(caller, sdp.description)
                Log.d(TAG, "Callee: Answer sent to ${caller.address}")
                onCallStarted(sdp)
            }

            override fun onSetSuccess() {
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Callee: Failed to create answer: $error")
            }

            override fun onSetFailure(error: String) {
                Log.e(TAG, "Callee: Failed to set local description: $error")
            }
        }, MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        })
    }

    fun callAccept() {
        nsdRpcServer?.sendAccept(caller, peerConnection?.localDescription?.description ?: "")
        nsdRpcServer?.sendAccept(callee, peerConnection?.localDescription?.description ?: "")
        createAnswer()
    }

    fun dismissCall(fromButton: Boolean) {
        nsdRpcServer?.sendDismiss(caller, peerConnection?.localDescription?.description ?: "")
        nsdRpcServer?.sendDismiss(callee, peerConnection?.localDescription?.description ?: "")
        if (fromButton) {
            onCallEnded(CallEndReason.LOCAL_END)
        }
    }

    fun toggleAudio(mute: Boolean) {
        localAudioTrack?.setEnabled(!mute)
    }

    fun toggleVideo(enable: Boolean) {
        localVideoTrack?.setEnabled(enable)
    }

    fun switchCamera() {
        (videoCapturer as? CameraVideoCapturer)?.switchCamera(null)
    }

    fun toggleSpeaker(enable: Boolean) {
        audioManager.isSpeakerphoneOn = enable
    }

    fun mute() {
        audioManager.isMicrophoneMute = true
    }

    fun unmute() {
        audioManager.isMicrophoneMute = false
    }

    fun release() {
        runCatching {
            restoreAudio()
            nsdRpcServer?.unregisterCallManager(this)
            peerConnection?.apply {
                close()
                dispose()
            }
            videoCapturer?.apply {
                stopCapture()
                dispose()
            }
            videoSource?.dispose()
            audioSource?.dispose()
            surfaceTextureHelper?.dispose()
            localVideoTrack?.dispose()
            localAudioTrack?.dispose()
            peerConnectionFactory?.dispose()
            eglBase.release()
        }.onFailure { e ->
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = CallManager::class.simpleName
    }
}
