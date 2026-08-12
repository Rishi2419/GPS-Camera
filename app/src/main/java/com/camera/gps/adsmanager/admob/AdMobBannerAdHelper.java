package com.camera.gps.adsmanager.admob;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.util.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdMobBannerAdHelper {
    private static final String TAG = "AdMobBannerAdHelper";

    public static void loadBannerAd(Activity activity, FrameLayout container, String adsName) {
        if (Utils.getIsPremium(activity)) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
            return;
        }
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            AdsData adsData = remoteConfig.getAdsDataByName(adsName);

            if (!remoteConfig.isShowAds() || adsData == null || !adsData.isEnableAds()) {
                container.setVisibility(View.GONE);
                return;
            }

            if (!"admob".equalsIgnoreCase(adsData.getPublishers())) {
                Utils.LogUtils.logW(TAG, "Banner supports AdMob only. Configured publisher: " + adsData.getPublishers());
                container.setVisibility(View.GONE);
                return;
            }

            String adUnitId = remoteConfig.getAdId("admob", "banner");
            if (adUnitId == null || adUnitId.isEmpty()) {
                Utils.LogUtils.logE(TAG, "AdMob banner ad unit ID not found");
                container.setVisibility(View.GONE);
                return;
            }

            container.setVisibility(View.VISIBLE);
            container.removeAllViews();

            AdView adView = new AdView(activity);
            adView.setAdUnitId(adUnitId);
            adView.setAdSize(getAnchoredAdaptiveAdSize(activity, container));
            container.addView(adView);
            adView.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
            Utils.LogUtils.logE(TAG, "Error loading banner ad: " + e.getMessage());
            container.setVisibility(View.GONE);
        }
    }

    private static AdSize getAnchoredAdaptiveAdSize(Activity activity, FrameLayout container) {
        int adWidth = container.getWidth();
        if (adWidth == 0) {
            adWidth = activity.getResources().getDisplayMetrics().widthPixels;
        }

        float density = activity.getResources().getDisplayMetrics().density;
        int adWidthDp = (int) (adWidth / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidthDp);
    }
}
