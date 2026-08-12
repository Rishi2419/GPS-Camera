package com.camera.gps.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camera.gps.R;
import com.camera.gps.adapter.Language_Adapter;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.listener.LanguageListener;
import com.camera.gps.util.Utils;
import com.camera.gps.viewmodel.LanguageSelectViewModel;
import com.camera.gps.MyApplication;

import java.util.Locale;

public class Language_Activity extends InsetAwareActivity implements LanguageListener {
    public static final String EXTRA_IS_SETTING = "is_setting";

    //Flow
//    App Starts ➝ Language_Activity opens ➝ ViewModel loads language list ➝
//    RecyclerView shows all language cards ➝ User taps a language ➝
//    Adapter notifies Activity via LanguageListener ➝ Activity updates ViewModel ➝
//    ViewModel updates LiveData ➝ Adapter refreshes selected UI ➝
//    User taps "Save" ➝ ViewModel saves language ➝ Starts next activity

    private LanguageSelectViewModel viewModel;
    private Language_Adapter languageAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LanguageSelectViewModel.class);

        Locale locale = new Locale(MyApplication.getLanguageCode());
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
        MyApplication.setLanguageCode(MyApplication.getLanguageCode());

        setContentView(R.layout.activity_language);

        // Set up observers
        setupObservers();

        // Initialize UI
        initializeUI();

    }

    private void setupObservers() {
        // Observe language list changes
        viewModel.getLanguageListLiveData().observe(this, languageList -> {
            if (languageAdapter != null) {
                languageAdapter.notifyDataSetChanged();
            }
        });

        // Observe selected language code changes
        viewModel.getSelectedLanguageCodeLiveData().observe(this, selectedLanguageCode -> {
            if (languageAdapter != null) {
                languageAdapter.setSelectedLanguageCode(selectedLanguageCode);
                languageAdapter.notifyDataSetChanged();
            }
        });

        // Observe back button visibility
        viewModel.getShouldShowBackButtonLiveData().observe(this, shouldShow -> {
            findViewById(R.id.imgBackLanguage).setVisibility(shouldShow ? VISIBLE : GONE);
        });

        // Observe navigation events
        viewModel.getNavigationLiveData().observe(this, intent -> {
            if (intent != null) {
                startActivity(intent);
            }
        });

        // Observe finish activity events
        viewModel.getFinishActivityLiveData().observe(this, shouldFinish -> {
            if (shouldFinish) {
                finish();
            }
        });
    }

    private void initializeUI() {
        // Set isSetting flag from intent
        boolean isSetting = getIntent().getBooleanExtra(EXTRA_IS_SETTING, false);
        viewModel.setIsSetting(isSetting);

        // Setup RecyclerView
        RecyclerView rcyLanguage = findViewById(R.id.select_lan_recyclerview);
        rcyLanguage.setLayoutManager(new GridLayoutManager(this, 2));

        // Initialize adapter with data from ViewModel
        languageAdapter = new Language_Adapter(this, viewModel.getLanguageListLiveData().getValue(), viewModel.getSelectedLanguageCodeLiveData().getValue());
        rcyLanguage.setAdapter(languageAdapter);
        languageAdapter.setLanguageListener(this);

        // Setup click listeners
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            viewModel.onSaveButtonClicked(this);
            MyApplication.setIsLanguage(true);
        });
        findViewById(R.id.imgBackLanguage).setOnClickListener(view -> onBackPressed());
    }

    @Override
    public void language(String code) {
        viewModel.onLanguageSelected(code);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ViewModel will handle cleanup in onCleared()
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "language_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("Language", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }
}
