package com.hongplayer.bean.apiopen;

public class VideoContentData {
    private String description;
    private String title;//可以用作title
    private String playUrl;//视频流有效地址
    private VideoCover cover;//里面的某个字段可以用作背景图
    private int duration;
    private int id;
    private int height;
    private int width;
    private String status;
    private String resourceType;
    private boolean addWatermark;
    private String playUrlWatermark;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public VideoCover getCover() {
        return cover;
    }

    public void setCover(VideoCover cover) {
        this.cover = cover;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isAddWatermark() {
        return addWatermark;
    }

    public void setAddWatermark(boolean addWatermark) {
        this.addWatermark = addWatermark;
    }

    public String getPlayUrlWatermark() {
        return playUrlWatermark;
    }

    public void setPlayUrlWatermark(String playUrlWatermark) {
        this.playUrlWatermark = playUrlWatermark;
    }
}
