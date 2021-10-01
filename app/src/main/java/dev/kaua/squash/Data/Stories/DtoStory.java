package dev.kaua.squash.Data.Stories;

import java.util.List;

public class DtoStory {
    private String imageUrl;
    private long timeStart;
    private long timeEnd;
    private String storyId;
    private String userId;
    private String userPhoto;
    private String userName;
    private boolean seen;
    private String uploadTime;
    private String userLevel;

    public DtoStory(String imageUrl, long timeStart, long timeEnd, String storyId, String userId) {
        this.imageUrl = imageUrl;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.storyId = storyId;
        this.userId = userId;
    }

    public DtoStory(){}

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isSeen() {
        return seen;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
