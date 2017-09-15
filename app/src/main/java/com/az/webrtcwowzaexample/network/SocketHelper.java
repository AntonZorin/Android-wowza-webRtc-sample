package com.az.webrtcwowzaexample.network;

import android.os.StrictMode;
import android.util.Log;

import com.az.webrtcwowzaexample.common.Constants;
import com.az.webrtcwowzaexample.models.IceCandidateModel;
import com.az.webrtcwowzaexample.models.OfferResponseModel;
import com.az.webrtcwowzaexample.models.Sdp;
import com.az.webrtcwowzaexample.models.ConnectionRequestModel;
import com.az.webrtcwowzaexample.models.StreamInfo;
import com.google.gson.Gson;

import org.webrtc.SessionDescription;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by zorin.a on 12.09.2017.
 */

public class SocketHelper extends WebSocketListener {
    private String TAG = "APP_RTC_Socket";
    private static final int STATUS_OK = 200;

    //region var
    WebSocket ws;
    SignalingEvents events;
    Gson gson;
    private OfferResponseModel offerResponse;
    private OkHttpClient okHttpClient;
    private boolean isConnected;
    //endregion

    public SocketHelper(SignalingEvents events, String tag) {
        TAG += "_" + tag;
        this.events = events;
        gson = new Gson();
        prepareWebSocket();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        output("onOpen:");
        events.onConnected();
        isConnected = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String message) {
        output("onMessage: " + message);

        OfferResponseModel tempResponse = gson.fromJson(message, OfferResponseModel.class);

        if (isCheckStatusOk(tempResponse) && isGetOfferCommand(tempResponse)) {
            output("direction: " + tempResponse.getDirection() + " command: " + tempResponse.getCommand());
            offerResponse = tempResponse;
            SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER,
                    offerResponse.getSdp().getSdp());
            events.onRemoteDescription(sessionDescription);
        }

        if (isCheckStatusOk(tempResponse) && isSendResponseCommand(tempResponse)) {
            output("direction: " + tempResponse.getDirection() + " command: " + tempResponse.getCommand());
            List<IceCandidateModel> iceCandidates = tempResponse.getIceCandidates();
            events.onIceCandidatesAdded(iceCandidates);
            events.onConnectionEstablished();
        }

        if (isCheckStatusOk(tempResponse) && isSendOfferCommand(tempResponse)) {
            output("direction: " + tempResponse.getDirection() + " command: " + tempResponse.getCommand());
            Sdp spd = tempResponse.getSdp();
            String spdDescription = spd.getSdp();
            SessionDescription sdpAnswer = new SessionDescription(
                    SessionDescription.Type.ANSWER, spdDescription);
            List<IceCandidateModel> iceCandidates = tempResponse.getIceCandidates();
            events.onIceCandidatesAdded(iceCandidates);
            events.onRemoteDescription(sdpAnswer);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        output("Receiving bytes: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        output("onClosing: " + code + " / " + reason);
        isConnected = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        output("onFailure: " + t.getMessage());
        isConnected = false;
    }

    public void closeWebSocket() {
        Log.d(TAG, "closeWebSocket");
        if (isConnected) {
            ws.close(1000, "normal close");
        }
//        okHttpClient.dispatcher().executorService().shutdown();
    }

    public void sendMessage(final String message) {
        if (ws != null) {
            ws.send(message);
        }
    }

    public void sendOffer(String streamName) {
        ConnectionRequestModel offerModel = new ConnectionRequestModel();
        offerModel.setDirection("play");
        offerModel.setCommand("getOffer");
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setApplicationName("webrtc");
        output("sendOffer stream name: " + streamName);
        streamInfo.setStreamName(streamName);
        streamInfo.setSessionId("[empty]");
        offerModel.setStreamInfo(streamInfo);
        String json = gson.toJson(offerModel);
        output("send json getOffer: " + json);
        ws.send(json);
    }

    public void sendPlayResponse(SessionDescription sdp) {
        ConnectionRequestModel offerModel = new ConnectionRequestModel();
        offerModel.setDirection("play");
        offerModel.setCommand("sendResponse");
        offerModel.setStreamInfo(offerResponse.getStreamInfo());
        offerModel.getStreamInfo().setApplicationName("webrtc");
        offerModel.setSdp(offerResponse.getSdp());
        String json = gson.toJson(offerModel);
        output("send send PlayResponse: " + json);
        ws.send(json);
    }

    public void sendOfferSdp(String streamName, final SessionDescription localSdp) {
        ConnectionRequestModel publishModel = new ConnectionRequestModel();
        publishModel.setDirection("publish");
        publishModel.setCommand("sendOffer");
        Sdp sdp = new Sdp();
        sdp.setType("offer");
        sdp.setSdp(localSdp.description);
        publishModel.setSdp(sdp);
        StreamInfo streamInfo = new StreamInfo();
        streamInfo.setApplicationName("webrtc");
        output("sendOffer stream name: " + streamName);
        streamInfo.setStreamName(streamName);
        streamInfo.setSessionId("[empty]");
        publishModel.setStreamInfo(streamInfo);
        String json = gson.toJson(publishModel);
        output("send sendOfferSdp: " + json);
        ws.send(json);
    }

    public boolean isActive() {
        return ws != null;
    }

    private void output(String s) {
        Log.d(TAG, s);
    }

    private boolean isGetOfferCommand(OfferResponseModel offerResponse) {
        return offerResponse.getCommand().equals("getOffer") && offerResponse.getDirection().equals("play");
    }

    private boolean isSendResponseCommand(OfferResponseModel tempResponse) {
        return tempResponse.getCommand().equals("sendResponse");
    }

    private boolean isSendOfferCommand(OfferResponseModel response) {
        return response.getCommand().equals("sendOffer") && response.getDirection().equals("publish");

    }

    private boolean isCheckStatusOk(OfferResponseModel offerResponse) {
        return offerResponse.getStatus() == STATUS_OK;
    }

    private void prepareWebSocket() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(Constants.WOWZA_SOCKET_URL).build();
        ws = okHttpClient.newWebSocket(request, this);
    }

    public interface SignalingEvents {
        void onRemoteDescription(final SessionDescription sdp);

        void onConnected();

        void onConnectionEstablished();

        void onIceCandidatesAdded(List<IceCandidateModel> iceCandidates);
    }
}
