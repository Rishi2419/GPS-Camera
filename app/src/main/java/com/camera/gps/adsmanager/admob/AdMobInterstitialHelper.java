package com.camera.gps.adsmanager.admob;

import android.app.Activity;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.util.Utils.LogUtils;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdMobInterstitialHelper {
    
    private static final String TAG = "AdMobInterstitialHelper";
    private static InterstitialAd mInterstitialAd;

    public static void loadAdMobInterstitialAd(Activity activity, Runnable onLoaded, InterstitialAdManager.AdCallback onError) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            String adUnitId = remoteConfig.getAdId("admob", "interstitial");

            if (adUnitId == null || adUnitId.isEmpty()) {
                LogUtils.logE(TAG, "AdMob interstitial ad unit ID not found");
                if (onError != null) onError.onFailure("Ad unit ID not found");
                return;
            }

            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(activity, adUnitId, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd interstitialAd) {
                    mInterstitialAd = interstitialAd;
                    LogUtils.logI(TAG, "AdMob interstitial ad loaded successfully");
                    if (onLoaded != null) onLoaded.run();
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    mInterstitialAd = null;
                    LogUtils.logE(TAG, "AdMob interstitial ad failed to load: " + loadAdError.getMessage());
                    if (onError != null) onError.onFailure(loadAdError.getMessage());
                }
            });
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error loading AdMob interstitial ad: " + e.getMessage());
            if (onError != null) onError.onFailure("Exception: " + e.getMessage());
        }
    }

    public static void showAdMobInterstitialAd(Activity activity, Runnable onAdClosed, Runnable onAdFail) {
        try {
            if (mInterstitialAd != null) {
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mInterstitialAd = null;
                        LogUtils.logI(TAG, "AdMob interstitial ad dismissed");
                        if (onAdClosed != null) onAdClosed.run();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        mInterstitialAd = null;
                        LogUtils.logE(TAG, "AdMob interstitial ad failed to show: " + adError.getMessage());
                        if (onAdFail != null) onAdFail.run();
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        LogUtils.logI(TAG, "AdMob interstitial ad showed");
                    }
                });

                mInterstitialAd.show(activity);
            } else {
                LogUtils.logE(TAG, "AdMob interstitial ad not ready");
                if (onAdFail != null) onAdFail.run();
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error showing AdMob interstitial ad: " + e.getMessage());
            if (onAdFail != null) onAdFail.run();
        }
    }

    public static boolean isAdReady() {
        return mInterstitialAd != null;
    }
}
