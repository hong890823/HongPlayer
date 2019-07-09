package com.hongplayer.bean.idataapi;

import java.util.List;

public class BiliLiveData {
    private boolean isLive;
    private int idGrade;
    private long income;
    private List<String> videoUrls;
    private String userId;
    private List<String> tags;
    private int onlineCount;
    private int publishDate;
    private String userScreenName;
    private String catId1;
    private String id;
    private String avatarUrl;
    private int fansCount;
    private String catPathKey;
    private String url;
    private String publishDateStr;
    private List<BiliLiveDonor> vipDonors;
    private String catName1;
    private String[] imageUrls;
    private String coverUrl;
    private String description;
    private List<BiliLiveDonor> weekDonors;
    private int followCount;
    private String subtitle;
    private String title;
    private List<BiliLiveDonor> fansDonors;
    private List<BiliLiveDonor> donors;

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public int getIdGrade() {
        return idGrade;
    }

    public void setIdGrade(int idGrade) {
        this.idGrade = idGrade;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public int getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(int publishDate) {
        this.publishDate = publishDate;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public void setUserScreenName(String userScreenName) {
        this.userScreenName = userScreenName;
    }

    public String getCatId1() {
        return catId1;
    }

    public void setCatId1(String catId1) {
        this.catId1 = catId1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getFansCount() {
        return fansCount;
    }

    public void setFansCount(int fansCount) {
        this.fansCount = fansCount;
    }

    public String getCatPathKey() {
        return catPathKey;
    }

    public void setCatPathKey(String catPathKey) {
        this.catPathKey = catPathKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<BiliLiveDonor> getVipDonors() {
        return vipDonors;
    }

    public void setVipDonors(List<BiliLiveDonor> vipDonors) {
        this.vipDonors = vipDonors;
    }

    public String getCatName1() {
        return catName1;
    }

    public void setCatName1(String catName1) {
        this.catName1 = catName1;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BiliLiveDonor> getWeekDonors() {
        return weekDonors;
    }

    public void setWeekDonors(List<BiliLiveDonor> weekDonors) {
        this.weekDonors = weekDonors;
    }

    public int getFollowCount() {
        return followCount;
    }

    public void setFollowCount(int followCount) {
        this.followCount = followCount;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<BiliLiveDonor> getFansDonors() {
        return fansDonors;
    }

    public void setFansDonors(List<BiliLiveDonor> fansDonors) {
        this.fansDonors = fansDonors;
    }

    public List<BiliLiveDonor> getDonors() {
        return donors;
    }

    public void setDonors(List<BiliLiveDonor> donors) {
        this.donors = donors;
    }

    public String getPublishDateStr() {
        return publishDateStr;
    }

    public void setPublishDateStr(String publishDateStr) {
        this.publishDateStr = publishDateStr;
    }
}
