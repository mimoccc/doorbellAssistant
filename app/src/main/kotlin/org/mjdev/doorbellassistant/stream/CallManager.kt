package org.mjdev.doorbellassistant.stream

import android.content.Context
import android.media.AudioManager
import android.util.Log
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

@Suppress("DEPRECATION", "USELESS_CAST")
class CallManager(
    private val context: Context,
    private val remoteIp: String,
    private val isCaller: Boolean = false,
    private val enableIntelVp8Encoder: Boolean = true,
    private val enableH264HighProfile: Boolean = true,
    private val signallingPort: Int = 8889,
    private val onLocalTrackReady: (VideoTrack) -> Unit = {},
    private val onRemoteTrackReady: (VideoTrack) -> Unit = {},
    private val onCallEnded: (CallEndReason) -> Unit = {},
    private val onCallStarted: () -> Unit = {},
) : PeerConnection.Observer {
    companion object {
        private val TAG = CallManager::class.simpleName
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var audioSource: AudioSource? = null
    private var videoSource: VideoSource? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null

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
    private val signalingClient: UdpSignalingClient by lazy {
        UdpSignalingClient(
            remoteIp,
            signallingPort,
            onOfferReceived = { sdp ->
                if (!isCaller) {
                    Log.d(TAG, "Callee: Offer received")
                    peerConnection?.setRemoteDescription(
                        object : SdpObserver {
                            override fun onSetSuccess() {
                                Log.d(TAG, "Callee: Remote offer set, creating answer")
                                createAnswer()
                            }

                            override fun onCreateSuccess(p0: SessionDescription?) {
                            }

                            override fun onCreateFailure(p0: String?) {
                                Log.e(TAG, "Callee: Failed to create answer: $p0")
                            }

                            override fun onSetFailure(p0: String?) {
                                Log.e(TAG, "Callee: Failed to set remote offer: $p0")
                            }
                        },
                        SessionDescription(SessionDescription.Type.OFFER, sdp)
                    )
                } else {
                    Log.d(TAG, "Caller: Ignoring offer from peer")
                }
            },
            onAnswerReceived = { sdp ->
                if (isCaller) {
                    Log.d(TAG, "Caller: Answer received")
                    peerConnection?.setRemoteDescription(
                        object : SdpObserver {
                            override fun onSetSuccess() {
                                Log.d(TAG, "Caller: Remote answer set successfully")
                            }

                            override fun onCreateSuccess(p0: SessionDescription?) {
                            }

                            override fun onCreateFailure(p0: String?) {
                            }

                            override fun onSetFailure(p0: String?) {
                                Log.e(TAG, "Caller: Failed to set remote answer: $p0")
                            }
                        },
                        SessionDescription(
                            SessionDescription.Type.ANSWER,
                            sdp
                        )
                    )
                } else {
                    Log.d(TAG, "Callee: Ignoring answer from peer")
                }
            },
            onIceCandidateReceived = { candidate ->
                peerConnection?.addIceCandidate(candidate)
            },
            onDismissReceived = { reason ->
                onCallEnded(reason)
//                when (reason) {
//                    CallEndReason.REMOTE_PARTY_END -> {
//
//                    }
//                    else -> this@CallManager.release(reason)
//                }
            }
        )
    }

    val eglBaseContext: EglBase.Context?
        get() = eglBase.eglBaseContext

    override fun onIceCandidate(candidate: IceCandidate) {
        Log.d(TAG, "ICE candidate generated: ${candidate.sdp}")
        signalingClient.sendIceCandidate(candidate)
    }

    override fun onTrack(transceiver: RtpTransceiver) {
        val track = transceiver.receiver.track()
        Log.d(TAG, "onTrack: track=$track, kind=${track?.kind()}")
        if (track is VideoTrack) {
            remoteVideoTrack = track
            Log.d(TAG, "Remote video track ready: ${track.id()}")
            onRemoteTrackReady(track)
        }
    }

    override fun onAddTrack(
        receiver: RtpReceiver,
        streams: Array<MediaStream>
    ) {
        val track = receiver.track()
        Log.d(TAG, "onAddTrack: track=$track, kind=${track?.kind()}, streams=${streams.size}")
        if (track is VideoTrack) {
            remoteVideoTrack = track
            Log.d(TAG, "Remote video track ready via onAddTrack: ${track.id()}")
            onRemoteTrackReady(track)
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
        Log.d(TAG, "Stream added: ${stream.id}, videoTracks=${stream.videoTracks.size}")
    }

    fun initialize() {
        initializePeerConnectionFactory()
        createPeerConnection()
        createLocalMediaStream()
        startSignaling()
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection() {
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, this)
    }

    private fun createLocalMediaStream() {
        audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", audioSource)
        videoCapturer = createVideoCapturer()
        videoSource = peerConnectionFactory?.createVideoSource(videoCapturer?.isScreencast ?: false)
        localVideoTrack = peerConnectionFactory?.createVideoTrack("video_track", videoSource)
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
        videoCapturer?.startCapture(1280, 720, 30)
        localVideoTrack?.let { track ->
            Log.d(TAG, "Local video track created: ${track.id()}")
            onLocalTrackReady(track)
            peerConnection?.addTrack(track, listOf("local_stream"))
        }
        localAudioTrack?.let { track ->
            Log.d(TAG, "Local audio track created: ${track.id()}")
            peerConnection?.addTrack(track, listOf("local_stream"))
        }
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

    private fun startSignaling() {
        signalingClient.connect {
            if (isCaller) {
                Log.d(TAG, "Caller: UDP signaling ready, creating offer")
                createOffer()
            } else {
                Log.d(TAG, "Callee: UDP signaling ready, waiting for offer")
            }
        }
    }

    private fun createOffer() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                signalingClient.sendOffer(sdp.description)
                Log.d(TAG, "Caller: Offer sent")
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
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)
                signalingClient.sendAnswer(sdp.description)
                Log.d(TAG, "Callee: Answer sent")
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

    fun dismiss(fromButton: Boolean) {
        signalingClient.sendDismiss(
            peerConnection?.localDescription?.description ?: ""
        )
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

    fun release(reason: CallEndReason) = runCatching {
        onCallEnded(reason)
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
        signalingClient.release()
        surfaceTextureHelper?.dispose()
        localVideoTrack?.dispose()
        localAudioTrack?.dispose()
        peerConnectionFactory?.dispose()
        eglBase.release()
    }.onFailure { e ->
        e.printStackTrace()
    }
}
