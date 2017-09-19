package com.az.webrtcwowzaexample.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.az.webrtcwowzaexample.streaming.ProxyRenderer;

import org.webrtc.Camera1Enumerator;
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

    // List of mandatory application permissions.
    private static final String[] MANDATORY_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    PeerConnectionClient localPeerConnectionClient;
    PeerConnectionClient remotePeerConnectionClient;
    PeerConnectionParameters peerConnectionParameters;
    SocketHelper localSocketHelper;
    SocketHelper remoteSocketHelper;
    EglBase eglBase;
    RTCAudioManager audioManager;

    EditText remoteStreamNameEditText;
    EditText localStreamNameEditText;
    private VideoCapturer videoCapturer;


    ProxyRenderer localProxyRender = new ProxyRenderer("local");
    ProxyRenderer remoteProxyRender = new ProxyRenderer("remote");

    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;

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
                        if (peerConnectionParameters.videoMaxBitrate > 0) {
                            log("localPeerConnectionClient.setVideoMaxBitrate: " + peerConnectionParameters.videoMaxBitrate);
                            localPeerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                        }
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

                Logging.d(TAG, "Creating capturer using camera1 API.");
                videoCapturer = createVideoCapturer(new Camera1Enumerator(false));

            if (videoCapturer == null) {
                log("Failed to open camera");
            }
            localPeerConnectionClient.createPeerConnection(eglBase.getEglBaseContext(), localProxyRender,
                    remoteProxyRender, videoCapturer);

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
            remotePeerConnectionClient.createPeerConnection(eglBase.getEglBaseContext(), localProxyRender,
                    remoteProxyRender, null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_main);
        requestPermissions();
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
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        eglBase.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isAllPermissionsGranted = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                toast("Permission " + permissions[i] + " not allowed!", true);
                isAllPermissionsGranted = false;
            } else {
                toast("Permission " + permissions[i] + " granted!", false);
            }
        }
        if (isAllPermissionsGranted) {
            initVars();
        }
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

        localProxyRender.setTarget(localRenderer);
        remoteProxyRender.setTarget(remoteRenderer);

        findViewById(R.id.connect_local).setOnClickListener(this);
        findViewById(R.id.connect_remote).setOnClickListener(this);

        localRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (localPeerConnectionClient != null) {
                    localPeerConnectionClient.switchCamera();
                }
            }
        });

        log("initViews done");
    }

    private void initVars() {
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));
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

        localProxyRender.setTarget(null);
        remoteProxyRender.setTarget(null);

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

    private void toast(String s, boolean isLongLength) {
        Toast.makeText(this, s, isLongLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    private void requestPermissions() {
        boolean isAllPermissionsGranted = true;
        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                isAllPermissionsGranted = false;
                log("Permission " + permission + " is not granted");
                ActivityCompat.requestPermissions(this, MANDATORY_PERMISSIONS, 1);
            }
        }
        if (isAllPermissionsGranted) {
            initVars();
        }
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
