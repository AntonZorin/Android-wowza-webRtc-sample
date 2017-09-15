package com.az.webrtcwowzaexample.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zorin.a on 06.09.2017.
 */

public class StreamInfo {
    @SerializedName("applicationName")
    private String applicationName;
    @SerializedName("streamName")
    private String streamName;
    @SerializedName("sessionId")
    private String sessionId;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
