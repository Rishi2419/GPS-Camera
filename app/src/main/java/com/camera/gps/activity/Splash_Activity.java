//package com.camera.gps.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import androidx.appcompat.app.AppCompatActivity;
//import com.airbnb.lottie.LottieAnimationView;
//import com.camera.gps.R;
//import com.camera.gps.MyApplication;
//
//public class Splash_Activity extends AppCompatActivity {
//
//    private static final int SPLASH_DELAY = 3000;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Hide navigation bar
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        setContentView(R.layout.activity_splash_screen);
//        LottieAnimationView lottieView = findViewById(R.id.lottieAnimationView);
//        lottieView.setImageAssetsFolder("splash_lottie/images");
//        lottieView.setRepeatCount(0);
//        lottieView.playAnimation();
//
//        // Proceed to MainActivity after delay
//        new Handler(getMainLooper()).postDelayed(() -> {
//            if (!MyApplication.getIsLanguage()) {
//                startActivity(new Intent(Splash_Activity.this, Language_Activity.class));
//            }
//            else if (!MyApplication.isCameraPermissionGranted() || !MyApplication.isLocationPermissionGranted()) {
//                startActivity(new Intent(Splash_Activity.this, Permissions_Activity.class));
//            }
//            else if(MyApplication.getIsOnBoardingScreen()) {
//                startActivity(new Intent(Splash_Activity.this, Onboarding_Activity.class));
//            } else {
//                startActivity(new Intent(Splash_Activity.this, MainActivity.class));
//            }
//            finish();
//        }, SPLASH_DELAY);
//}
//
//    @Override
//    public void onBackPressed() {
//        // Disable back button during splash
//        // Do nothing
//    }
//}

package com.camera.gps.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.camera.gps.R;
import com.camera.gps.MyApplication;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.OpenAdManager;
import com.camera.gps.adsmanager.RemoteConfigManager;
import com.camera.gps.model.Ads.AdsData;
import com.camera.gps.util.Utils;
import com.camera.gps.util.Utils.LogUtils;
import com.camera.gps.util.SP;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Locale;

public class Splash_Activity extends AppCompatActivity {

    private static final String TAG = "Splash_Activity_Rishi";
    private static final int SPLASH_DELAY = 2000;
    private SP SP;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private RemoteConfigManager remoteConfigManager;
    private boolean hasNavigated = false;
    private long splashStartedAtMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashStartedAtMs = System.currentTimeMillis();

        // Hide navigation bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_splash_screen);

        SP = new SP(this);
        LottieAnimationView lottieView = findViewById(R.id.lottieAnimationView);
        lottieView.setImageAssetsFolder("splash_lottie/images");
        lottieView.setRepeatCount(0);
        lottieView.playAnimation();

        LogUtils.logD(TAG, "Splash_Activity onCreate started");
        LogUtils.logD(TAG, "Network available: " + MyApplication.isNetworkAvailable(this));

        Locale locale = new Locale(MyApplication.getLanguageCode());
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        MyApplication.setLanguageCode(MyApplication.getLanguageCode());

        // Initialize remote config manager
        remoteConfigManager = RemoteConfigManager.getInstance(this);

        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            LogUtils.logD(TAG, "Network available and not premium, starting splash ad flow");
            getAdsUnitData();
            getRemoteConfigData();
        } else {
            LogUtils.logD(TAG, "No network available or premium member, skipping splash ad work");
            runAfterMinimumSplash(this::mainNavigation);
        }
    }

    private void getAdsUnitData() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Save additional remote config values
                SP.saveString(this, "app_name", firebaseRemoteConfig.getString("app_name"));
                LogUtils.logI(TAG, "Additional remote config data saved successfully");
            } else {
                LogUtils.logE(TAG, "Failed to fetch additional remote config data");
            }
        }).addOnFailureListener(e -> {
            LogUtils.logE(TAG, "Error fetching additional remote config: " + e.getMessage());
        });
    }

    private void getRemoteConfigData() {
        LogUtils.logD(TAG, "Fetching remote config data...");

        remoteConfigManager.fetchAndStore(success -> {
            LogUtils.logD(TAG, "fetchAndStore() callback → success = " + success);

            if (success) {
                boolean showAds = remoteConfigManager.isShowAds();
                String admobInterstitial = remoteConfigManager.getAdId("admob", "interstitial");
                AdsData splashConfig = remoteConfigManager.getAdsDataByName("splash_ad");

                LogUtils.logD(TAG, "Show Ads flag: " + showAds);
                LogUtils.logD(TAG, "AdMob Interstitial ID: " + admobInterstitial);
                if (splashConfig != null) {
                    LogUtils.logD(TAG, "Splash Config → " +
                            "name=" + splashConfig.getAdsName() +
                            ", type=" + splashConfig.getAdsType() +
                            ", enabled=" + splashConfig.isEnableAds() +
                            ", publisher=" + splashConfig.getPublishers());
                } else {
                    LogUtils.logE(TAG, "Splash Config is NULL");
                }

                // Preload ads for faster display
                LogUtils.logD(TAG, "Calling preloadPublishersFromConfig()");
                InterstitialAdManager.getInstance().preloadPublishersFromConfig(this);

                // Also preload specific app resume ad
                LogUtils.logD(TAG, "Preloading specific app_resume_ad");
                OpenAdManager.getInstance().preloadOpenAd(this, "app_resume_ad");


                runAfterMinimumSplash(this::openAdLoad);

            } else {
                LogUtils.logE(TAG, "❌ Remote config fetch FAILED");

                // Extra debug: log last fetch status
                try {
                    FirebaseRemoteConfig frc = FirebaseRemoteConfig.getInstance();
                    LogUtils.logD(TAG, "Last Fetch Status: " + frc.getInfo().getLastFetchStatus());
                    LogUtils.logD(TAG, "Last Fetch Time: " + frc.getInfo().getFetchTimeMillis());
                } catch (Exception e) {
                    LogUtils.logE(TAG, "Error reading fetch info: " + e.getMessage());
                }

                runAfterMinimumSplash(this::mainNavigation);
            }
        });
    }


    private void openAdLoad() {
        AdsData config = remoteConfigManager.getAdsDataByName("splash_ad");
        if (config == null) {
            LogUtils.logE(TAG, "No splash ad config found, proceeding to main navigation");
            mainNavigation();
            return;
        }

        LogUtils.logI(TAG, "openAdLoad: " + config.getAdsType());

        if (remoteConfigManager.isShowAds()) {
            if ("splash_ad".equals(config.getAdsName())) {
                if (config.isEnableAds()) {
                    if ("interstitial".equals(config.getAdsType())) {
                        LogUtils.logD(TAG, "Loading interstitial ad");
                        InterstitialAdManager.getInstance().loadAndShowInterstitialAd(
                                this,
                                "splash_ad",
                                this::mainNavigation,
                                new InterstitialAdManager.AdCallback() {
                                    @Override
                                    public void onFailure(String error) {
                                        LogUtils.logE(TAG, "Interstitial ad failed: " + error);
                                        mainNavigation();
                                    }
                                }
                        );
                    } else {
                        LogUtils.logD(TAG, "Loading open ad");
                        OpenAdManager.getInstance().loadAndShowOpenAd(
                                this,
                                "splash_ad",
                                this::mainNavigation,
                                new OpenAdManager.AdCallback() {
                                    @Override
                                    public void onFailure(String error) {
                                        LogUtils.logE(TAG, "Open ad failed: " + error);
                                        mainNavigation();
                                    }
                                },
                                true
                        );
                    }
                } else {
                    LogUtils.logD(TAG, "Splash ads disabled, proceeding to main navigation");
                    mainNavigation();
                }
            } else {
                LogUtils.logW(TAG, "Splash ad config name mismatch, proceeding to main navigation");
                mainNavigation();
            }
        } else {
            LogUtils.logD(TAG, "Ads globally disabled, proceeding to main navigation");
            mainNavigation();
        }
    }

    private void mainNavigation() {
        if (hasNavigated || isFinishing() || isDestroyed()) {
            return;
        }
        hasNavigated = true;
        LogUtils.logI(TAG, "Main navigation started");

        if (!MyApplication.getIsLanguage()) {
            startActivity(new Intent(Splash_Activity.this, Language_Activity.class));
        } else if (!MyApplication.isCameraPermissionGranted() || !MyApplication.isLocationPermissionGranted()) {
            startActivity(new Intent(Splash_Activity.this, Permissions_Activity.class));
        } else if (MyApplication.getIsOnBoardingScreen()) {
            startActivity(new Intent(Splash_Activity.this, Onboarding_Activity.class));
        } else {
            startActivity(new Intent(Splash_Activity.this, MainActivity.class));
        }
        finish();
//
//        try {
//            if ("shortcut".equals(getIntent().getStringExtra("isFrom"))) {
//                // Handle shortcut navigation if needed
//                LogUtils.logD(TAG, "Shortcut detected - implement your shortcut handling logic here");
//                // startActivity(new Intent(this, TroubleshootActivity.class));
//            } else if (!SP.getBoolean(this, "isLanguageScreenDisplay", false)) {
//                LogUtils.logD(TAG, "Navigating to Language Activity");
//                startActivity(new Intent(this, Language_Activity.class));
//            } else {
//                if (!SP.getBoolean(this, "onboard_first_time", false)) {
//                    LogUtils.logD(TAG, "Navigating to Onboarding Activity");
//                    startActivity(new Intent(this, Onboarding_Activity.class));
//                } else {
//                    LogUtils.logD(TAG, "Navigating to Main Activity");
//                    startActivity(new Intent(this, MainActivity.class));
//                }
//            }
//        } catch (Exception e) {
//            LogUtils.logE(TAG, "Error in navigation: " + e.getMessage());
//            // Fallback to main activity
//            startActivity(new Intent(this, MainActivity.class));
//        }
//
//        finish();
    }

    private void runAfterMinimumSplash(Runnable action) {
        long elapsedMs = System.currentTimeMillis() - splashStartedAtMs;
        long remainingMs = Math.max(0, SPLASH_DELAY - elapsedMs);
        new Handler(getMainLooper()).postDelayed(() -> {
            if (!hasNavigated && !isFinishing() && !isDestroyed()) {
                action.run();
            }
        }, remainingMs);
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash
        LogUtils.logD(TAG, "Back button pressed during splash - ignoring");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.logD(TAG, "Splash_Activity destroyed");
    }
}
