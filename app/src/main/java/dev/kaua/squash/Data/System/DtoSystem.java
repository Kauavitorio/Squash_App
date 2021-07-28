package dev.kaua.squash.Data.System;

public class DtoSystem {
    long versionCode, needUpdate, apiRunning;
    String versionName;

    public DtoSystem(long versionCode, long apiRunning, long needUpdate, String versionName) {
        this.versionCode = versionCode;
        this.apiRunning = apiRunning;
        this.versionName = versionName;
        this.needUpdate = needUpdate;
    }

    public DtoSystem(){}

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
