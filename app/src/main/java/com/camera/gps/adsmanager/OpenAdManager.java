package com.camera.gps.adsmanager;

import android.app.Activity;

import com.camera.gps.adsmanager.admob.AdMobOpenAdHelper;
import com.camera.gps.adsmanager.applovin.AppLovinOpenAdHelper;
import com.camera.gps.adsmanager.pangle.PangleOpenAdHelper;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.model.Ads.RemoteConfigResponse;
import com.camera.gps.util.Utils.LogUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OpenAdManager {

    private static final String TAG = "OpenAdManager";
    private static volatile OpenAdManager INSTANCE;

    private final Set<String> loadingPublishers;

//    public interface AdCallback {
//        void onSuccess();
//        void onFailure(String error);
//    }

    @FunctionalInterface
    public interface AdCallback {
        void onFailure(String error);
    }


    private OpenAdManager() {
        loadingPublishers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public static OpenAdManager getInstance() {
        if (INSTANCE == null) {
            synchronized (OpenAdManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OpenAdManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Load and show open ad with fallback mechanism
     */
    public void loadAndShowOpenAd(Activity activity, String adsName, Runnable onAdClosed) {
        loadAndShowOpenAd(activity, adsName, onAdClosed, null, true);
    }

    public void loadAndShowOpenAd(Activity activity, String adsName, Runnable onAdClosed, AdCallback onAdFailed, boolean proceedWithoutAdOnFailure) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);

            if (!remoteConfig.isShowAds()) {
                LogUtils.logD(TAG, "Ads globally disabled");
                if (onAdClosed != null) onAdClosed.run();
                return;
            }

            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            if (adsData == null) {
                LogUtils.logE(TAG, "No ads data found for: " + adsName);
                if (onAdFailed != null) onAdFailed.onFailure("No ads data found");
                return;
            }

            if (!adsData.isEnableAds()) {
                LogUtils.logD(TAG, "OpenAd disabled for: " + adsName);
                if (onAdClosed != null) onAdClosed.run();
                return;
            }

            String primaryPublisher = adsData.getPublishers().toLowerCase();
            String fallbackPublisher = adsData.getAdFailed().toLowerCase();

            LogUtils.logD(TAG, "loadAndShowOpenAd " + adsName + " → primary=" + primaryPublisher + " fallback=" + fallbackPublisher);

            // Show immediately if primary ready
            if (isAdReadyForPublisher(primaryPublisher)) {
                LogUtils.logI(TAG, "Primary OpenAd ready: " + primaryPublisher);
                showAdWithPublisher(activity, primaryPublisher, onAdClosed, onAdFailed, error -> {
                    tryFallback(activity, adsName, fallbackPublisher, primaryPublisher, error, onAdClosed, onAdFailed, proceedWithoutAdOnFailure);
                });
                return;
            }

            // Show immediately if fallback ready
            if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher) && isAdReadyForPublisher(fallbackPublisher)) {
                LogUtils.logI(TAG, "Fallback OpenAd ready: " + fallbackPublisher);
                showAdWithPublisher(activity, fallbackPublisher, onAdClosed, onAdFailed, fbError -> {
                    LogUtils.logW(TAG, "Fallback show failed: " + fbError + ", trying primary load");
                    loadAdWithPublisher(activity, primaryPublisher,
                            () -> {
                                showAdWithPublisher(activity, primaryPublisher, onAdClosed, onAdFailed, pError -> {
                                    failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, "Fallback failed: " + fbError + ", Primary failed: " + pError);
                                });
                            },
                            loadError -> {
                                failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, "Fallback failed: " + fbError + ", Primary load failed: " + loadError);
                            }
                    );
                });
                return;
            }

            // Load primary and then show
            loadAdWithPublisher(activity, primaryPublisher,
                    () -> {
                        LogUtils.logI(TAG, "Primary OpenAd loaded successfully");
                        showAdWithPublisher(activity, primaryPublisher, onAdClosed, onAdFailed, error -> {
                            tryFallback(activity, adsName, fallbackPublisher, primaryPublisher, error, onAdClosed, onAdFailed, proceedWithoutAdOnFailure);
                        });
                    },
                    error -> {
                        LogUtils.logW(TAG, "Primary load failed: " + error);
                        if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
                            loadAdWithPublisher(activity, fallbackPublisher,
                                    () -> {
                                        LogUtils.logI(TAG, "Fallback OpenAd loaded successfully");
                                        showAdWithPublisher(activity, fallbackPublisher, onAdClosed, onAdFailed, fbError -> {
                                            failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, "Primary failed: " + error + ", Fallback failed: " + fbError);
                                        });
                                    },
                                    fbError -> {
                                        failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, "Primary failed: " + error + ", Fallback load failed: " + fbError);
                                    }
                            );
                        } else {
                            failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, error);
                        }
                    }
            );
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in loadAndShowOpenAd: " + e.getMessage());
            if (onAdFailed != null) onAdFailed.onFailure("System error: " + e.getMessage());
        }
    }

    private void loadAdWithPublisher(Activity activity, String publisher, Runnable onLoaded, AdCallback onPublisherFailed) {
        try {
            switch (publisher) {
                case "admob":
                    AdMobOpenAdHelper.loadAdmobOpenAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "applovin":
                    AppLovinOpenAdHelper.loadAppLovinOpenAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "pangle":
                    PangleOpenAdHelper.loadPangleOpenAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                default:
                    onPublisherFailed.onFailure("Unknown publisher: " + publisher);
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "loadAdWithPublisher error: " + e.getMessage());
            onPublisherFailed.onFailure("Unexpected error: " + e.getMessage());
        }
    }

    public void showAdWithPublisher(Activity activity, String publisher, Runnable onAdClosed, AdCallback onAdFailed, AdCallback onPublisherFailed) {
        try {
            switch (publisher) {
                case "admob":
                    AdMobOpenAdHelper.showAdmobOpenAdIfAvailable(activity,
                            () -> {
                                if (onAdClosed != null) onAdClosed.run();
                            },
                            error -> onPublisherFailed.onFailure(error)
                    );
                    break;
                case "applovin":
                    AppLovinOpenAdHelper.showAppLovinOpenAdIfAvailable(activity,
                            () -> {
                                if (onAdClosed != null) onAdClosed.run();
                            },
                            error -> onPublisherFailed.onFailure(error)
                    );
                    break;
                case "pangle":
                    PangleOpenAdHelper.showPangleOpenAdIfAvailable(activity,
                            () -> {
                                if (onAdClosed != null) onAdClosed.run();
                            },
                            error -> onPublisherFailed.onFailure(error)
                    );
                    break;
                default:
                    onPublisherFailed.onFailure("Unknown publisher: " + publisher);
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "showAdWithPublisher error: " + e.getMessage());
            onPublisherFailed.onFailure("Unexpected error: " + e.getMessage());
        }
    }

    boolean isAdReadyForPublisher(String publisher) {
        try {
            switch (publisher) {
                case "admob":
                    return AdMobOpenAdHelper.isAdReady();
                case "applovin":
                    return AppLovinOpenAdHelper.isAdReady();
                case "pangle":
                    return PangleOpenAdHelper.isAdReady();
                default:
                    return false;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error checking ad ready for " + publisher + ": " + e.getMessage());
            return false;
        }
    }

    private void tryFallback(Activity activity, String adsName, String fallbackPublisher, String primaryPublisher, String error, Runnable onAdClosed, AdCallback onAdFailed, boolean proceedWithoutAdOnFailure) {
        if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
            LogUtils.logW(TAG, "Primary show failed, trying fallback: " + fallbackPublisher);
            showAdWithPublisher(activity, fallbackPublisher, onAdClosed, onAdFailed, fbError -> {
                failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, "Primary failed: " + error + ", Fallback failed: " + fbError);
            });
        } else {
            failOrProceed(proceedWithoutAdOnFailure, onAdClosed, onAdFailed, error);
        }
    }

    private void failOrProceed(boolean proceedWithoutAdOnFailure, Runnable onAdClosed, AdCallback onAdFailed, String error) {
        if (proceedWithoutAdOnFailure) {
            LogUtils.logW(TAG, "Proceeding without ad: " + error);
            if (onAdClosed != null) onAdClosed.run();
        } else {
            LogUtils.logE(TAG, "Ad failed and not proceeding: " + error);
            if (onAdFailed != null) onAdFailed.onFailure(error);
        }
    }

    public void preloadOpenAd(Activity activity, String adsName) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);

            if (!remoteConfig.isShowAds()) {
                LogUtils.logD(TAG, "Ads globally disabled - skip preload for: " + adsName);
                return;
            }

            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
            if (adsData == null) {
                LogUtils.logE(TAG, "No ads data found for preload: " + adsName);
                return;
            }

            if (!adsData.isEnableAds()) {
                LogUtils.logD(TAG, "OpenAd disabled for preload: " + adsName);
                return;
            }

            String primaryPublisher = adsData.getPublishers().toLowerCase();
            String fallbackPublisher = adsData.getAdFailed().toLowerCase();

            LogUtils.logD(TAG, "Preloading OpenAd: " + adsName + " → primary=" + primaryPublisher + " fallback=" + fallbackPublisher);

            // Preload primary if not ready
            if (!isAdReadyForPublisher(primaryPublisher) && !loadingPublishers.contains(primaryPublisher)) {
                LogUtils.logD(TAG, "Preloading primary OpenAd: " + primaryPublisher);
                loadAdWithPublisher(activity, primaryPublisher,
                        () -> {
                            LogUtils.logI(TAG, "Primary OpenAd preloaded: " + primaryPublisher);
                            loadingPublishers.remove(primaryPublisher);
                        },
                        error -> {
                            LogUtils.logW(TAG, "Primary preload failed: " + error);
                            loadingPublishers.remove(primaryPublisher);
                        }
                );
                loadingPublishers.add(primaryPublisher);
            } else if (isAdReadyForPublisher(primaryPublisher)) {
                LogUtils.logD(TAG, "Primary OpenAd already ready: " + primaryPublisher);
            }

            // Preload fallback if different and not ready
            if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)
                    && !isAdReadyForPublisher(fallbackPublisher) && !loadingPublishers.contains(fallbackPublisher)) {
                LogUtils.logD(TAG, "Preloading fallback OpenAd: " + fallbackPublisher);
                loadAdWithPublisher(activity, fallbackPublisher,
                        () -> {
                            LogUtils.logI(TAG, "Fallback OpenAd preloaded: " + fallbackPublisher);
                            loadingPublishers.remove(fallbackPublisher);
                        },
                        error -> {
                            LogUtils.logW(TAG, "Fallback preload failed: " + error);
                            loadingPublishers.remove(fallbackPublisher);
                        }
                );
                loadingPublishers.add(fallbackPublisher);
            } else if (isAdReadyForPublisher(fallbackPublisher)) {
                LogUtils.logD(TAG, "Fallback OpenAd already ready: " + fallbackPublisher);
            }

        } catch (Exception e) {
            LogUtils.logE(TAG, "Error preloading OpenAd: " + e.getMessage());
        }
    }
}
