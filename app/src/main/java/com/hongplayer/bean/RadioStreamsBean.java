package com.hongplayer.bean;

import com.hongplayer.base.BaseBean;

public class RadioStreamsBean extends BaseBean {

    private String bitstreamType;
    private String resolution;
    private String url;

    public String getBitstreamType() {
        return bitstreamType;
    }

    public void setBitstreamType(String bitstreamType) {
        this.bitstreamType = bitstreamType;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
