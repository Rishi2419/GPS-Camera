package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.NativeAdManager;
import com.camera.gps.adsmanager.admob.AdMobBannerAdHelper;
import com.camera.gps.camerax.util.SharedPrefsSettings;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.databinding.ActivitySettingsBinding;
import com.camera.gps.dialogs.DateTimeDialog;
import com.camera.gps.dialogs.FontStyleDialog;
import com.camera.gps.dialogs.RateDialog;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.listener.OnFontSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.model.StampTemplateDefaults;
import com.camera.gps.premium.PremiumManager;
import com.camera.gps.premium.PremiumTestBottomSheet;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.StampSettingsBottomSheets;
import com.camera.gps.util.Utils;
import com.camera.gps.viewmodel.FontStyleViewModel;

public class Settings_Activity extends InsetAwareActivity {

    public static final String EXTRA_CURRENT_FONT_STYLE = "settings_current_font_style";
    public static final String EXTRA_CURRENT_DATE_FORMAT = "settings_current_date_format";
    public static final String EXTRA_CURRENT_TIME_FORMAT = "settings_current_time_format";
    public static final String EXTRA_CURRENT_COMBINED_FORMAT = "settings_current_combined_format";
    public static final String EXTRA_CURRENT_MAP_TYPE = "settings_current_map_type";
    public static final String EXTRA_SESSION_FONT_STYLE = "settings_session_font_style";
    public static final String EXTRA_SESSION_DATE_FORMAT = "settings_session_date_format";
    public static final String EXTRA_SESSION_TIME_FORMAT = "settings_session_time_format";
    public static final String EXTRA_SESSION_COMBINED_FORMAT = "settings_session_combined_format";
    public static final String EXTRA_SESSION_MAP_TYPE = "settings_session_map_type";

    private ActivitySettingsBinding binding;
    private FontStyleDialog fontStyleDialog;
    private FontStyleViewModel fontViewModel;
    private DateTimeDialog dateTimeDialog;
    private RateDialog rateDialog;
    private MyLocation selectedLocation;
    private ActivityResultLauncher locationLauncher;
    private ActivityResultLauncher<Intent> mapTypeLauncher;

    private boolean showWatermark;
    private String currentFontStyle;
    private String currentDateFormat;
    private String currentTimeFormat;
    private String currentCombinedFormat;
    private int currentMapType;
    private boolean fontChanged;
    private boolean dateTimeChanged;
    private boolean mapTypeChanged;
    private Integer activeSavedLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int activeLocationId = getIntent().getIntExtra(
                MyLocation_Activity.EXTRA_ACTIVE_SAVED_LOCATION_ID, -1);
        activeSavedLocationId = activeLocationId >= 0 ? activeLocationId : null;

        locationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.hasExtra(MyApplication.EXTRA_LOCATION)) {
                            selectedLocation = (MyLocation) data.getSerializableExtra(MyApplication.EXTRA_LOCATION);
                            activeSavedLocationId = selectedLocation != null
                                    ? selectedLocation.getId()
                                    : null;
                            if (selectedLocation != null) {
                                // Location selection is the final action in this flow.
                                // Return directly to the camera instead of making the
                                // user come back to Settings and press Back again.
                                setSessionResult();
                                finish();
                            }
                        }
                    }
                }
        );

        mapTypeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                            && result.getData().hasExtra(Map_Activity.EXTRA_SELECTED_MAP_TYPE)) {
                        currentMapType = result.getData().getIntExtra(
                                Map_Activity.EXTRA_SELECTED_MAP_TYPE,
                                currentMapType
                        );
                        mapTypeChanged = true;
                    }
                }
        );

        fontViewModel = new ViewModelProvider(this).get(FontStyleViewModel.class);
        loadCurrentSessionSettings();


        setupAppVersion();
        setupListeners();
        setupSwitches();
        updatePremiumUi();
        loadNativeAds();
        loadBottomBannerAd();
    }

    private void loadCurrentSessionSettings() {
        Intent intent = getIntent();
        currentFontStyle = intent.getStringExtra(EXTRA_CURRENT_FONT_STYLE);
        currentDateFormat = intent.getStringExtra(EXTRA_CURRENT_DATE_FORMAT);
        currentTimeFormat = intent.getStringExtra(EXTRA_CURRENT_TIME_FORMAT);
        currentCombinedFormat = intent.getStringExtra(EXTRA_CURRENT_COMBINED_FORMAT);
        currentMapType = intent.getIntExtra(EXTRA_CURRENT_MAP_TYPE, MyApplication.getMapType());
    }

    private void setupAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.tvAppVersion.setText(getString(
                    R.string.app_version_format,
                    packageInfo.versionName
            ));
        } catch (PackageManager.NameNotFoundException e) {
            binding.tvAppVersion.setVisibility(android.view.View.GONE);
        }
    }

    private void loadNativeAds() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            NativeAdManager.getInstance().loadAndShowNativeAd(
                    this,
                    "settings_native",
                    binding.flNativeSettingsOne,
                    false,
                    null,
                    null
            );
            NativeAdManager.getInstance().loadAndShowNativeAd(
                    this,
                    "settings_native",
                    binding.flNativeSettingsTwo,
                    false,
                    null,
                    null
            );
        } else {
            binding.flNativeSettingsOne.setVisibility(android.view.View.GONE);
            binding.flNativeSettingsTwo.setVisibility(android.view.View.GONE);
        }
    }

    private void loadBottomBannerAd() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            binding.flSettingsBanner.post(() -> AdMobBannerAdHelper.loadBannerAd(this, binding.flSettingsBanner, "settings_banner"));
        } else {
            binding.flSettingsBanner.setVisibility(android.view.View.GONE);
        }
    }

    private void setupListeners() {
        binding.premiumBanner.setOnClickListener(v -> showPremiumSheet());
        binding.btnUpgrade.setOnClickListener(v -> showPremiumSheet());

        binding.layoutMap.setOnClickListener(v -> {

            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "map_open_from_setting_interstitial", () -> {
                        setInterstitialShowing(false);
                        openMapForSession();
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        Utils.LogUtils.logE("Map_fromSettings", "Ad failed: " + errorMsg);
                    });
                } else {
                    Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                openMapForSession();
            }

        });

        binding.layoutMyLocation.setOnClickListener(v -> {

            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "location_open_from_setting_interstitial", () -> {
                        setInterstitialShowing(false);
                        myLocationNavigation();
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        Utils.LogUtils.logE("MyLocation_fromSettings", "Ad failed: " + errorMsg);
                    });
                } else {
                    Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                myLocationNavigation();
            }
        });

        binding.layoutDateTimeFormat.setOnClickListener(v -> showDateTimeDialog());

        binding.layoutFontFormat.setOnClickListener(v -> showFontStyleDialog());

        binding.layoutRateUs.setOnClickListener(v -> showRateUsDialog());

        binding.layoutLanguage.setOnClickListener(v -> {

                    if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                        if (!InterstitialAdManager.isInterstitialShowing()) {
                            setInterstitialShowing(true);
                            InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "language_open_interstitial", () -> {
                                setInterstitialShowing(false);
                                openLanguageSettings();
                            }, errorMsg -> {
                                setInterstitialShowing(false);
                                Utils.LogUtils.logE("Language", "Ad failed: " + errorMsg);
                            });
                        } else {
                            Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                        }
                    } else {
                        openLanguageSettings();
                    }
                }
        );

        binding.layoutFeedback.setOnClickListener(v -> {

            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "feedback_open_interstitial", () -> {
                        setInterstitialShowing(false);
                        startActivity(new Intent(this, FeedBack_Activity.class));
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        Utils.LogUtils.logE("Feedback", "Ad failed: " + errorMsg);
                    });
                } else {
                    Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                startActivity(new Intent(this, FeedBack_Activity.class));
            }

        });

        binding.layoutShare.setOnClickListener(v -> shareApplication(this));

        binding.layoutPrivacyPolicy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class))
        );

        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void openMapForSession() {
        Intent intent = new Intent(this, Map_Activity.class);
        intent.putExtra(Map_Activity.EXTRA_SELECTED_MAP_TYPE, currentMapType);
        mapTypeLauncher.launch(intent);
    }

    private void openLanguageSettings() {
        Intent intent = new Intent(this, Language_Activity.class);
        intent.putExtra(Language_Activity.EXTRA_IS_SETTING, true);
        startActivity(intent);
    }

    private void myLocationNavigation() {
        MyLocation receivedLocation = (MyLocation) getIntent().getSerializableExtra(MyApplication.EXTRA_LOCATION);
        Intent intent = new Intent(Settings_Activity.this, MyLocation_Activity.class);
        intent.putExtra(MyApplication.EXTRA_LOCATION, receivedLocation);
        if (activeSavedLocationId != null) {
            intent.putExtra(MyLocation_Activity.EXTRA_ACTIVE_SAVED_LOCATION_ID,
                    activeSavedLocationId);
        }

        intent.putExtra("SOURCE", "SETTINGS");
       // startActivity(intent);
        locationLauncher.launch(intent);
    }

    private void setupSwitches() {
        // Watermark
        showWatermark = PremiumManager.isPremium(this)
                ? MyApplication.getShowWatermark()
                : true;
        if (!PremiumManager.isPremium(this)) {
            MyApplication.setShowWatermark(true);
        }
        binding.switchWatermark.setChecked(showWatermark);
        binding.switchWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!PremiumManager.isPremium(this) && !isChecked) {
                buttonView.setChecked(true);
                showPremiumSheet();
                return;
            }
            showWatermark = isChecked;
            MyApplication.setShowWatermark(isChecked);
        });
        binding.layoutWatermark.setOnClickListener(v -> {
            if (!PremiumManager.isPremium(this)) {
                showPremiumSheet();
            }
        });

        // Image Quality
        boolean currentQualityState = SharedPrefsSettings.getImageMaxQuality(this);
        binding.switchImgQuality.setChecked(currentQualityState);
        binding.switchImgQuality.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPrefsSettings.setImageMaxQuality(isChecked, this));

        // Video Sound
        boolean currentSoundStatus = SharedPrefsSettings.getSoundStatus(this);
        binding.switchVideoVolume.setChecked(currentSoundStatus);
        binding.switchVideoVolume.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPrefsSettings.setSoundStatus(isChecked, this));
    }

    private void showPremiumSheet() {
        PremiumTestBottomSheet.show(this, new PremiumTestBottomSheet.Listener() {
            @Override
            public void onPremiumChanged(boolean premium) {
                if (!premium) {
                    showWatermark = true;
                    MyApplication.setShowWatermark(true);
                }
                updatePremiumUi();
                refreshAdsForPremiumState();
            }

            @Override
            public void onContinueFree() {
                applyDefaultFreeSettings();
                updatePremiumUi();
            }
        });
    }

    private void updatePremiumUi() {
        boolean premium = PremiumManager.isPremium(this);
        binding.shimmerPremium.setVisibility(android.view.View.VISIBLE);
        if (premium) {
            binding.shimmerPremium.stopShimmer();
            binding.tvPremiumTitle.setText(R.string.premium_active);
        } else {
            binding.shimmerPremium.startShimmer();
            binding.tvPremiumTitle.setText(R.string.go_premium);
        }
        binding.premiumWatermarkBadge.setVisibility(
                premium ? android.view.View.GONE : android.view.View.VISIBLE);

        binding.switchWatermark.setOnCheckedChangeListener(null);
        showWatermark = premium ? MyApplication.getShowWatermark() : true;
        if (!premium) {
            MyApplication.setShowWatermark(true);
        }
        binding.switchWatermark.setChecked(showWatermark);
        binding.switchWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!PremiumManager.isPremium(this) && !isChecked) {
                buttonView.setChecked(true);
                showPremiumSheet();
                return;
            }
            showWatermark = isChecked;
            MyApplication.setShowWatermark(isChecked);
        });
    }

    private void refreshAdsForPremiumState() {
        if (PremiumManager.isPremium(this)) {
            binding.flNativeSettingsOne.setVisibility(android.view.View.GONE);
            binding.flNativeSettingsTwo.setVisibility(android.view.View.GONE);
            binding.flSettingsBanner.setVisibility(android.view.View.GONE);
        } else {
            loadNativeAds();
            loadBottomBannerAd();
        }
    }

    private void applyDefaultFreeSettings() {
        StampTemplateDefaults.Settings defaults = StampTemplateDefaults.forTemplate(this, 1);
        currentFontStyle = defaults.fontStyle;
        currentDateFormat = defaults.dateFormat;
        currentTimeFormat = defaults.timeFormat;
        currentCombinedFormat = defaults.combinedFormat;
        currentMapType = defaults.mapType;
        fontChanged = true;
        dateTimeChanged = true;
        mapTypeChanged = true;
        showWatermark = true;
        MyApplication.setShowWatermark(true);
    }

    private void showFontStyleDialog() {
        if (fontStyleDialog != null && fontStyleDialog.isShowing()) {
            return;
        }

        String[] fontList = fontViewModel.getFontList().getValue();
        if (fontList == null) {
            fontList = getResources().getStringArray(R.array.font_name_array);
        }

        fontStyleDialog = StampSettingsBottomSheets.showFontStyle(this, fontList, currentFontStyle, new OnFontSelectedListener() {
            @Override
            public void onFontSelected(String fontName, int position) {
                currentFontStyle = fontName;
                fontChanged = true;
                MyApplication.setSessionFontStyle(fontName);
                setSessionResult();
                finish();
            }

            @Override
            public void onDialogDismissed() {
                fontStyleDialog = null;
            }
        });
    }

    private void showDateTimeDialog() {
        if (dateTimeDialog != null && dateTimeDialog.isShowing()) {
            return;
        }
        dateTimeDialog = StampSettingsBottomSheets.showDateTime(this, currentCombinedFormat, new OnDateTimeSelectedListener() {
            @Override
            public void onDateTimeSelected(DateFormatModel selectedFormat, int position) {
                currentDateFormat = selectedFormat.getFormat_Date();
                currentTimeFormat = selectedFormat.getFormat_Time();
                currentCombinedFormat = selectedFormat.getFormat_Combined();
                dateTimeChanged = true;
                MyApplication.setSessionDateTimeFormats(
                        currentDateFormat,
                        currentTimeFormat,
                        currentCombinedFormat);
                setSessionResult();
                finish();
            }

            @Override
            public void onDialogDismissed() {
                dateTimeDialog = null;
            }
        });
    }
    private void showRateUsDialog() {
        if (rateDialog != null && rateDialog.isShowing()) {
            return; // prevent multiple dialogs
        }

        if (MyApplication.isNetworkAvailable(this)) {
            if (!Utils.isRated(this)) {
                rateDialog = new RateDialog(this, false);
                rateDialog.setCancelable(true);

                new HelperClass().setBottomDialog(rateDialog);
                rateDialog.show();

                // when dismissed, reset reference
                rateDialog.setOnDismissListener(dialog -> rateDialog = null);

            } else {
                Toast.makeText(this, getString(R.string.you_have_already_rated_this_application), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.please_check_your_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApplication(Context context) {
        String appPackageName = context.getPackageName();
        String appName = context.getString(R.string.app_name);

        String shareText = String.format("Check out this amazing app!\n\n" + "📸 %s\n" + "⭐ Your photos, your story – now with smart stamps (Map, Date, Time & more)!\n\n" + "📲 Download it from Google Play:\n" + "https://play.google.com/store/apps/details?id=%s", appName, appPackageName);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        context.startActivity(Intent.createChooser(shareIntent, "Share app via"));
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        setSessionResult();

        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "settings_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        }, errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("Settings", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }

    private void setSessionResult() {
        Intent resultIntent = new Intent();
        boolean hasResult = false;
        if (selectedLocation != null) {
            resultIntent.putExtra(MyApplication.EXTRA_LOCATION, selectedLocation);
            hasResult = true;
        }
        if (fontChanged) {
            resultIntent.putExtra(EXTRA_SESSION_FONT_STYLE, currentFontStyle);
            hasResult = true;
        }
        if (dateTimeChanged) {
            resultIntent.putExtra(EXTRA_SESSION_DATE_FORMAT, currentDateFormat);
            resultIntent.putExtra(EXTRA_SESSION_TIME_FORMAT, currentTimeFormat);
            resultIntent.putExtra(EXTRA_SESSION_COMBINED_FORMAT, currentCombinedFormat);
            hasResult = true;
        }
        if (mapTypeChanged) {
            resultIntent.putExtra(EXTRA_SESSION_MAP_TYPE, currentMapType);
            hasResult = true;
        }
        if (hasResult) {
            setResult(Activity.RESULT_OK, resultIntent);
        }
    }
}
