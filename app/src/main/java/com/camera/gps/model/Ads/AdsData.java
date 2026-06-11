package com.camera.gps.model.Ads;

public class AdsData {
    private String adsName;
    private String adsType;
    private boolean enableAds;
    private String adFailed;
    private String publishers;
    private String adSize;

    public AdsData(String adsName, String adsType, boolean enableAds,
                   String adFailed, String publishers, String adSize) {
        this.adsName = adsName;
        this.adsType = adsType;
        this.enableAds = enableAds;
        this.adFailed = adFailed;
        this.publishers = publishers;
        this.adSize = adSize;
    }

    public String getAdsName() { return adsName; }
    public String getAdsType() { return adsType; }
    public boolean isEnableAds() { return enableAds; }
    public String getAdFailed() { return adFailed; }
    public String getPublishers() { return publishers; }
    public String getAdSize() { return adSize; }
}