package com.camera.gps.adsmanager;

import android.content.Context;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.model.Ads.RemoteConfigResponse;
import com.camera.gps.util.SP;

public class RemoteConfigManager {
    
    private static final String KEY_REMOTE_JSON = "adsSharedPref";
    private static final String REMOTE_CONFIG_KEY = "ads_gps_map";
    private static volatile RemoteConfigManager INSTANCE;
    
    private final Context context;
    private final SP sp;
    private final Gson gson;
    private final FirebaseRemoteConfig remoteConfig;
    
    public interface RemoteConfigCallback {
        void onComplete(boolean success);
    }
    
    private RemoteConfigManager(Context context) {
        this.context = context.getApplicationContext();
        this.sp = new SP(this.context);
        this.gson = new Gson();
        this.remoteConfig = FirebaseRemoteConfig.getInstance();
        
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache
            .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
    }
    
    public static RemoteConfigManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RemoteConfigManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteConfigManager(context);
                }
            }
        }
        return INSTANCE;
    }
    
    public void fetchAndStore(RemoteConfigCallback callback) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String jsonString = remoteConfig.getString(REMOTE_CONFIG_KEY);
                    if (!jsonString.isEmpty()) {
                        SP.saveString(context, KEY_REMOTE_JSON, jsonString);
                        callback.onComplete(true);
                    } else {
                        callback.onComplete(false);
                    }
                } else {
                    callback.onComplete(false);
                }
            });
    }
    
    public RemoteConfigResponse getRemoteConfigData() {
        String json = SP.getString2(context, KEY_REMOTE_JSON, "");
        if (json.isEmpty()) return null;
        
        try {
            return gson.fromJson(json, RemoteConfigResponse.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String getAdId(String publisher, String adType) {
        RemoteConfigResponse data = getRemoteConfigData();
        if (data == null || data.getAdsIds() == null) return null;
        
        for (com.camera.gps.model.Ads.AdIds adIds : data.getAdsIds()) {
            if (adIds.getPublisher().equalsIgnoreCase(publisher)) {
                return adIds.getIds().get(adType);
            }
        }
        return null;
    }
    
    public AdsData getAdsDataByName(String adsName) {
        RemoteConfigResponse data = getRemoteConfigData();
        if (data == null || data.getAdsData() == null) return null;
        
        for (AdsData adsData : data.getAdsData()) {
            if (adsData.getAdsName().equals(adsName)) {
                return adsData;
            }
        }
        return null;
    }
    
    public boolean getUpdatedStatus() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null && data.isUpdatedStatus();
    }
    
    public String getNewPackage() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null ? data.getNewPackage() : "";
    }
    
    public boolean isShowAds() {
//        RemoteConfigResponse data = getRemoteConfigData();
//        return data != null && data.isShowAds();

        return true;
    }
    
    public boolean getHomeScreenAds() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null && data.isHomeScreenAds();
    }
    
    public int getUpdatedType() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null ? data.getUpdatedType() : 0;
    }
    
    public int getUserClickCounter() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null ? data.getUserClickCounter() : 0;
    }
    
    /**
     * Global interstitial cooldown seconds from Remote Config (default 45)
     */
    public int getInterstitialCooldownSeconds() {
        RemoteConfigResponse data = getRemoteConfigData();
        int cooldown = data != null ? data.getPreloadAdAfterSec() : 45;
        return Math.max(cooldown, 0);
    }
    
    public boolean isAppLive() {
        RemoteConfigResponse data = getRemoteConfigData();
        return data != null && data.isAppLiveStatus();
    }
}
