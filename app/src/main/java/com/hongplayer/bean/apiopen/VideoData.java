package com.hongplayer.bean.apiopen;

public class VideoData {
    private String dataType;
    private VideoHeader header;
    private VideoContent content;//看这里

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public VideoHeader getHeader() {
        return header;
    }

    public void setHeader(VideoHeader header) {
        this.header = header;
    }

    public VideoContent getContent() {
        return content;
    }

    public void setContent(VideoContent content) {
        this.content = content;
    }
}
