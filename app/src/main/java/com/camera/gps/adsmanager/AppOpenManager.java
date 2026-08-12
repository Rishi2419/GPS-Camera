package com.camera.gps.adsmanager;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.camera.gps.MyApplication;
import com.camera.gps.activity.Splash_Activity;
import com.camera.gps.util.Utils;

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    private final MyApplication myApplication;
    private Activity currentActivity = null;
    private boolean isShowingAd = false;
    private boolean isAppInBackground = true; // track background/foreground
    private long lastAppOpenAdClosedAtMs = 0L;
    private static final String TAG = "AppOpenManager";

    public AppOpenManager(MyApplication myApplication) {
        this.myApplication = myApplication;
        myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        Utils.LogUtils.logD(TAG, "App moved to foreground → Try showing OpenAd");
        showAdIfAvailable();
        isAppInBackground = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        isAppInBackground = true;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Utils.LogUtils.logD(TAG, "Activity started: " + activity.getLocalClassName() + "  isShowingAd " + isShowingAd);
        if (!isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Utils.LogUtils.logI(TAG, "Activity resumed: " + activity.getLocalClassName());
        currentActivity = activity;

        if (!(activity instanceof Splash_Activity) && !Utils.getIsPremium(activity)) {
            // preload for next resume
            OpenAdManager.getInstance().preloadOpenAd(activity, "app_resume_ad");
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

//    public void showAdIfAvailable() {
//        if (currentActivity == null) return;
//
////        Utils.LogUtils.logI(TAG, "isShowingAd " + isShowingAd +
////                " currentActivity " + currentActivity.getLocalClassName() +
////                " IsShowingInter " + InterstitialAdManager.AdShowingState.isShowingInterstitial);
//
////        if (isShowingAd) return;
//
//        if (currentActivity instanceof Splash_Activity) {
//            Utils.LogUtils.logE(TAG, "Skip OpenAd → Splash handles its own ad");
//            return;
//        }
//

    /// /        if (InterstitialAdManager.AdShowingState.isShowingInterstitial) {
    /// /            Utils.LogUtils.logE(TAG, "Skip OpenAd → Interstitial is already showing");
    /// /            return;
    /// /        }
//
//        isShowingAd = true;
//        Utils.LogUtils.logD(TAG, "Showing OpenAd");
//
//        OpenAdManager.getInstance().loadAndShowOpenAd(
//                currentActivity,
//                "app_resume_ad",
//                () -> {
//                    Utils.LogUtils.logD(TAG, "OpenAd closed");
//                    isShowingAd = false;
//                },
//                error -> {
//                    Utils.LogUtils.logW(TAG, "OpenAd failed: " + error);
//                    isShowingAd = false;
//                },
//                false
//        );
//    }
    public void showAdIfAvailable() {
        if (currentActivity == null) return;
        if (Utils.getIsPremium(currentActivity)) return;
        if (isShowingAd) return;
        if (currentActivity instanceof Splash_Activity) return;

        if (InterstitialAdManager.isInterstitialShowing()) {
            Utils.LogUtils.logE(TAG, "Skip OpenAd → Interstitial is already showing");
            return;
        }

        RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(currentActivity);
        int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
        if (isInAppOpenCooldown(cooldownSeconds)) {
            Utils.LogUtils.logD(TAG, "OpenAd cooldown active → skip showing");
            OpenAdManager.getInstance().preloadOpenAd(currentActivity, "app_resume_ad");
            return;
        }

        // ✅ Only show if already cached
        if (OpenAdManager.getInstance().isAdReadyForPublisher("admob")) {
            isShowingAd = true;
            OpenAdManager.getInstance().showAdWithPublisher(
                    currentActivity,
                    "admob",
                    () -> {
                        Utils.LogUtils.logD(TAG, "OpenAd closed");
                        lastAppOpenAdClosedAtMs = System.currentTimeMillis();
                        isShowingAd = false;
                        // preload next one immediately
                        OpenAdManager.getInstance().preloadOpenAd(currentActivity, "app_resume_ad");
                    },
                    error -> {
                        Utils.LogUtils.logW(TAG, "OpenAd failed: " + error);
                        isShowingAd = false;
                        OpenAdManager.getInstance().preloadOpenAd(currentActivity, "app_resume_ad");
                    },
                    err -> {
                    }
            );
        } else {
            Utils.LogUtils.logD(TAG, "OpenAd not ready → skip showing");
            OpenAdManager.getInstance().preloadOpenAd(currentActivity, "app_resume_ad");
        }
    }

    private boolean isInAppOpenCooldown(int cooldownSeconds) {
        if (cooldownSeconds <= 0 || lastAppOpenAdClosedAtMs == 0L) {
            return false;
        }

        long elapsedMs = System.currentTimeMillis() - lastAppOpenAdClosedAtMs;
        return elapsedMs < cooldownSeconds * 1000L;
    }

    // Empty required overrides
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // log if needed
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // log if needed
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // log if needed
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // log if needed
    }
}
