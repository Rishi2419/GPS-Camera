//package com.camera.gps.activity;
//
//import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;
//
//import android.app.Application;
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.camera.gps.MyApplication;
//import com.camera.gps.R;
//import com.camera.gps.adapter.CreationViewPagerAdapter;
//import com.camera.gps.adsmanager.InterstitialAdManager;
//import com.camera.gps.data.GlobalViewModel;
//import com.camera.gps.data.GlobalViewModelFactory;
//import com.camera.gps.database.entity.Photo;
//import com.camera.gps.databinding.ActivityMyCreationBinding;
//import com.camera.gps.util.Utils;
//import com.google.android.material.tabs.TabLayoutMediator;
//
//import java.util.List;
//
//public class MyCreation_Activity extends AppCompatActivity {
//
//    private ActivityMyCreationBinding binding;
//    private final ActivityResultLauncher<Intent> resultLauncher;
//    private GlobalViewModel viewModel;
//    private CreationViewPagerAdapter viewPagerAdapter;
//
//    public MyCreation_Activity() {
//        this.resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() {
//            @Override
//            public void onActivityResult(Object obj) {
//                if (((ActivityResult) obj).getResultCode() == -1) {
//                    getPhotos();
//                }
//            }
//        });
//    }
//
//
//    @Override
//    public void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//        binding = ActivityMyCreationBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        initViews();
//        initViewModel();
//        setupViewPager();
//        getPhotos();
//
//        // Check if we need to select a specific tab
//        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
//        if (selectedTab >= 0 && selectedTab < 2) {
//            binding.viewPager.setCurrentItem(selectedTab, false);
//        }
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        // Handle the case when activity is already running and new intent arrives
//        int selectedTab = intent.getIntExtra("selectedTab", -1);
//        if (selectedTab >= 0 && selectedTab < 2) {
//            binding.viewPager.setCurrentItem(selectedTab, false);
//        }
//    }
//
//    private void initViews() {
//        binding.btBack.setOnClickListener(view -> onBackPressed());
//    }
//
//    private void initViewModel() {
//        Application application = getApplication();
//        this.viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(application))
//                .get(GlobalViewModel.class);
//    }
//
//    private void setupViewPager() {
//        viewPagerAdapter = new CreationViewPagerAdapter(this);
//        binding.viewPager.setAdapter(viewPagerAdapter);
//
//        // Setup TabLayout with ViewPager2
//        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
//            switch (position) {
//                case 0:
//                    tab.setText(getString(R.string.photos));
//                    break;
//                case 1:
//                    tab.setText(getString(R.string.videos));
//                    break;
//            }
//        }).attach();
//    }
//
//    private void getPhotos() {
//        this.viewModel.getAllPhoto().observe(this, new Observer<List<Photo>>() {
//            @Override
//            public void onChanged(List<Photo> photos) {
//                if (photos != null) {
//                    viewPagerAdapter.updateData(photos);
//                }
//            }
//        });
//    }
//
//    public ActivityResultLauncher<Intent> getResultLauncher() {
//        return this.resultLauncher;
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
//            if (!InterstitialAdManager.isInterstitialShowing()) {
//                setInterstitialShowing(true);
//                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "previewgallery_close_interstitial",
//                        () -> {
//                            setInterstitialShowing(false);
//                            finish();
//                        },
//                        errorMsg -> {
//                            setInterstitialShowing(false);
//                            Utils.LogUtils.logE("MyCreation", "Ad failed: " + errorMsg);
//                        });
//            } else {
//                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
//            }
//        } else {
//            finish();
//        }
//    }
//}


package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.CreationViewPagerAdapter;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.adsmanager.admob.AdMobBannerAdHelper;
import com.camera.gps.data.GlobalViewModel;
import com.camera.gps.data.GlobalViewModelFactory;
import com.camera.gps.database.entity.Photo;
import com.camera.gps.databinding.ActivityMyCreationBinding;
import com.camera.gps.util.Utils;
import com.camera.gps.util.SharedMediaStore;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MyCreation_Activity extends InsetAwareActivity implements CreationViewPagerAdapter.OnSelectionChangeListener {

    private ActivityMyCreationBinding binding;
    private final ActivityResultLauncher<Intent> resultLauncher;
    private GlobalViewModel viewModel;
    private CreationViewPagerAdapter viewPagerAdapter;
    private boolean isSelectionMode = false;
    private Dialog dialog;

    public MyCreation_Activity() {
        this.resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() {
            @Override
            public void onActivityResult(Object obj) {
                if (((ActivityResult) obj).getResultCode() == -1) {
                    getPhotos();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = ActivityMyCreationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initViewModel();
        setupViewPager();
        loadBottomBannerAd();

        // Check if we need to select a specific tab
        int selectedTab = getIntent().getIntExtra("selectedTab", 0);
        if (selectedTab >= 0 && selectedTab < 2) {
            binding.viewPager.setCurrentItem(selectedTab, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPhotos();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle the case when activity is already running and new intent arrives
        int selectedTab = intent.getIntExtra("selectedTab", -1);
        if (selectedTab >= 0 && selectedTab < 2) {
            binding.viewPager.setCurrentItem(selectedTab, false);
        }
    }

    private void initViews() {
        binding.btBack.setOnClickListener(view -> onBackPressed());


        binding.btDelete.setOnClickListener(view -> {
            if (!isSelectionMode) {
                // Enter selection mode
                enterSelectionMode();
            } else {
                // Check if items are selected
                List<Photo> selectedItems = viewPagerAdapter.getSelectedItems();
                if (selectedItems.isEmpty()) {
                    // Exit selection mode
                    exitSelectionMode();
                } else {
                    // Show delete confirmation dialog
                    showDeleteDialog(selectedItems);
                }
            }
        });
    }

    private void enterSelectionMode() {
        isSelectionMode = true;
        viewPagerAdapter.enterSelectionMode();
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        viewPagerAdapter.clearSelection();
        viewPagerAdapter.exitSelectionMode();
    }

    private void showDeleteDialog(List<Photo> selectedItems) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_item);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
        }

        TextView title = dialog.findViewById(R.id.tvDeleteTitle);
        TextView message = dialog.findViewById(R.id.tvDeleteMessage);
        TextView btnDelete = dialog.findViewById(R.id.btnDelete);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        // Dynamically set text based on selection count and type
        int selectedCount = selectedItems.size();
        boolean hasVideos = false;
        boolean hasPhotos = false;

        for (Photo photo : selectedItems) {
            if (photo.getImagePath() != null) {
                if (SharedMediaStore.isVideo(photo)) {
                    hasVideos = true;
                } else {
                    hasPhotos = true;
                }
            }
        }

        if (selectedCount == 1) {
            // Single item
            if (hasVideos) {
                title.setText(getString(R.string.delete_this_video));
                message.setText(getString(R.string.delete_video_message));
            } else {
                title.setText(getString(R.string.delete_this_image));
                message.setText(getString(R.string.delete_image_message));
            }
        }else {
            // Multiple items
            if (hasVideos && hasPhotos) {
                title.setText(getString(R.string.delete_multiple_mixed_title, selectedCount));
                message.setText(getString(R.string.delete_multiple_mixed_message, selectedCount));
            } else if (hasVideos) {
                title.setText(getString(R.string.delete_multiple_videos_title, selectedCount));
                message.setText(getString(R.string.delete_multiple_videos_message, selectedCount));
            } else {
                title.setText(getString(R.string.delete_multiple_photos_title, selectedCount));
                message.setText(getString(R.string.delete_multiple_photos_message, selectedCount));
            }
        }

        btnDelete.setOnClickListener(v -> {
            delete(selectedItems);
            dialog.dismiss();
            exitSelectionMode();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void delete(List<Photo> photoList) {
        List<Photo> deletedItems = new ArrayList<>();
        for (Photo photo : photoList) {
            if (SharedMediaStore.delete(this, photo)) {
                deletedItems.add(photo);
            }
        }

        if (deletedItems.isEmpty()) {
            Toast.makeText(this, "Unable to delete selected media", Toast.LENGTH_SHORT).show();
            getPhotos();
            return;
        }

        viewModel.deletePhotos(deletedItems).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer result) {
                if (result > 0) {
                    getPhotos();
                }
            }
        });
    }

    private void initViewModel() {
        Application application = getApplication();
        this.viewModel = new ViewModelProvider(this, new GlobalViewModelFactory(application))
                .get(GlobalViewModel.class);
    }

    private void setupViewPager() {
        viewPagerAdapter = new CreationViewPagerAdapter(this);
        viewPagerAdapter.setOnSelectionChangeListener(this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        binding.viewPager.setOffscreenPageLimit(2);

        // Setup TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.photos));
                    break;
                case 1:
                    tab.setText(getString(R.string.videos));
                    break;
            }
        }).attach();
    }

    private void getPhotos() {
        LiveData<List<Photo>> source = this.viewModel.getAllPhoto();
        source.observe(this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(List<Photo> photos) {
                source.removeObserver(this);
                if (photos != null) {
                    List<Photo> validPhotos = new ArrayList<>();
                    List<Photo> missingPhotos = new ArrayList<>();
                    for (Photo photo : photos) {
                        if (SharedMediaStore.exists(MyCreation_Activity.this,
                                photo.getMediaUri(), photo.getImagePath())) {
                            photo.setImagePath(SharedMediaStore.resolveCurrentPath(
                                    MyCreation_Activity.this,
                                    photo.getMediaUri(),
                                    photo.getImagePath()));
                            validPhotos.add(photo);
                        } else {
                            missingPhotos.add(photo);
                        }
                    }
                    viewPagerAdapter.updateData(validPhotos);
                    if (!missingPhotos.isEmpty()) {
                        for (Photo missingPhoto : missingPhotos) {
                            SharedMediaStore.delete(MyCreation_Activity.this, missingPhoto);
                        }
                        viewModel.deletePhotos(missingPhotos);
                    }
                }
            }
        });
    }

    private void loadBottomBannerAd() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            binding.flNative.post(() -> AdMobBannerAdHelper.loadBannerAd(this, binding.flNative, "my_creation_banner"));
        } else {
            binding.flNative.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public void onSelectionChanged(boolean hasSelection) {
        if (hasSelection && !isSelectionMode) {
            // Show delete button when items are selected
            binding.btDelete.setVisibility(android.view.View.VISIBLE);
        } else if (!hasSelection && !isSelectionMode) {
            // Hide delete button when no items are selected and not in selection mode
//            binding.btDelete.setVisibility(android.view.View.GONE);
        }
    }


    public ActivityResultLauncher<Intent> getResultLauncher() {
        return this.resultLauncher;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        if (isSelectionMode) {
            // Exit selection mode on back press
            exitSelectionMode();
            return;
        }

        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "previewgallery_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("MyCreation", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }
}
