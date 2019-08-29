package com.hongplayer.bean.apiopen;

public class VideoContent {
    private VideoContentData data;//看这里
    private int adIndex;
    private String tag;
    private int id;
    private String type;

    public VideoContentData getData() {
        return data;
    }

    public void setData(VideoContentData data) {
        this.data = data;
    }

    public int getAdIndex() {
        return adIndex;
    }

    public void setAdIndex(int adIndex) {
        this.adIndex = adIndex;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
