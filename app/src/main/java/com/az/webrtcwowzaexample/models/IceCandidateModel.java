package com.az.webrtcwowzaexample.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zorin.a on 06.09.2017.
 */

public class IceCandidateModel {
    @SerializedName("candidate")
    public String candidate;
    @SerializedName("sdpMid")
    public String sdpMid;
    @SerializedName("sdpMLineIndex")
    public int sdpMLineIndex;

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(int sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }
}
