package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.camerax.util.SharedPrefsSettings;
import com.camera.gps.database.entity.MyLocation;
import com.camera.gps.databinding.ActivitySettingsBinding;
import com.camera.gps.dialogs.DateTimeDialog;
import com.camera.gps.dialogs.FontStyleDialog;
import com.camera.gps.dialogs.RateDialog;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.listener.OnFontSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.repositories.DateFormatRepository;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SP;
import com.camera.gps.util.Utils;
import com.camera.gps.viewmodel.FontStyleViewModel;

public class Settings_Activity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FontStyleDialog fontStyleDialog;
    private FontStyleViewModel fontViewModel;
    private DateTimeDialog dateTimeDialog;
    private RateDialog rateDialog;
    private MyLocation selectedLocation;
    private ActivityResultLauncher locationLauncher;

    private boolean showWatermark;
    private SP msp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.hasExtra(MyApplication.EXTRA_LOCATION)) {
                            selectedLocation = (MyLocation) data.getSerializableExtra(MyApplication.EXTRA_LOCATION);
                        }
                    }
                }
        );

        fontViewModel = new ViewModelProvider(this).get(FontStyleViewModel.class);
        msp = new SP(this);


        binding.shimmerPremium.startShimmer();

        setupListeners();
        setupSwitches();
    }

    private void setupListeners() {
        binding.layoutMap.setOnClickListener(v -> {

            if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
                if (!InterstitialAdManager.isInterstitialShowing()) {
                    setInterstitialShowing(true);
                    InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "map_open_from_setting_interstitial", () -> {
                        setInterstitialShowing(false);
                        startActivity(new Intent(this, Map_Activity.class));
                    }, errorMsg -> {
                        setInterstitialShowing(false);
                        Utils.LogUtils.logE("Map_fromSettings", "Ad failed: " + errorMsg);
                    });
                } else {
                    Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                }
            } else {
                startActivity(new Intent(this, Map_Activity.class));
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
                                startActivity(new Intent(this, Language_Activity.class));
                            }, errorMsg -> {
                                setInterstitialShowing(false);
                                Utils.LogUtils.logE("Language", "Ad failed: " + errorMsg);
                            });
                        } else {
                            Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
                        }
                    } else {
                        startActivity(new Intent(this, Language_Activity.class));
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

        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void myLocationNavigation() {
        MyLocation receivedLocation = (MyLocation) getIntent().getSerializableExtra(MyApplication.EXTRA_LOCATION);
        Intent intent = new Intent(Settings_Activity.this, MyLocation_Activity.class);
        intent.putExtra(MyApplication.EXTRA_LOCATION, receivedLocation);

        intent.putExtra("SOURCE", "SETTINGS");
       // startActivity(intent);
        locationLauncher.launch(intent);
    }

    private void setupSwitches() {
        // Watermark
        showWatermark = MyApplication.getShowWatermark();
        binding.switchWatermark.setChecked(showWatermark);
        binding.switchWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showWatermark = isChecked;
            MyApplication.setShowWatermark(isChecked);
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

    private void showFontStyleDialog() {
        if (fontStyleDialog != null && fontStyleDialog.isShowing()) {
            return;
        }

        String[] fontList = fontViewModel.getFontList().getValue();
        if (fontList == null) {
            fontList = getResources().getStringArray(R.array.font_name_array);
        }

        fontStyleDialog = new FontStyleDialog(this, fontList, new OnFontSelectedListener() {
            @Override
            public void onFontSelected(String fontName, int position) {
                msp.setInteger(getApplicationContext(), SP.LOCATION_FONT_POSITION, position);
                FastSave.getInstance().saveString(MyApplication.FONT_STYLE, fontViewModel.getFontList().getValue()[position]);
            }

            @Override
            public void onDialogDismissed() {
                fontStyleDialog = null;
            }
        });

        new HelperClass().setBottomDialog(fontStyleDialog);
        fontStyleDialog.show();
    }

    private void showDateTimeDialog() {
        if (dateTimeDialog != null && dateTimeDialog.isShowing()) {
            return;
        }
        DateTimeDialog dateTimeDialog = new DateTimeDialog(this, new OnDateTimeSelectedListener() {
            @Override
            public void onDateTimeSelected(DateFormatModel selectedFormat, int position) {
                // Save the selected format
                DateFormatRepository repo = new DateFormatRepository(Settings_Activity.this);
                repo.saveSelectedFormat(selectedFormat.getFormat_Combined(), selectedFormat.getFormat_Date(), selectedFormat.getFormat_Time());
            }

            @Override
            public void onDialogDismissed() {
                // Optional: do something when dialog is closed
            }
        });

        // Pass currently active format so the dialog highlights it
        String currentFormat = new DateFormatRepository(this).getCurrentFormat();
        dateTimeDialog.setCurrentActiveFormat(currentFormat);

        new HelperClass().setBottomDialog(dateTimeDialog);
        dateTimeDialog.show();
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
    public void onBackPressed() {

        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(MyApplication.EXTRA_LOCATION, selectedLocation);
            setResult(Activity.RESULT_OK, resultIntent);
        }


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
}
