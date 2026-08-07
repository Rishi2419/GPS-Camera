package com.camera.gps.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.camera.gps.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends InsetAwareActivity {

    private static final String PRIVACY_POLICY_URL =
            "https://gps-camera-privacypolicy.vercel.app/";

    private ActivityPrivacyPolicyBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WebSettings settings = binding.privacyPolicyWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        binding.privacyPolicyWebView.setWebViewClient(new WebViewClient());
        binding.privacyPolicyWebView.loadUrl(PRIVACY_POLICY_URL);
    }

    @Override
    public void onBackPressed() {
        if (binding != null && binding.privacyPolicyWebView.canGoBack()) {
            binding.privacyPolicyWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (binding != null) {
            WebView webView = binding.privacyPolicyWebView;
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.destroy();
            binding = null;
        }
        super.onDestroy();
    }
}
