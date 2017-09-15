package com.az.webrtcwowzaexample.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zorin.a on 06.09.2017.
 */

public class OfferResponseModel {
    @SerializedName("status")
    public int status;
    @SerializedName("statusDescription")
    public String statusDescription;
    @SerializedName("direction")
    public String direction;
    @SerializedName("command")
    public String command;
    @SerializedName("streamInfo")
    public StreamInfo streamInfo;
    @SerializedName("sdp")
    public Sdp sdp;
    @SerializedName("iceCandidates")
    private List<IceCandidateModel> iceCandidates;

    public List<IceCandidateModel> getIceCandidates() {
        return iceCandidates;
    }

    public void setIceCandidates(List<IceCandidateModel> iceCandidates) {
        this.iceCandidates = iceCandidates;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

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
