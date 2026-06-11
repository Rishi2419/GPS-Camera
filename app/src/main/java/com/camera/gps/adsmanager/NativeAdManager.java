package com.camera.gps.adsmanager;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import com.camera.gps.adsmanager.admob.AdMobNativeAdHelper;
import com.camera.gps.adsmanager.applovin.AppLovinNativeAdHelper;
import com.camera.gps.adsmanager.inmobi.InMobiNativeHelper;
import com.camera.gps.adsmanager.mintegral.MintegralNativeHelper;
import com.camera.gps.adsmanager.pangle.PangleNativeAdHelper;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.util.Utils.LogUtils;

/**
 * Native Ad Manager that handles loading and displaying native ads
 * Simple approach: check publisher, try primary, try fallback, hide container if both fail
 */
public class NativeAdManager {
    
    private static final String TAG = "NativeAdManager";
    private static volatile NativeAdManager INSTANCE;
    
//    public interface AdCallback {
//        void onSuccess();
//        void onFailure(String error);
//    }

    @FunctionalInterface
    public interface AdCallback {
        void onFailure(String error);
    }


    private NativeAdManager() {}
    
    public static NativeAdManager getInstance() {
        if (INSTANCE == null) {
            synchronized (NativeAdManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NativeAdManager();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Load and display native ad with simple fallback - hide container if both fail
     * @param activity The activity context
     * @param adsName The name of the ad configuration from remote config
     * @param container The container to display the ad
     * @param isSizeBig Whether the ad should be displayed as big size
     * @param onLoaded Callback when ad is successfully loaded and displayed
     * @param onFail Callback when ad fails to load
     */
    public void loadAndShowNativeAd(Activity activity, String adsName, FrameLayout container, boolean isSizeBig, Runnable onLoaded, Runnable onFail) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            
            if (!remoteConfig.isShowAds()) {
                LogUtils.logD(TAG, "Ads globally disabled");
                container.setVisibility(View.GONE);
                return;
            }
            
            if (adsData == null) {
                LogUtils.logE(TAG, "No ads data found for: " + adsName);
                container.setVisibility(View.GONE);
                return;
            }
            
            if (!adsData.isEnableAds()) {
                LogUtils.logD(TAG, "Ads disabled for: " + adsName);
                container.setVisibility(View.GONE);
                return;
            }
            
            String primaryPublisher = adsData.getPublishers().toLowerCase();
            String fallbackPublisher = adsData.getAdFailed().toLowerCase();
            String adSize = adsData.getAdSize();
            
            LogUtils.logD(TAG, "Loading native ad for " + adsName + " with primary: " + primaryPublisher + ", fallback: " + fallbackPublisher + ", adSize: " + adSize);
            
            // Try primary publisher first
            loadNativeAdWithPublisher(activity, primaryPublisher, adSize, isSizeBig, container, onLoaded, primaryError -> {
                LogUtils.logI(TAG, "Primary failed: " + primaryError);
                
                // If primary fails and fallback is configured, try fallback
                if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
                    LogUtils.logW(TAG, "Trying fallback: " + fallbackPublisher);
                    loadNativeAdWithPublisher(activity, fallbackPublisher, adSize, isSizeBig, container, onLoaded, fallbackError -> {
                        LogUtils.logD(TAG, "Both primary and fallback failed. Hiding container.");
                        LogUtils.logI(TAG, "Primary error: " + primaryError);
                        LogUtils.logW(TAG, "Fallback error: " + fallbackError);
                        container.setVisibility(View.GONE);
                        if (onFail != null) onFail.run();
                    });
                } else {
                    LogUtils.logE(TAG, "Primary failed and no fallback configured. Hiding container.");
                    container.setVisibility(View.GONE);
                    if (onFail != null) onFail.run();
                }
            });
        } catch (Exception e) {
            LogUtils.logE(TAG, "Unexpected error in loadAndShowNativeAd: " + e.getMessage());
            container.setVisibility(View.GONE);
            if (onFail != null) onFail.run();
        }
    }
    
    /**
     * Overloaded method with default parameters
     */
    public void loadAndShowNativeAd(Activity activity, String adsName, FrameLayout container) {
        loadAndShowNativeAd(activity, adsName, container, true, null, null);
    }
    
    /**
     * Load native ad with specific publisher
     */
    private void loadNativeAdWithPublisher(Activity activity, String publisher, String adSize, boolean isSizeBig, FrameLayout container, Runnable onLoaded, AdCallback onPublisherFailed) {
        try {
            switch (publisher) {
                case "admob":
                    AdMobNativeAdHelper.loadAdmobNativeAd(activity, container, onLoaded, error -> onPublisherFailed.onFailure(error), adSize, isSizeBig);
                    break;
                case "applovin":
                    AppLovinNativeAdHelper.loadApplovinNativeAd(activity, container, adSize, isSizeBig, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "inmobi":
                    InMobiNativeHelper.loadInMobiNativeAd(activity, container, adSize, isSizeBig, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "pangle":
                    PangleNativeAdHelper.loadPangleNativeAd(activity, container, adSize, isSizeBig, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "mintegral":
                    MintegralNativeHelper.loadMintegralNativeAd(activity, container, adSize, isSizeBig, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                default:
                    LogUtils.logW(TAG, "Unknown publisher: " + publisher);
                    onPublisherFailed.onFailure("Unknown publisher: " + publisher);
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Unexpected error in loadNativeAdWithPublisher for " + publisher + ": " + e.getMessage());
            onPublisherFailed.onFailure("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Check if ads are enabled for a specific ad configuration
     * @param activity The activity context
     * @param adsName The name of the ad configuration
     * @return true if ads are enabled, false otherwise
     */
    public boolean isAdsEnabled(Activity activity, String adsName) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            if (!remoteConfig.isShowAds()) {
                return false;
            }
            
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            return adsData != null && adsData.isEnableAds();
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error checking if ads are enabled: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the primary publisher for a specific ad configuration
     * @param activity The activity context
     * @param adsName The name of the ad configuration
     * @return The primary publisher name or null if not found
     */
    public String getPrimaryPublisher(Activity activity, String adsName) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            return adsData != null ? adsData.getPublishers().toLowerCase() : null;
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error getting primary publisher: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the fallback publisher for a specific ad configuration
     * @param activity The activity context
     * @param adsName The name of the ad configuration
     * @return The fallback publisher name or null if not found
     */
    public String getFallbackPublisher(Activity activity, String adsName) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            return adsData != null ? adsData.getAdFailed().toLowerCase() : null;
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error getting fallback publisher: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the ad size for a specific ad configuration
     * @param activity The activity context
     * @param adsName The name of the ad configuration
     * @return The ad size or null if not found
     */
    public String getAdSize(Activity activity, String adsName) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            return adsData != null ? adsData.getAdSize().toLowerCase() : null;
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error getting ad size: " + e.getMessage());
            return null;
        }
    }
}
