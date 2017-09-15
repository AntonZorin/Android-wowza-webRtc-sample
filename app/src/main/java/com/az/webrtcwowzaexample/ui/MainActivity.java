package com.az.webrtcwowzaexample.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.az.webrtcwowzaexample.R;
import com.az.webrtcwowzaexample.common.Constants;
import com.az.webrtcwowzaexample.common.PrefManager;
import com.az.webrtcwowzaexample.common.RTCAudioManager;
import com.az.webrtcwowzaexample.common.UnhandledExceptionHandler;
import com.az.webrtcwowzaexample.models.IceCandidateModel;
import com.az.webrtcwowzaexample.network.SocketHelper;
import com.az.webrtcwowzaexample.network.SocketHelper.SignalingEvents;
import com.az.webrtcwowzaexample.streaming.PeerConnectionClient;
import com.az.webrtcwowzaexample.streaming.PeerConnectionEvents;
import com.az.webrtcwowzaexample.streaming.PeerConnectionParameters;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "APP_RTC";

    PeerConnectionClient localPeerConnectionClient;
    PeerConnectionClient remotePeerConnectionClient;
    PeerConnectionParameters peerConnectionParameters;
    SocketHelper localSocketHelper;
    SocketHelper remoteSocketHelper;
    EglBase eglBase;
    RTCAudioManager audioManager;

    SurfaceViewRenderer localRenderer;
    SurfaceViewRenderer remoteRenderer;
    EditText remoteStreamNameEditText;
    EditText localStreamNameEditText;
    private VideoCapturer videoCapturer;

    PeerConnectionEvents localPeerConnectionEvents = new PeerConnectionEvents() {
        @Override
        public void onLocalDescription(final SessionDescription sdp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (localSocketHelper != null) {
                        log("localPeerConnectionEvents onLocalDescription " + sdp.type);
                        String streamName = localStreamNameEditText.getText().toString();
                        localSocketHelper.sendOfferSdp(streamName, sdp);
                    }
                }
            });
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        }

        @Override
        public void onIceConnected() {

        }

        @Override
        public void onIceDisconnected() {

        }

        @Override
        public void onPeerConnectionClosed() {

        }

        @Override
        public void onPeerConnectionStatsReady(StatsReport[] reports) {

        }

        @Override
        public void onPeerConnectionError(String description) {

        }
    };
    PeerConnectionEvents remotePeerConnectionEvents = new PeerConnectionEvents() {
        @Override
        public void onLocalDescription(SessionDescription sdp) {
            log("remotePeerConnectionEvents onLocalDescription " + sdp.type);
            remoteSocketHelper.sendPlayResponse(sdp);
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {

        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] candidates) {

        }

        @Override
        public void onIceConnected() {

        }

        @Override
        public void onIceDisconnected() {

        }

        @Override
        public void onPeerConnectionClosed() {

        }

        @Override
        public void onPeerConnectionStatsReady(StatsReport[] reports) {

        }

        @Override
        public void onPeerConnectionError(String description) {

        }
    };

    SignalingEvents localSignalingEvents = new SignalingEvents() {
        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectedInternal();
                }
            });
        }

        @Override
        public void onConnectionEstablished() {
            log("localSignalingEvents onConnectionEstablished");
//            remoteSocketHelper.closeWebSocket();
        }

        @Override
        public void onIceCandidatesAdded(final List<IceCandidateModel> iceCandidates) {
            log("localSignalingEvents onIceCandidatesAdded: " + iceCandidates.size());
            localPeerConnectionClient.setIceCandidates(iceCandidates);
        }

        @Override
        public void onRemoteDescription(final SessionDescription sdp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (localPeerConnectionClient == null) {
                        log("Received remote SDP for non-initilized peer connection.");
                        return;
                    }
                    log("localSignalingEvents onRemoteDescription " + sdp.type);
                    localPeerConnectionClient.setRemoteDescription(sdp);
                }
            });
        }

        private void onConnectedInternal() {
            log("localSignalingEvents onConnectedInternal");
//            if (isCamera2Supported()) {
//                Logging.d(TAG, "Creating capturer using camera2 API.");
//                videoCapturer = createVideoCapturer(new Camera2Enumerator(getApplicationContext()));
//            } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createVideoCapturer(new Camera1Enumerator(false));
//            }
            if (videoCapturer == null) {
                log("Failed to open camera");
            }
            localPeerConnectionClient.createPeerConnection(eglBase.getEglBaseContext(), localRenderer,
                    remoteRenderer, videoCapturer);

        }
    };
    SignalingEvents remoteSignalingEvents = new SignalingEvents() {
        @Override
        public void onConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectedInternal();
                }
            });
        }

        @Override
        public void onConnectionEstablished() {
            log("remoteSignalingEvents onConnectionEstablished");
//            remoteSocketHelper.closeWebSocket();
        }

        @Override
        public void onIceCandidatesAdded(final List<IceCandidateModel> iceCandidates) {
            log("remoteSignalingEvents onIceCandidatesAdded: " + iceCandidates.size());
            remotePeerConnectionClient.setIceCandidates(iceCandidates);
            remotePeerConnectionClient.drainCandidates();
        }

        @Override
        public void onRemoteDescription(final SessionDescription spd) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (remotePeerConnectionClient == null) {
                        log("Received remote SDP for non-initilized peer connection.");
                        return;
                    }
                    log("remoteSignalingEvents onRemoteDescription " + spd.type);
                    remotePeerConnectionClient.setRemoteDescription(spd);
                    remotePeerConnectionClient.createAnswer();
                }
            });
        }

        private void onConnectedInternal() {
            log("remoteSignalingEvents onConnectedInternal");
            remotePeerConnectionClient.createPeerConnection(eglBase.getEglBaseContext(), localRenderer,
                    remoteRenderer, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (localPeerConnectionClient != null) {
            localPeerConnectionClient.startVideoSource();
        }
    }

    @Override
    protected void onStop() {
        if (localPeerConnectionClient != null) {
            localPeerConnectionClient.stopVideoSource();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        eglBase.release();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.connect_local) {
            connectLocal();
        }
        if (view.getId() == R.id.connect_remote) {
            connectRemote();
        }
    }

    private void connectRemote() {
        String streamName = remoteStreamNameEditText.getText().toString();
        if (!streamName.isEmpty()) {
            remoteSocketHelper.sendOffer(streamName);
            PrefManager.saveRemoteStreamName(getApplicationContext(), streamName);
        }
    }

    private void connectLocal() {
        String streamName = localStreamNameEditText.getText().toString();
        if (!streamName.isEmpty()) {
            localPeerConnectionClient.createOffer();
            PrefManager.saveLocalStreamName(getApplicationContext(), streamName);
        }

    }

    private void initViews() {
        localRenderer = (SurfaceViewRenderer) findViewById(R.id.local_renderer);
        remoteRenderer = (SurfaceViewRenderer) findViewById(R.id.remote_renderer);

        localStreamNameEditText = (EditText) findViewById(R.id.local_stream_name);
        String localStreamName = PrefManager.getLocalStreamName(getApplicationContext());
        localStreamNameEditText.setText(localStreamName);

        remoteStreamNameEditText = (EditText) findViewById(R.id.remote_stream_name);
        String remoteStreamName = PrefManager.getRemoteStreamName(getApplicationContext());
        remoteStreamNameEditText.setText(remoteStreamName);

        localRenderer.init(eglBase.getEglBaseContext(), null);
        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRenderer.setEnableHardwareScaler(true);

        remoteRenderer.init(eglBase.getEglBaseContext(), null);
        remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteRenderer.setEnableHardwareScaler(true);

        findViewById(R.id.connect_local).setOnClickListener(this);
        findViewById(R.id.connect_remote).setOnClickListener(this);
        log("initViews done");
    }

    private void initVars() {
//        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
        eglBase = EglBase.create();
        // Create peer connection factory.

        localPeerConnectionClient = PeerConnectionClient.create(true);
        remotePeerConnectionClient = PeerConnectionClient.create(false);

        peerConnectionParameters = new PeerConnectionParameters(320, 240, 30, 1700,
                Constants.VIDEO_CODEC_VP8, true, 32,
                Constants.AUDIO_CODEC_OPUS,
                false, false, false, false, false);

        PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), peerConnectionParameters.videoCodecHwAcceleration);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        localPeerConnectionClient.setPeerConnectionFactoryOptions(options);
        remotePeerConnectionClient.setPeerConnectionFactoryOptions(options);

        log("createPeerConnectionFactory");
        localPeerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, localPeerConnectionEvents);
        remotePeerConnectionClient.createPeerConnectionFactory(getApplicationContext(), peerConnectionParameters, remotePeerConnectionEvents);

        localSocketHelper = new SocketHelper(localSignalingEvents, "socket_local");
        remoteSocketHelper = new SocketHelper(remoteSignalingEvents, "socket_remote");

        audioManager = RTCAudioManager.create(this);
        audioManager.start();
    }

    private void disconnect() {
        localSocketHelper.closeWebSocket();
        remoteSocketHelper.closeWebSocket();

        if (localPeerConnectionClient != null) {
            localPeerConnectionClient.close();
            localPeerConnectionClient = null;
        }
        if (remotePeerConnectionClient != null) {
            remotePeerConnectionClient.close();
            remotePeerConnectionClient = null;
        }
        if (localRenderer != null) {
            localRenderer.release();
            localRenderer = null;
        }
        if (remoteRenderer != null) {
            remoteRenderer.release();
            remoteRenderer = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private boolean isCamera2Supported() {
        return Camera2Enumerator.isSupported(this);
    }

    private VideoCapturer createVideoCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        log("Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        log("Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }
}
