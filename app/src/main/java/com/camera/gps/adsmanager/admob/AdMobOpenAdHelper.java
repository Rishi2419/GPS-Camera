//package com.camera.gps.adsmanager.admob;
//
//import android.app.Activity;
//import com.camera.gps.adsmanager.OpenAdManager;
//import com.camera.gps.adsmanager.RemoteConfigManager;
//import com.camera.gps.util.Utils.LogUtils;
//
//public class AdMobOpenAdHelper {
//
//    private static final String TAG = "AdMobOpenAdHelper";
//
//    public static void loadAdmobOpenAd(Activity activity, Runnable onLoaded, OpenAdManager.AdCallback onFailed) {
//        try {
//            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
//            String adUnitId = remoteConfig.getAdId("admob", "open_ad");
//
//            if (adUnitId == null || adUnitId.isEmpty()) {
//                LogUtils.logE(TAG, "AdMob open ad unit ID not found");
//                if (onFailed != null) onFailed.onFailure("Ad unit ID not found");
//                return;
//            }
//
//            // TODO: Implement AdMob open ad loading
//            // This is a placeholder implementation
//            LogUtils.logI(TAG, "AdMob open ad loading - Implementation needed");
//            if (onFailed != null) onFailed.onFailure("Implementation needed");
//
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error loading AdMob open ad: " + e.getMessage());
//            if (onFailed != null) onFailed.onFailure("Exception: " + e.getMessage());
//        }
//    }
//
//    public static void showAdmobOpenAdIfAvailable(Activity activity, Runnable onAdDismissed, OpenAdManager.AdCallback onAdFailed) {
//        try {
//            // TODO: Implement AdMob open ad showing
//            // This is a placeholder implementation
//            LogUtils.logI(TAG, "AdMob open ad showing - Implementation needed");
//            if (onAdFailed != null) onAdFailed.onFailure("Implementation needed");
//
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error showing AdMob open ad: " + e.getMessage());
//            if (onAdFailed != null) onAdFailed.onFailure("Exception: " + e.getMessage());
//        }
//    }
//
//    public static boolean isAdReady() {
//        // TODO: Implement AdMob open ad ready check
//        return false;
//    }
//}

package com.camera.gps.adsmanager.admob;

import android.app.Activity;

import com.camera.gps.adsmanager.OpenAdManager;
import com.camera.gps.util.Utils.LogUtils;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

public class AdMobOpenAdHelper {

    private static final String TAG = "AdMobOpenAdHelper";

    private static AppOpenAd appOpenAd = null;
    private static boolean isLoading = false;

    /**
     * Load AdMob Open Ad
     */
    public static void loadAdmobOpenAd(Activity activity, Runnable onLoaded, OpenAdManager.AdCallback onFailed) {
        try {
            if (isLoading) {
                LogUtils.logI(TAG, "AdMob open ad already loading, skipping");
                return;
            }

            if (appOpenAd != null) {
                LogUtils.logI(TAG, "AdMob open ad already loaded");
                if (onLoaded != null) onLoaded.run();
                return;
            }

            isLoading = true;

            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            String adUnitId = remoteConfig.getAdId("admob", "open_ad");

            if (adUnitId == null || adUnitId.isEmpty()) {
                // Default Test AdMob OpenAd ID

                LogUtils.logE(TAG, "AdMob open ad unit ID not found");
                if (onFailed != null) onFailed.onFailure("Ad unit ID not found");
                return;
                //adUnitId = "ca-app-pub-3940256099942544/3419835294";
            }

            LogUtils.logI(TAG, "Loading AdMob open ad with unit ID: " + adUnitId);

            AdRequest request = new AdRequest.Builder().build();

            AppOpenAd.load(
                    activity,
                    adUnitId,
                    request,
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(AppOpenAd ad) {
                            LogUtils.logI(TAG, "AdMob open ad loaded successfully");
                            appOpenAd = ad;
                            isLoading = false;
                            if (onLoaded != null) onLoaded.run();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError loadAdError) {
                            LogUtils.logE(TAG, "AdMob open ad failed to load: " + loadAdError.getMessage());
                            appOpenAd = null;
                            isLoading = false;
                            if (onFailed != null) onFailed.onFailure("AdMob open ad failed to load: " + loadAdError.getMessage());
                        }
                    }
            );

        } catch (Exception e) {
            LogUtils.logE(TAG, "Error loading AdMob open ad: " + e.getMessage());
            isLoading = false;
            if (onFailed != null) onFailed.onFailure("Exception: " + e.getMessage());
        }
    }

    /**
     * Show AdMob Open Ad if available
     */
    public static void showAdmobOpenAdIfAvailable(Activity activity, Runnable onAdDismissed, OpenAdManager.AdCallback onAdFailed) {
        try {
            if (appOpenAd == null) {
                LogUtils.logE(TAG, "AdMob open ad not available");
                if (onAdFailed != null) onAdFailed.onFailure("AdMob open ad not available");
                return;
            }

            LogUtils.logI(TAG, "Showing AdMob open ad");

            appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    LogUtils.logI(TAG, "AdMob open ad dismissed");
                    appOpenAd = null;
                    if (onAdDismissed != null) onAdDismissed.run();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    LogUtils.logE(TAG, "AdMob open ad failed to show: " + adError.getMessage());
                    appOpenAd = null;
                    if (onAdFailed != null) onAdFailed.onFailure("AdMob open ad failed to show: " + adError.getMessage());
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    LogUtils.logI(TAG, "AdMob open ad showed full screen content");
                }
            });

            appOpenAd.show(activity);

        } catch (Exception e) {
            LogUtils.logE(TAG, "Error showing AdMob open ad: " + e.getMessage());
            if (onAdFailed != null) onAdFailed.onFailure("Exception: " + e.getMessage());
        }
    }

    /**
     * Check if AdMob Open Ad is ready
     */
    public static boolean isAdReady() {
        return appOpenAd != null && !isLoading;
    }

    /**
     * Clear the loaded ad
     */
    public static void clearAd() {
        appOpenAd = null;
        isLoading = false;
    }
}

