package com.az.webrtcwowzaexample.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zorin.a on 06.09.2017.
 */

public class ConnectionRequestModel {
    @SerializedName("direction")
    private String direction;
    @SerializedName("command")
    private String command;
    @SerializedName("streamInfo")
    private StreamInfo streamInfo;
    @SerializedName("sdp")
    private Sdp sdp;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public StreamInfo getStreamInfo() {
        return streamInfo;
    }

    public void setStreamInfo(StreamInfo streamInfo) {
        this.streamInfo = streamInfo;
    }

    public Sdp getSdp() {
        return sdp;
    }

    public void setSdp(Sdp sdp) {
        this.sdp = sdp;
    }
}
