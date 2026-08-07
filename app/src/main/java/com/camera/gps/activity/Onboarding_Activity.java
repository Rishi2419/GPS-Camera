package com.camera.gps.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import me.relex.circleindicator.CircleIndicator3;
import java.util.ArrayList;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.ViewPagerAdapter;
import com.camera.gps.adsmanager.NativeAdManager;
import com.camera.gps.util.Utils;

public class Onboarding_Activity extends InsetAwareActivity {

    ViewPager2 slideViewPager;
    ViewPagerAdapter viewPagerAdapter;
    ArrayList<Integer> arrayList = new ArrayList<>();
    String[] title_list;
    String[] desc_list;
    FrameLayout flNative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        initView();

        FrameLayout flNative = findViewById(R.id.flNative);
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            NativeAdManager.getInstance().loadAndShowNativeAd(
                    this,
                    "onboarding_native",
                    flNative
            );
        }
    }

    private void initView() {
        arrayList.add(R.drawable.onboard_one);
        arrayList.add(R.drawable.onboard_two);
        arrayList.add(R.drawable.onboard_three);
        arrayList.add(R.drawable.onboard_four);

        title_list = new String[]{getString(R.string.intro_title_one),
                getString(R.string.intro_title_two),
                getString(R.string.intro_title_three),
                getString(R.string.intro_title_four)};
        desc_list = new String[]{getString(R.string.intro_desc_one),
                getString(R.string.intro_desc_two),
                getString(R.string.intro_desc_three),
                getString(R.string.intro_desc_four)};

        findViewById(R.id.back_btn).setOnClickListener(view -> {
                if (backwardsliding(0)>0){
                    slideViewPager.setCurrentItem(backwardsliding(1),true);
                }
        });
        findViewById(R.id.tv_next).setOnClickListener(v -> {
            if (forwardsliding(0) < 3) {
                slideViewPager.setCurrentItem(forwardsliding(1), true);
            } else {
                MyApplication.setIsOnBoardingScreen(false);
                startActivity(new Intent(Onboarding_Activity.this, MainActivity.class));
                finish();
            }
        });

        findViewById(R.id.skipButton).setOnClickListener(v -> {
            MyApplication.setIsOnBoardingScreen(false);
            startActivity(new Intent(Onboarding_Activity.this, MainActivity.class));
            finish();
        });

        slideViewPager = findViewById(R.id.slideViewPager);
        viewPagerAdapter = new ViewPagerAdapter(this, arrayList, title_list, desc_list);
        slideViewPager.setAdapter(viewPagerAdapter);
        CircleIndicator3 indicator = findViewById(R.id.dot_indicator);
        indicator.setViewPager(slideViewPager);
        slideViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TextView tvNext = findViewById(R.id.tv_next);
                ImageView backBtn = findViewById(R.id.back_btn);
                if (position == 0){
                    backBtn.setVisibility(GONE);
                }
                else{
                    backBtn.setVisibility(VISIBLE);
                }
                if (position == 3) {
                    tvNext.setText(R.string.getstarted);
                    tvNext.setTextColor(ContextCompat.getColor(Onboarding_Activity.this, R.color.blue_primary));
                } else {
                    tvNext.setText(R.string.next);
                    tvNext.setTextColor(ContextCompat.getColor(Onboarding_Activity.this, R.color.black));
                }
            }
        });

    }

    //Forward sliding
    private int forwardsliding(int i) {
        return slideViewPager.getCurrentItem() + i;
    }

    //Backward sliding
    private int backwardsliding(int i) {
        return slideViewPager.getCurrentItem() - i;
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        startActivity(new Intent(Onboarding_Activity.this, Permissions_Activity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
