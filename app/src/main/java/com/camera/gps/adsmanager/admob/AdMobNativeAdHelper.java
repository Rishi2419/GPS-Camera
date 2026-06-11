//package com.camera.gps.adsmanager.admob;
//
//import android.app.Activity;
//import android.widget.FrameLayout;
//import com.camera.gps.adsmanager.NativeAdManager;
//import com.camera.gps.adsmanager.RemoteConfigManager;
//import com.camera.gps.util.Utils.LogUtils;
//
//public class AdMobNativeAdHelper {
//
//    private static final String TAG = "AdMobNativeAdHelper";
//
//    public static void loadAdmobNativeAd(Activity activity, FrameLayout container, Runnable onLoaded, NativeAdManager.AdCallback onFailed, String adSize, boolean isSizeBig) {
//        try {
//            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
//            String adUnitId = remoteConfig.getAdId("admob", "native");
//
//            if (adUnitId == null || adUnitId.isEmpty()) {
//                LogUtils.logE(TAG, "AdMob native ad unit ID not found");
//                if (onFailed != null) onFailed.onFailure("Ad unit ID not found");
//                return;
//            }
//
//            // TODO: Implement AdMob native ad loading
//            // This is a placeholder implementation
//            LogUtils.logI(TAG, "AdMob native ad loading - Implementation needed");
//            if (onFailed != null) onFailed.onFailure("Implementation needed");
//
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error loading AdMob native ad: " + e.getMessage());
//            if (onFailed != null) onFailed.onFailure("Exception: " + e.getMessage());
//        }
//    }
//}

package com.camera.gps.adsmanager.admob;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.camera.gps.R;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.util.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

public class AdMobNativeAdHelper {

    private static final String TAG = "AdMobNativeAdHelper";
    private static NativeAd currentNativeAd;

    public interface AdCallback {
        void onFailure(String error);
    }

    public static void loadAdmobNativeAd(Activity activity,
                                         FrameLayout container,
                                         Runnable onLoaded,
                                         AdCallback onFailed,
                                         String adSize,
                                         boolean isSizeBig) {

        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            String adUnitId = remoteConfig.getAdId("admob", "native");

            if (adUnitId == null || adUnitId.isEmpty()) {
                Utils.LogUtils.logE(TAG, "AdMob native ad unit ID not found");
                if (onFailed != null) onFailed.onFailure("Ad unit ID not found");
                return;
            }

            AdLoader adLoader = new AdLoader.Builder(activity, adUnitId)
                    .forNativeAd(nativeAd -> {
                        boolean activityDestroyed = activity.isDestroyed()
                                || activity.isFinishing()
                                || activity.isChangingConfigurations();
                        if (activityDestroyed) {
                            nativeAd.destroy();
                            return;
                        }

                        Utils.LogUtils.logD(TAG, "Native AD onAdLoaded");
                        if (currentNativeAd != null) {
                            currentNativeAd.destroy();
                        }
                        currentNativeAd = nativeAd;

                        View adBinding;
                        if ("nativeBig".equals(adSize)) {
                            adBinding = activity.getLayoutInflater().inflate(R.layout.native_big_ad_layout_admob, null, false);
                        } else {
                            adBinding = activity.getLayoutInflater().inflate(R.layout.native_small_ad_layout_admob, null, false);
                        }

                        populateNativeAdView(nativeAd, adBinding, adSize, isSizeBig);
                        container.removeAllViews();
                        container.addView(adBinding);
                        if (onLoaded != null) onLoaded.run();

                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            Utils.LogUtils.logE(TAG, "AdMob failed - Code: " + adError.getCode() +
                                    " Domain: " + adError.getDomain() +
                                    " Message: " + adError.getMessage());
                            if (onFailed != null) onFailed.onFailure(adError.getMessage());
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder().build())
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());

        } catch (Exception e) {
            Utils.LogUtils.logE(TAG, "Error loading AdMob native ad: " + e.getMessage());
            if (onFailed != null) onFailed.onFailure("Exception: " + e.getMessage());
        }
    }

    private static void populateNativeAdView(NativeAd nativeAd, View adBinding, String adSize, boolean isSizeBig) {
        NativeAdView nativeAdView;
        MediaView adMedia = null;
        TextView adHeadline;
        TextView adBody;
        AppCompatTextView adCallToAction;
        ImageView adAppIcon;

        if ("nativeBig".equals(adSize)) {
            nativeAdView = (NativeAdView) adBinding;
            adMedia = nativeAdView.findViewById(R.id.ad_media);
            adHeadline = nativeAdView.findViewById(R.id.ad_headline);
            adBody = nativeAdView.findViewById(R.id.ad_body);
            adCallToAction = nativeAdView.findViewById(R.id.ad_call_to_action);
            adAppIcon = nativeAdView.findViewById(R.id.ad_app_icon);

            nativeAdView.setMediaView(adMedia);
            if (nativeAd.getMediaContent() != null) {
                adMedia.setMediaContent(nativeAd.getMediaContent());
            }
            if (!isSizeBig && adMedia != null) {
                adMedia.setVisibility(View.GONE);
            }

        } else {
            nativeAdView = (NativeAdView) adBinding;
            adHeadline = nativeAdView.findViewById(R.id.ad_headline);
            adBody = nativeAdView.findViewById(R.id.ad_body);
            adCallToAction = nativeAdView.findViewById(R.id.ad_call_to_action);
            adAppIcon = nativeAdView.findViewById(R.id.ad_app_icon);
        }

        // Bind native ad assets
        nativeAdView.setHeadlineView(adHeadline);
        nativeAdView.setBodyView(adBody);
        nativeAdView.setCallToActionView(adCallToAction);
        nativeAdView.setIconView(adAppIcon);

        adHeadline.setText(nativeAd.getHeadline());

        if (nativeAd.getBody() == null) {
            adBody.setVisibility(View.INVISIBLE);
        } else {
            adBody.setVisibility(View.VISIBLE);
            adBody.setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adCallToAction.setVisibility(View.INVISIBLE);
        } else {
            adCallToAction.setVisibility(View.VISIBLE);
            adCallToAction.setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adAppIcon.setVisibility(View.GONE);
        } else {
            adAppIcon.setImageDrawable(nativeAd.getIcon().getDrawable());
            adAppIcon.setVisibility(View.VISIBLE);
        }

        nativeAdView.setNativeAd(nativeAd);

        if (nativeAd.getMediaContent() != null) {
            VideoController vc = nativeAd.getMediaContent().getVideoController();
            if (vc != null && nativeAd.getMediaContent().hasVideoContent()) {
                vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        super.onVideoEnd();
                    }
                });
            }
        }
    }
}

