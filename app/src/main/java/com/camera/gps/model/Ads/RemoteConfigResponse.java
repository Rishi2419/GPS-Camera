package com.camera.gps.model.Ads;


import java.util.List;

public class RemoteConfigResponse {
    private boolean showAds;
    private boolean updatedStatus;
    private boolean appLiveStatus;
    private String newPackage;
    private int preloadAdAfterSec;
    private int versionCode;
    private boolean homeScreenAds;
    private int updatedType;
    private int userClickCounter;
    private List<AdIds> adsIds;
    private List<AdsData> adsData;
    private List<String> preload;

    public RemoteConfigResponse(boolean showAds, boolean updatedStatus, boolean appLiveStatus,
                                String newPackage, int preloadAdAfterSec, int versionCode,
                                boolean homeScreenAds, int updatedType, int userClickCounter,
                                List<AdIds> adsIds, List<AdsData> adsData, List<String> preload) {
        this.showAds = showAds;
        this.updatedStatus = updatedStatus;
        this.appLiveStatus = appLiveStatus;
        this.newPackage = newPackage;
        this.preloadAdAfterSec = preloadAdAfterSec;
        this.versionCode = versionCode;
        this.homeScreenAds = homeScreenAds;
        this.updatedType = updatedType;
        this.userClickCounter = userClickCounter;
        this.adsIds = adsIds;
        this.adsData = adsData;
        this.preload = preload;
    }

    public boolean isShowAds() { return showAds; }
    public boolean isUpdatedStatus() { return updatedStatus; }
    public boolean isAppLiveStatus() { return appLiveStatus; }
    public String getNewPackage() { return newPackage; }
    public int getPreloadAdAfterSec() { return preloadAdAfterSec; }
    public int getVersionCode() { return versionCode; }
    public boolean isHomeScreenAds() { return homeScreenAds; }
    public int getUpdatedType() { return updatedType; }
    public int getUserClickCounter() { return userClickCounter; }
    public List<AdIds> getAdsIds() { return adsIds; }
    public List<AdsData> getAdsData() { return adsData; }
    public List<String> getPreload() { return preload; }
}