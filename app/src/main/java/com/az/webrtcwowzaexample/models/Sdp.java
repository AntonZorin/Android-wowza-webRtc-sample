package com.az.webrtcwowzaexample.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zorin.a on 06.09.2017.
 */

public class Sdp {
    @SerializedName("type")
    public String type;
    @SerializedName("sdp")
    public String sdp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }
}
