package com.camera.gps.adsmanager;

import android.app.Activity;

import com.camera.gps.adsmanager.admob.AdMobInterstitialHelper;
import com.camera.gps.adsmanager.applovin.AppLovinInterstitialHelper;
import com.camera.gps.adsmanager.inmobi.InMobiInterstitialHelper;
import com.camera.gps.adsmanager.ironsource.IronSourceInterstitialHelper;
import com.camera.gps.adsmanager.mintegral.MintegralInterstitialHelper;
import com.camera.gps.adsmanager.pangle.PangleInterstitialAdHelper;
import com.camera.gps.adsmanager.unity.UnityInterstitialHelper;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.model.Ads.RemoteConfigResponse;
import com.camera.gps.util.Utils.LogUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InterstitialAdManager {

    private static final String TAG = "InterstitialAdManager";
    private static volatile InterstitialAdManager INSTANCE;
    private static volatile boolean isShowingInterstitial = false;
    private final Set<String> loadingPublishers;

    @FunctionalInterface
    public interface AdCallback {
        void onFailure(String error);
    }

    private InterstitialAdManager() {
        loadingPublishers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public static InterstitialAdManager getInstance() {
        if (INSTANCE == null) {
            synchronized (InterstitialAdManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InterstitialAdManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Preload interstitial ads for the publishers listed in Remote Config's preload array.
     * This makes the first show instant if those networks are selected later.
     */
    public void preloadPublishersFromConfig(Activity activity) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
            RemoteConfigResponse config = remoteConfig.getRemoteConfigData();
            if (config == null) return;
            if (!remoteConfig.isShowAds()) return;

            List<String> publishersToPreload = config.getPreload();
            if (publishersToPreload == null) return;

            // Avoid duplicates while preserving intention
            Set<String> uniquePublishers = new LinkedHashSet<>();
            for (String publisher : publishersToPreload) {
                uniquePublishers.add(publisher.toLowerCase());
            }
            LogUtils.logD(TAG, "uniquePublishers " + uniquePublishers);

            for (String publisher : uniquePublishers) {
                preloadPublisher(activity, publisher);
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in preloadPublishersFromConfig: " + e.getMessage());
        }
    }

    /**
     * Preload a specific publisher's interstitial irrespective of placement.
     */
    public void preloadPublisher(Activity activity, String publisher) {
        try {
            String normalized = publisher.toLowerCase();
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);

            // Do not preload during global cooldown window
            int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
            if (AdCooldownManager.getInstance().isInGlobalCooldown(cooldownSeconds)) {
                LogUtils.logD(TAG, "Global cooldown active. Skip preloading for " + normalized);
                return;
            }


            // Skip if already ready
            if (isAdReadyForPublisher(normalized)) {
                LogUtils.logD(TAG, "Ad already ready for " + normalized + ". Skip AD");
                return;
            }

            // Avoid duplicate concurrent preloads
            if (!loadingPublishers.add(normalized)) {
                LogUtils.logD(TAG, "Preload already in progress for " + normalized + ". Ignoring duplicate call");
                return;
            }

            Runnable clearLoading = () -> loadingPublishers.remove(normalized);

            LogUtils.logI(TAG, "preloadPublisher " + normalized);
            switch (normalized) {
                case "admob":
                    AdMobInterstitialHelper.loadAdMobInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "applovin":
                    AppLovinInterstitialHelper.loadAppLovinInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "inmobi":
                    InMobiInterstitialHelper.loadInmobiInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "pangle":
                    PangleInterstitialAdHelper.loadPangleInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "mintegral":
                    MintegralInterstitialHelper.loadMintegralInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "unity":
                    UnityInterstitialHelper.loadUnityInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                case "ironsource":
                    IronSourceInterstitialHelper.loadIronSourceInterstitialAd(activity, clearLoading, error -> clearLoading.run());
                    break;
                default:
                    LogUtils.logW(TAG, "preloadPublisher: Unknown publisher " + normalized + " - skipping");
                    clearLoading.run();
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in preloadPublisher: " + e.getMessage());
        }
    }


    //    public void loadAndShowInterstitialAd(Activity activity, String adsName, Runnable onAdClosed, AdCallback onAdFailed) {
//        try {
//            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
//
//            if (!remoteConfig.isShowAds()) {
//                LogUtils.logD(TAG, "Ads globally disabled");
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
//            if (adsData == null) {
//                LogUtils.logE(TAG, "No ads data found for: " + adsName);
//                if (onAdFailed != null) onAdFailed.onFailure("No ads data found");
//                return;
//            }
//
//            if (!adsData.isEnableAds()) {
//                LogUtils.logD(TAG, "Ads disabled for: " + adsName);
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//            // Check placement cooldown
//            int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
//            if (AdCooldownManager.getInstance().isInCooldown(adsName, cooldownSeconds)) {
//                LogUtils.logD(TAG, "Ad in cooldown for placement: " + adsName);
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//            String primaryPublisher = adsData.getPublishers().toLowerCase();
//            String fallbackPublisher = adsData.getAdFailed().toLowerCase();
//
//            LogUtils.logD(TAG, "Loading interstitial ad for " + adsName + " with primary: " + primaryPublisher + ", fallback: " + fallbackPublisher);
//
//            // Try primary publisher first
//            loadAdWithPublisher(activity, primaryPublisher, null, primaryError -> {
//                LogUtils.logI(TAG, "Primary failed: " + primaryError);
//
//                // If primary fails and fallback is configured, try fallback
//                if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
//                    LogUtils.logW(TAG, "Trying fallback: " + fallbackPublisher);
//                    loadAdWithPublisher(activity, fallbackPublisher, null, fallbackError -> {
//                        LogUtils.logE(TAG, "Both primary and fallback failed to load");
//                        if (onAdFailed != null)
//                            onAdFailed.onFailure("Primary: " + primaryError + ", Fallback: " + fallbackError);
//                    });
//                } else {
//                    LogUtils.logE(TAG, "Primary failed and no fallback configured");
//                    if (onAdFailed != null) onAdFailed.onFailure(primaryError);
//                }
//            });
//
//            // Show the ad once loaded
//            showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, error -> {
//                // Try fallback if primary show fails
//                if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
//                    showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackError -> {
//                        if (onAdFailed != null)
//                            onAdFailed.onFailure("Primary: " + error + ", Fallback: " + fallbackError);
//                    });
//                } else {
//                    if (onAdFailed != null) onAdFailed.onFailure(error);
//                }
//            });
//
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error in loadAndShowInterstitialAd: " + e.getMessage());
//            if (onAdFailed != null) onAdFailed.onFailure("System error: " + e.getMessage());
//        }
//    }


    /**
     * Load and show interstitial ad with fallback mechanism
     */
    public void loadAndShowInterstitialAd(Activity activity, String adsName, Runnable onAdClosed) {
        loadAndShowInterstitialAd(activity, adsName, onAdClosed, null);
    }

    public void loadAndShowInterstitialAd(Activity activity, String adsName, Runnable onAdClosed, AdCallback onAdFailed) {
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
                LogUtils.logD(TAG, "Ads disabled for: " + adsName);
                if (onAdClosed != null) onAdClosed.run();
                return;
            }

            // Check placement cooldown
            int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
            if (AdCooldownManager.getInstance().isInCooldown(adsName, cooldownSeconds)) {
                LogUtils.logD(TAG, "Ad in cooldown for placement: " + adsName);
                if (onAdClosed != null) onAdClosed.run();
                return;
            }

            // check global cooldown
            if (AdCooldownManager.getInstance().isInGlobalCooldown(cooldownSeconds)) {
                LogUtils.logD(TAG, "Global cooldown active. Skip showing ad");
                if (onAdClosed != null) onAdClosed.run();
                return;
            }

            String primaryPublisher = adsData.getPublishers().toLowerCase();
            String fallbackPublisher = adsData.getAdFailed().toLowerCase();

            // SCENARIO 1: PRIMARY READY
            if (isAdReadyForPublisher(primaryPublisher)) {
                LogUtils.logI(TAG, "PRIMARY READY - SHOWING PRIMARY");

                showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, primaryShowError -> {
                    LogUtils.logE(TAG, "PRIMARY SHOW FAILED: " + primaryShowError);

                    // CHECK IF FALLBACK IS READY
                    if (!fallbackPublisher.isEmpty() &&
                            !fallbackPublisher.equals(primaryPublisher) &&
                            isAdReadyForPublisher(fallbackPublisher)) {

                        LogUtils.logI(TAG, "FALLBACK READY - SHOWING FALLBACK");
                        showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
                            LogUtils.logE(TAG, "BOTH SHOW FAILED - PROCEED WITHOUT AD");
                            if (onAdClosed != null) onAdClosed.run(); // Proceed without ad
                        });
                    } else {
                        // FALLBACK NOT READY - LOAD FALLBACK
                        if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
                            LogUtils.logI(TAG, "FALLBACK NOT READY - LOADING FALLBACK");
                            loadAdWithPublisher(activity, fallbackPublisher, () -> {
                                showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
                                    LogUtils.logE(TAG, "FALLBACK SHOW FAILED AFTER LOAD - PROCEED WITHOUT AD");
                                    if (onAdClosed != null) onAdClosed.run();
                                });
                            }, fallbackLoadError -> {
                                LogUtils.logE(TAG, "FALLBACK LOAD FAILED - PROCEED WITHOUT AD");
                                if (onAdClosed != null) onAdClosed.run();
                            });
                        } else {
                            LogUtils.logE(TAG, "NO FALLBACK CONFIGURED - PROCEED WITHOUT AD");
                            if (onAdClosed != null) onAdClosed.run();
                        }
                    }
                });
            }
            // SCENARIO 2: PRIMARY NOT READY - CHECK FALLBACK
            else if (!fallbackPublisher.isEmpty() &&
                    !fallbackPublisher.equals(primaryPublisher) &&
                    isAdReadyForPublisher(fallbackPublisher)) {

                LogUtils.logI(TAG, "PRIMARY NOT READY - FALLBACK READY - SHOWING FALLBACK");
                showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
                    LogUtils.logE(TAG, "FALLBACK SHOW FAILED: " + fallbackShowError);

                    LogUtils.logI(TAG, "LOADING PRIMARY AFTER FALLBACK SHOW FAILED");
                    loadAdWithPublisher(activity, primaryPublisher, () -> {
                        showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, primaryShowError -> {
                            LogUtils.logE(TAG, "PRIMARY SHOW FAILED AFTER LOAD - PROCEED WITHOUT AD");
                            if (onAdClosed != null) onAdClosed.run();
                        });
                    }, primaryLoadError -> {
                        LogUtils.logE(TAG, "PRIMARY LOAD FAILED AFTER FALLBACK SHOW FAILED - PROCEED WITHOUT AD");
                        if (onAdClosed != null) onAdClosed.run();
                    });
                });
            }
            // SCENARIO 3: NEITHER PRIMARY NOR FALLBACK READY - LOAD BOTH
            else {
                LogUtils.logI(TAG, "NEITHER PRIMARY NOR FALLBACK READY - LOADING PRIMARY FIRST");

                // LOAD PRIMARY FIRST
                loadAdWithPublisher(activity, primaryPublisher, () -> {
                    // PRIMARY LOADED - SHOW PRIMARY
                    LogUtils.logI(TAG, "PRIMARY LOADED - SHOWING PRIMARY");
                    showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, primaryShowError -> {
                        // PRIMARY SHOW FAILED AFTER LOAD
                        LogUtils.logE(TAG, "PRIMARY SHOW FAILED AFTER LOAD: " + primaryShowError);

                        // LOAD FALLBACK
                        if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
                            LogUtils.logI(TAG, "LOADING FALLBACK AFTER PRIMARY SHOW FAILED");
                            loadAdWithPublisher(activity, fallbackPublisher, () -> {
                                showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
                                    LogUtils.logE(TAG, "BOTH LOAD+SHOW FAILED - PROCEED WITHOUT AD");
                                    if (onAdClosed != null) onAdClosed.run();
                                });
                            }, fallbackLoadError -> {
                                LogUtils.logE(TAG, "FALLBACK LOAD FAILED AFTER PRIMARY SHOW FAILED - PROCEED WITHOUT AD");
                                if (onAdClosed != null) onAdClosed.run();
                            });
                        } else {
                            LogUtils.logE(TAG, "NO FALLBACK CONFIGURED - PROCEED WITHOUT AD");
                            if (onAdClosed != null) onAdClosed.run();
                        }
                    });
                }, primaryLoadError -> {
                    // PRIMARY LOAD FAILED
                    LogUtils.logE(TAG, "PRIMARY LOAD FAILED: " + primaryLoadError);

                    // LOAD FALLBACK
                    if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
                        LogUtils.logI(TAG, "LOADING FALLBACK AFTER PRIMARY LOAD FAILED");
                        loadAdWithPublisher(activity, fallbackPublisher, () -> {
                            showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
                                LogUtils.logE(TAG, "FALLBACK SHOW FAILED AFTER PRIMARY LOAD FAILED - PROCEED WITHOUT AD");
                                if (onAdClosed != null) onAdClosed.run();
                            });
                        }, fallbackLoadError -> {
                            LogUtils.logE(TAG, "BOTH PRIMARY AND FALLBACK LOAD FAILED - PROCEED WITHOUT AD");
                            if (onAdClosed != null) onAdClosed.run();
                        });
                    } else {
                        LogUtils.logE(TAG, "PRIMARY LOAD FAILED AND NO FALLBACK - PROCEED WITHOUT AD");
                        if (onAdClosed != null) onAdClosed.run();
                    }
                });
            }

        } catch (Exception e) {
            LogUtils.logE(TAG, "Error in loadAndShowInterstitialAd: " + e.getMessage());
            if (onAdClosed != null) onAdClosed.run(); // Proceed without ad on system error
        }
    }

//    public void loadAndShowInterstitialAd(Activity activity, String adsName, Runnable onAdClosed, AdCallback onAdFailed) {
//        try {
//            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);
//
//            if (!remoteConfig.isShowAds()) {
//                LogUtils.logD(TAG, "Ads globally disabled");
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//            AdsData adsData = remoteConfig.getAdsDataByName(adsName);
//            if (adsData == null) {
//                LogUtils.logE(TAG, "No ads data found for: " + adsName);
//                if (onAdFailed != null) onAdFailed.onFailure("No ads data found");
//                return;
//            }
//
//            if (!adsData.isEnableAds()) {
//                LogUtils.logD(TAG, "Ads disabled for: " + adsName);
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//
//            // Check placement cooldown
//            int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
//            if (AdCooldownManager.getInstance().isInCooldown(adsName, cooldownSeconds)) {
//                LogUtils.logD(TAG, "Ad in cooldown for placement: " + adsName);
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//
//            // check global cooldown - **************
//            if (AdCooldownManager.getInstance().isInGlobalCooldown(cooldownSeconds)) {
//                LogUtils.logD(TAG, "Global cooldown active. Skip preloading");
//                if (onAdClosed != null) onAdClosed.run();
//                return;
//            }
//
//            String primaryPublisher = adsData.getPublishers().toLowerCase();
//            String fallbackPublisher = adsData.getAdFailed().toLowerCase();
//
//            if (isAdReadyForPublisher(primaryPublisher)) {
//                LogUtils.logI(TAG, "Primary ad already loaded, showing immediately");
//                showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, error -> {
//                    // Try fallback if show fails...
//                });
//                return;
//            }
//
//            // ADD THIS: Check if fallback is already loaded
//            if (!fallbackPublisher.isEmpty() && isAdReadyForPublisher(fallbackPublisher)) {
//                LogUtils.logI(TAG, "Fallback ad already loaded, showing immediately");
//                showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, error -> {
//                    // Handle failure...
//                });
//                return;
//            }
//
//            LogUtils.logD(TAG, "Loading interstitial ad for " + adsName + " with primary: " + primaryPublisher + ", fallback: " + fallbackPublisher);
//
//            // Try primary publisher first
//            loadAdWithPublisher(activity, primaryPublisher, () -> {
//                // Primary ad loaded successfully, now show it
//                LogUtils.logI(TAG, "Primary ad loaded successfully, showing ad");
//                showAdWithPublisher(activity, primaryPublisher, adsName, onAdClosed, onAdFailed, showError -> {
//                    LogUtils.logE(TAG, "Primary ad failed to show: " + showError);
//                    // Try fallback if primary show fails
//                    if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
//                        LogUtils.logW(TAG, "Trying fallback for showing: " + fallbackPublisher);
//                        loadAdWithPublisher(activity, fallbackPublisher, () -> {
//                            // Fallback loaded, show it
//                            showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
//                                LogUtils.logE(TAG, "Fallback ad also failed to show: " + fallbackShowError);
//                                if (onAdFailed != null)
//                                    onAdFailed.onFailure("Primary show: " + showError + ", Fallback show: " + fallbackShowError);
//                            });
//                        }, fallbackLoadError -> {
//                            LogUtils.logE(TAG, "Fallback ad failed to load: " + fallbackLoadError);
//                            if (onAdFailed != null)
//                                onAdFailed.onFailure("Primary show: " + showError + ", Fallback load: " + fallbackLoadError);
//                        });
//                    } else {
//                        if (onAdFailed != null) onAdFailed.onFailure(showError);
//                    }
//                });
//            }, primaryError -> {
//                LogUtils.logI(TAG, "Primary failed to load: " + primaryError);
//
//                // If primary fails and fallback is configured, try fallback
//                if (!fallbackPublisher.isEmpty() && !fallbackPublisher.equals(primaryPublisher)) {
//                    LogUtils.logW(TAG, "Trying fallback for loading: " + fallbackPublisher);
//                    loadAdWithPublisher(activity, fallbackPublisher, () -> {
//                        // Fallback loaded successfully, now show it
//                        LogUtils.logI(TAG, "Fallback ad loaded successfully, showing ad");
//                        showAdWithPublisher(activity, fallbackPublisher, adsName, onAdClosed, onAdFailed, fallbackShowError -> {
//                            LogUtils.logE(TAG, "Fallback ad failed to show: " + fallbackShowError);
//                            if (onAdFailed != null)
//                                onAdFailed.onFailure("Primary load: " + primaryError + ", Fallback show: " + fallbackShowError);
//                        });
//                    }, fallbackError -> {
//                        LogUtils.logE(TAG, "Both primary and fallback failed to load");
//                        if (onAdFailed != null)
//                            onAdFailed.onFailure("Primary: " + primaryError + ", Fallback: " + fallbackError);
//                    });
//                } else {
//                    LogUtils.logE(TAG, "Primary failed and no fallback configured");
//                    if (onAdFailed != null) onAdFailed.onFailure(primaryError);
//                }
//            });
//
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error in loadAndShowInterstitialAd: " + e.getMessage());
//            if (onAdFailed != null) onAdFailed.onFailure("System error: " + e.getMessage());
//        }
//    }

    private void loadAdWithPublisher(Activity activity, String publisher, Runnable onLoaded, AdCallback onPublisherFailed) {
        try {
            switch (publisher) {
                case "admob":
                    AdMobInterstitialHelper.loadAdMobInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "applovin":
                    AppLovinInterstitialHelper.loadAppLovinInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "inmobi":
                    InMobiInterstitialHelper.loadInmobiInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "pangle":
                    PangleInterstitialAdHelper.loadPangleInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "mintegral":
                    MintegralInterstitialHelper.loadMintegralInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "unity":
                    UnityInterstitialHelper.loadUnityInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                case "ironsource":
                    IronSourceInterstitialHelper.loadIronSourceInterstitialAd(activity, onLoaded, error -> onPublisherFailed.onFailure(error));
                    break;
                default:
                    LogUtils.logW(TAG, "Unknown publisher: " + publisher);
                    onPublisherFailed.onFailure("Unknown publisher: " + publisher);
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Unexpected error in loadAdWithPublisher for " + publisher + ": " + e.getMessage());
            onPublisherFailed.onFailure("Unexpected error: " + e.getMessage());
        }
    }

    private void showAdWithPublisher(Activity activity, String publisher, String placement, Runnable onAdClosed, AdCallback onAdFailed, AdCallback onPublisherFailed) {
        try {
            RemoteConfigManager remoteConfig = RemoteConfigManager.getInstance(activity);

            switch (publisher) {
                case "admob":
                    AdMobInterstitialHelper.showAdMobInterstitialAd(activity,
                            () -> {
                                int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                if (onAdClosed != null) onAdClosed.run();
                            },
                            () -> {
                                onPublisherFailed.onFailure("AdMob failed to show");
                            }
                    );
                    break;
                case "applovin":
                    try {
                        AppLovinInterstitialHelper.showAppLovinInterstitialAd(activity,
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                }
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "AppLovin ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("AppLovin failed to show: " + e.getMessage());
                    }
                    break;
                case "inmobi":
                    try {
                        InMobiInterstitialHelper.showInmobiInterstitialAd(activity,
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                }
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "InMobi ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("InMobi failed to show: " + e.getMessage());
                    }
                    break;
                case "pangle":
                    try {
                        PangleInterstitialAdHelper.showPangleInterstitialAd(activity,
                                () -> { /* Ad started showing */ },
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                }
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "Pangle ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("Pangle failed to show: " + e.getMessage());
                    }
                    break;
                case "mintegral":
                    try {
                        MintegralInterstitialHelper.showMintegralInterstitialAd(activity,
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                }
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "Mintegral ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("Mintegral failed to show: " + e.getMessage());
                    }
                    break;
                case "unity":
                    try {
                        UnityInterstitialHelper.showUnityInterstitialAd(activity,
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                },
                                () -> {
                                    onPublisherFailed.onFailure("Unity failed to show");
                                }
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "Unity ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("Unity failed to show: " + e.getMessage());
                    }
                    break;
                case "ironsource":
                    try {
                        IronSourceInterstitialHelper.showIronSourceInterstitialAd(activity,
                                () -> {
                                    int cooldownSeconds = remoteConfig.getInterstitialCooldownSeconds();
                                    AdCooldownManager.getInstance().onAdClosed(activity, placement, cooldownSeconds, publisher);
                                    if (onAdClosed != null) onAdClosed.run();
                                },
                                () -> onPublisherFailed.onFailure("IronSource failed to show")
                        );
                    } catch (Exception e) {
                        LogUtils.logE(TAG, "IronSource ad show error: " + e.getMessage());
                        onPublisherFailed.onFailure("IronSource failed to show: " + e.getMessage());
                    }
                    break;
                default:
                    LogUtils.logW(TAG, "Unknown publisher: " + publisher);
                    onPublisherFailed.onFailure("Unknown publisher: " + publisher);
                    break;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Unexpected error in showAdWithPublisher for " + publisher + ": " + e.getMessage());
            onPublisherFailed.onFailure("Unexpected error: " + e.getMessage());
        }
    }


    public static boolean isInterstitialShowing() {
        return isShowingInterstitial;
    }

    public static void setInterstitialShowing(boolean showing) {
        isShowingInterstitial = showing;
    }

    private boolean isAdReadyForPublisher(String publisher) {
        try {
            switch (publisher) {
                case "admob":
                    return AdMobInterstitialHelper.isAdReady();
                case "applovin":
                    return AppLovinInterstitialHelper.isAdReady();
                case "inmobi":
                    return InMobiInterstitialHelper.isAdReady();
                case "pangle":
                    return PangleInterstitialAdHelper.isAdReady();
                case "mintegral":
                    return MintegralInterstitialHelper.isAdReady();
                case "unity":
                    return UnityInterstitialHelper.isAdReady();
                case "ironsource":
                    return IronSourceInterstitialHelper.isAdReady();
                default:
                    return false;
            }
        } catch (Exception e) {
            LogUtils.logE(TAG, "Error checking ad ready for " + publisher + ": " + e.getMessage());
            return false;
        }
    }
}