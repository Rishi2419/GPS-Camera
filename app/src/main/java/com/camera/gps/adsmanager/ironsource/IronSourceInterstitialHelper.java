package com.camera.gps.adsmanager.ironsource;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.util.Utils.LogUtils;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;

public class IronSourceInterstitialHelper {

    private static final String TAG = "IronSourceInterstitialHelper";
    private static boolean isInitialized = false;
    private static boolean isAdLoaded = false;
    private static String currentPlacementName = null;

    public static void loadIronSourceInterstitialAd(Activity activity, Runnable onLoaded, InterstitialAdManager.AdCallback onError) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            String placementName = remoteConfig.getAdId("ironsource", "interstitial");

            if (placementName == null || placementName.isEmpty()) {
                LogUtils.logE(TAG, "IronSource interstitial placement name not found");
                if (onError != null) onError.onFailure("Placement name not found");
                return;
            }

            currentPlacementName = placementName;

            // Initialize IronSource if not already initialized
            if (!isInitialized) {
                String appKey = getIronSourceAppKey(activity);
                if (appKey == null || appKey.isEmpty()) {
                    LogUtils.logE(TAG, "IronSource app key not found in manifest");
                    if (onError != null) onError.onFailure("App key not found");
                    return;
                }

                // Set interstitial listener before initialization
                IronSource.setInterstitialListener(new InterstitialListener() {
                    @Override
                    public void onInterstitialAdReady() {
                        isAdLoaded = true;
                        LogUtils.logI(TAG, "IronSource interstitial ad ready");
                        if (onLoaded != null) onLoaded.run();
                    }

                    @Override
                    public void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
                        isAdLoaded = false;
                        String errorMessage = ironSourceError != null ? ironSourceError.getErrorMessage() : "Unknown error";
                        LogUtils.logE(TAG, "IronSource interstitial ad failed to load: " + errorMessage);
                        if (onError != null) onError.onFailure("Load failed: " + errorMessage);
                    }

                    @Override
                    public void onInterstitialAdOpened() {
                        LogUtils.logI(TAG, "IronSource interstitial ad opened");
                    }

                    @Override
                    public void onInterstitialAdClosed() {
                        isAdLoaded = false;
                        LogUtils.logI(TAG, "IronSource interstitial ad closed");
                        IronSourceCallbackManager.onClosed();
                    }

                    @Override
                    public void onInterstitialAdShowSucceeded() {
                        LogUtils.logI(TAG, "IronSource interstitial ad show succeeded");
                    }

                    @Override
                    public void onInterstitialAdShowFailed(IronSourceError ironSourceError) {
                        isAdLoaded = false;
                        String errorMessage = ironSourceError != null ? ironSourceError.getErrorMessage() : "Unknown error";
                        LogUtils.logE(TAG, "IronSource interstitial ad failed to show: " + errorMessage);
                        IronSourceCallbackManager.onFailed();
                    }

                    @Override
                    public void onInterstitialAdClicked() {
                        LogUtils.logI(TAG, "IronSource interstitial ad clicked");
                    }
                });

                // Initialize IronSource
                IronSource.init(activity, appKey, IronSource.AD_UNIT.INTERSTITIAL);
                isInitialized = true;
                LogUtils.logI(TAG, "IronSource initialized successfully");

                // Load the interstitial ad
                loadAd();
            } else {
                // If already initialized, just load the ad
                loadAd();
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error loading IronSource interstitial ad: " + e.getMessage());
            if (onError != null) onError.onFailure("Exception: " + e.getMessage());
        }
    }

    private static String getIronSourceAppKey(Activity activity) {
        try {
            Bundle metaData = activity.getPackageManager()
                    .getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            if (metaData != null) {
                return metaData.getString("ironsource_app_key");
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Failed to get IronSource App Key from manifest: " + e.getMessage());
        }
        return null;
    }

    private static void loadAd() {
        try {
            if (isInitialized) {
                IronSource.loadInterstitial();
                LogUtils.logI(TAG, "IronSource interstitial ad loading started");
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in loadAd: " + e.getMessage());
        }
    }

    public static void showIronSourceInterstitialAd(Activity activity, Runnable onAdClosed, Runnable onAdFail) {
        try {
            if (currentPlacementName != null && isAdLoaded && IronSource.isInterstitialReady()) {
                // Store callbacks for use in listener
                IronSourceCallbackManager.setCallbacks(onAdClosed, onAdFail);

                IronSource.showInterstitial(currentPlacementName);
                LogUtils.logI(TAG, "IronSource interstitial ad show initiated for placement: " + currentPlacementName);
            } else {
                LogUtils.logE(TAG, "IronSource interstitial ad not ready - placement: " + currentPlacementName +
                        ", isLoaded: " + isAdLoaded + ", isReady: " + IronSource.isInterstitialReady());
                if (onAdFail != null) onAdFail.run();
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error showing IronSource interstitial ad: " + e.getMessage());
            if (onAdFail != null) onAdFail.run();
        }
    }

    public static boolean isAdReady() {
        return isInitialized && isAdLoaded && IronSource.isInterstitialReady() && currentPlacementName != null;
    }

    // Helper class to manage callbacks since IronSource listener is static
    private static class IronSourceCallbackManager {
        private static Runnable onAdClosed;
        private static Runnable onAdFail;

        public static void setCallbacks(Runnable closed, Runnable fail) {
            onAdClosed = closed;
            onAdFail = fail;
        }

        public static void onClosed() {
            if (onAdClosed != null) {
                onAdClosed.run();
                onAdClosed = null;
            }
        }

        public static void onFailed() {
            if (onAdFail != null) {
                onAdFail.run();
                onAdFail = null;
            }
        }

        public static void clear() {
            onAdClosed = null;
            onAdFail = null;
        }
    }


}