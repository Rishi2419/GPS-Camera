package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.camera.gps.MyApplication;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.admob.AdMobBannerAdHelper;
import com.camera.gps.databinding.ActivityMyLocationBinding;
import com.camera.gps.util.Utils;
import com.google.android.material.tabs.TabLayoutMediator;

import com.camera.gps.R;
import com.camera.gps.fragment.AddCustomLocationFragment;
import com.camera.gps.fragment.SavedLocationFragment;

public final class MyLocation_Activity extends InsetAwareActivity {
    public static final String EXTRA_ACTIVE_SAVED_LOCATION_ID =
            "com.camera.gps.extra.ACTIVE_SAVED_LOCATION_ID";

    private ActivityMyLocationBinding binding;
    private String source;
    private ViewPagerAdapter pagerAdapter;
    private SavedLocationFragment savedLocationFragment;

    // Interfaces for fragment communication
    public interface OnDeleteClickListener {
        void onDeleteClick();
    }

    public interface OnSearchClickListener {
        void onSearchClick();
    }

    private OnDeleteClickListener deleteClickListener;
    private OnSearchClickListener searchClickListener;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = ActivityMyLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        source = getIntent().getStringExtra("SOURCE");
        setupClickListeners();
        setupViewPager();
        loadBottomBannerAd();
    }

    private void loadBottomBannerAd() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            binding.flNative.post(() -> AdMobBannerAdHelper.loadBannerAd(this, binding.flNative, "saved_location_banner"));
        } else {
            binding.flNative.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.btBack.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.btnDelete.setOnClickListener(view -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick();
            }
        });

        binding.btnSearch.setOnClickListener(view -> {
            if (searchClickListener != null) {
                searchClickListener.onSearchClick();
            }
        });

        binding.btnSetCurrentLocation.setOnClickListener(view -> {
            MyApplication.set_to_current_location = true;
            onBackPressed();
        });

        binding.header.post(this::updateSetCurrentLocationMaxWidth);
    }

    private void updateSetCurrentLocationMaxWidth() {
        android.view.ViewGroup.MarginLayoutParams params =
                (android.view.ViewGroup.MarginLayoutParams) binding.btnSetCurrentLocation.getLayoutParams();
        boolean isRtl = binding.header.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int availableWidth = isRtl
                ? binding.tvHeaderTitle.getLeft() - params.getMarginStart() - params.getMarginEnd()
                : binding.header.getWidth() - binding.tvHeaderTitle.getRight()
                - params.getMarginStart() - params.getMarginEnd();
        binding.btnSetCurrentLocation.setMaxWidth(Math.max(0, availableWidth));
    }

    public void setDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setSearchClickListener(OnSearchClickListener listener) {
        this.searchClickListener = listener;
    }

    public String getSource() {
        return source;
    }

    public void showSavedLocations() {
        binding.viewPager.setCurrentItem(1, true);
    }

    private void setupViewPager() {
        pagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        //Disabling the viewpager sliding
        binding.viewPager.setUserInputEnabled(false);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.custom_location));
                    break;
                case 1:
                    tab.setText(getString(R.string.saved_location));
                    break;
            }
        }).attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 1) { // SavedLocationFragment
                    binding.btnDelete.setVisibility(View.VISIBLE);
                    binding.btnSearch.setVisibility(View.VISIBLE);
                    binding.btnSetCurrentLocation.setVisibility(View.GONE);
                } else { // AddCustomLocationFragment
                    binding.btnDelete.setVisibility(View.GONE);
                    binding.btnSearch.setVisibility(View.GONE);
                    binding.btnSetCurrentLocation.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AddCustomLocationFragment();
                case 1:
                    return new SavedLocationFragment();
                default:
                    return new AddCustomLocationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "location_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("MyLocation", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }
}
