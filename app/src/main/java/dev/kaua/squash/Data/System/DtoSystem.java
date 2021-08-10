package dev.kaua.squash.Data.System;

public class DtoSystem {
    long versionCode, needUpdate, apiRunning, privacy_policy;
    String versionName;

    public DtoSystem(long versionCode, long apiRunning, long needUpdate, String versionName, long privacy_policy) {
        this.versionCode = versionCode;
        this.apiRunning = apiRunning;
        this.versionName = versionName;
        this.needUpdate = needUpdate;
        this.privacy_policy = privacy_policy;
    }

    public DtoSystem(){}

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
