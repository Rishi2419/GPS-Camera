package com.camera.gps.adsmanager.unity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.util.Utils.LogUtils;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;

public class UnityInterstitialHelper {

    private static final String TAG = "UnityInterstitialHelper";
    private static boolean isInitialized = false;
    private static boolean isAdLoaded = false;
    private static String currentAdUnitId = null;

    public static void loadUnityInterstitialAd(Activity activity, Runnable onLoaded, InterstitialAdManager.AdCallback onError) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            String adUnitId = remoteConfig.getAdId("unity", "interstitial");

            if (adUnitId == null || adUnitId.isEmpty()) {
                LogUtils.logE(TAG, "Unity interstitial ad unit ID not found");
                if (onError != null) onError.onFailure("Ad unit ID not found");
                return;
            }

            currentAdUnitId = adUnitId;

            // Initialize Unity Ads if not already initialized
            if (!isInitialized) {
                
                String gameId = getUnityGameId(activity);

                UnityAds.initialize(activity.getApplicationContext(), gameId, true, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        isInitialized = true;
                        LogUtils.logI(TAG, "Unity Ads initialized successfully");
                        loadAd(activity, adUnitId, onLoaded, onError);
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        isInitialized = false;
                        LogUtils.logE(TAG, "Unity Ads initialization failed: " + message);
                        if (onError != null) onError.onFailure("Initialization failed: " + message);
                    }
                });
            } else {
                loadAd(activity, adUnitId, onLoaded, onError);
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error loading Unity interstitial ad: " + e.getMessage());
            if (onError != null) onError.onFailure("Exception: " + e.getMessage());
        }
    }

    private static String getUnityGameId(Activity activity) throws PackageManager.NameNotFoundException {
        Context context = activity.getApplicationContext();
        ApplicationInfo ai = context.getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        Bundle bundle = ai.metaData;
       // String unityId = bundle.getString("unityads_game_id");
        String unityId = "5931753";
        return unityId;
    }

    private static void loadAd(Activity activity, String adUnitId, Runnable onLoaded, InterstitialAdManager.AdCallback onError) {
        try {
            UnityAds.load(adUnitId, new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    isAdLoaded = true;
                    LogUtils.logI(TAG, "Unity interstitial ad loaded successfully for placement: " + placementId);
                    if (onLoaded != null) onLoaded.run();
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    isAdLoaded = false;
                    LogUtils.logE(TAG, "Unity interstitial ad failed to load for placement " + placementId + ": " + message);
                    if (onError != null) onError.onFailure("Load failed: " + message);
                }
            });
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in loadAd: " + e.getMessage());
            if (onError != null) onError.onFailure("Load exception: " + e.getMessage());
        }
    }

    public static void showUnityInterstitialAd(Activity activity, Runnable onAdClosed, Runnable onAdFail) {
        try {
            if (currentAdUnitId != null && isAdLoaded) {
                UnityAdsShowOptions showOptions = new UnityAdsShowOptions();

                UnityAds.show(activity, currentAdUnitId, showOptions, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowStart(String placementId) {
                        LogUtils.logI(TAG, "Unity interstitial ad show started for placement: " + placementId);
                    }

                    @Override
                    public void onUnityAdsShowClick(String placementId) {
                        LogUtils.logI(TAG, "Unity interstitial ad clicked for placement: " + placementId);
                    }

                    @Override
                    public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                        isAdLoaded = false;
                        LogUtils.logI(TAG, "Unity interstitial ad completed for placement: " + placementId + " with state: " + state);
                        if (onAdClosed != null) onAdClosed.run();
                    }

                    @Override
                    public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                        isAdLoaded = false;
                        LogUtils.logE(TAG, "Unity interstitial ad failed to show for placement " + placementId + ": " + message);
                        if (onAdFail != null) onAdFail.run();
                    }
                });
            } else {
                LogUtils.logE(TAG, "Unity interstitial ad not ready - adUnitId: " + currentAdUnitId + ", isLoaded: " + isAdLoaded);
                if (onAdFail != null) onAdFail.run();
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error showing Unity interstitial ad: " + e.getMessage());
            if (onAdFail != null) onAdFail.run();
        }
    }

    public static boolean isAdReady() {
        return isInitialized && isAdLoaded && currentAdUnitId != null;
    }
}