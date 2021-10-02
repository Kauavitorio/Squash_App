package dev.kaua.squash.Data.System;

public class DtoSystem {
    public static final String Squash_Privacy_Policy = "Squash_Privacy_Policy";
    long versionCode, needUpdate, apiRunning, privacy_policy;
    String versionName;
    String title, link, date_time, webSite_Image, link_display;

    public DtoSystem(long versionCode, long apiRunning, long needUpdate, String versionName, long privacy_policy) {
        this.versionCode = versionCode;
        this.apiRunning = apiRunning;
        this.versionName = versionName;
        this.needUpdate = needUpdate;
        this.privacy_policy = privacy_policy;
    }

    public DtoSystem(String title, String link, String date_time, String webSite_Image){
        this.title = title;
        this.link = link;
        this.date_time = date_time;
        this.webSite_Image = webSite_Image;
    }

    public DtoSystem(String title){
        this.title = title;
    }

    public DtoSystem(){}

    public String getLink_display() {
        return link_display;
    }

    public void setLink_display(String link_display) {
        this.link_display = link_display;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getWebSite_Image() {
        return webSite_Image;
    }

    public void setWebSite_Image(String webSite_Image) {
        this.webSite_Image = webSite_Image;
    }

    public long getPrivacy_policy() {
        return privacy_policy;
    }

    public void setPrivacy_policy(long privacy_policy) {
        this.privacy_policy = privacy_policy;
    }

    public long getNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(long needUpdate) {
        this.needUpdate = needUpdate;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public long getApiRunning() {
        return apiRunning;
    }

    public void setApiRunning(long apiRunning) {
        this.apiRunning = apiRunning;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
