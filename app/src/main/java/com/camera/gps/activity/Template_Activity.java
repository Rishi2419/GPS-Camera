package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.Template_Adapter;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.databinding.ActivityTemplateBinding;
import com.camera.gps.model.StampTemplateDefaults;
import com.camera.gps.util.SP;
import com.camera.gps.util.Utils;

import java.util.ArrayList;

public class Template_Activity extends AppCompatActivity implements Template_Adapter.TemplateClicksListener {

    private ActivityTemplateBinding binding;
    int current_bg_alpha;
    int current_bg_color;
    int current_date_color;
    private SP msp;
    RelativeLayout rel_category_layouts_select;
    int current_map_type;
    public static int current_stamp_id;
    int current_text_color;
    ArrayList<Integer> arrayList = new ArrayList<>();
    private Template_Adapter templateAdapter;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SetContentView();
        msp = new SP(this);
    }

    private void SetContentView() {
        binding = ActivityTemplateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btBack.setOnClickListener(v -> {
            onBackPressed();
        });

        rel_category_layouts_select = binding.layoutRelLayoutSelect;

        current_stamp_id = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);

        arrayList.add(R.drawable.theme1);
        arrayList.add(R.drawable.theme2);
        arrayList.add(R.drawable.theme3);
        arrayList.add(R.drawable.theme4);
        arrayList.add(R.drawable.theme5);
        arrayList.add(R.drawable.theme6);
        arrayList.add(R.drawable.theme7);
        arrayList.add(R.drawable.theme8);
        arrayList.add(R.drawable.theme9);

        templateAdapter = new Template_Adapter(arrayList, this, this);
        binding.viewPagerMain.setAdapter(templateAdapter);
    }

    // NEW METHOD: Get predefined values for each template
    private void initializeTemplateWithPredefinedValues(int templateId) {
        StampTemplateDefaults.Settings settings = StampTemplateDefaults.forTemplate(this, templateId);

        // Save predefined values to SharedPreferences
        msp.setTemplateFontStyle(this, templateId, settings.fontStyle);
        msp.setTemplateBgColor(this, templateId, settings.bgColor);
        msp.setTemplateTextColor(this, templateId, settings.textColor);
        msp.setTemplateDateTimeColor(this, templateId, settings.dateTimeColor);
        msp.setTemplateMapType(this, templateId, settings.mapType);
        msp.setTemplateDateFormat(this, templateId, settings.dateFormat);
        msp.setTemplateTimeFormat(this, templateId, settings.timeFormat);
        msp.setTemplateDateTimeCombinedFormat(this, templateId, settings.combinedFormat);

        // Update global settings with selected template's predefined values
        FastSave.getInstance().saveString(MyApplication.FONT_STYLE, settings.fontStyle);
        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, settings.dateFormat);
        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, settings.timeFormat);
        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, settings.combinedFormat);
        MyApplication.setMapType(settings.mapType);

        Utils.LogUtils.logD("Template_Activity", "Template " + templateId + " initialized with predefined values");
        Utils.LogUtils.logD("Template_Activity", "Font: " + settings.fontStyle + ", DateTime: " + settings.combinedFormat);
    }

    @Override
    public void onThemeClick(int position) {
        // Just here for interface implementation
    }

    public void onUseClicked(int position) {
        current_stamp_id = position + 1;
        FastSave.getInstance().saveInt(MyApplication.STAMP_LAYOUT_ID, current_stamp_id);

        // Check if template has been edited/used before
        boolean isTemplateEdited = msp.isTemplateEdited(this, current_stamp_id);

        if (!isTemplateEdited) {
            // Template NEVER used before - initialize with predefined values for FIRST TIME ONLY
            initializeTemplateWithPredefinedValues(current_stamp_id);
            msp.setTemplateEdited(this, current_stamp_id, false);
            Utils.LogUtils.logD("Template_Activity", "Template " + current_stamp_id + " - FIRST TIME USE, initialized with predefined values");
        } else {
            // Template used/edited before - load existing saved values (could be predefined or user-modified)
            String savedFont = msp.getTemplateFontStyle(this, current_stamp_id, "SF Pro Display.otf");
            String savedDateFormat = msp.getTemplateDateFormat(this, current_stamp_id, "dd-MM-yyyy");
            String savedTimeFormat = msp.getTemplateTimeFormat(this, current_stamp_id, "HH:mm:ss a");
            String savedCombinedFormat = msp.getTemplateDateTimeCombinedFormat(this, current_stamp_id, "dd-MM-yyyy HH:mm:ss a");
            int savedMapType = msp.getTemplateMapType(this, current_stamp_id, 1);

            // Update global settings with saved template values
            FastSave.getInstance().saveString(MyApplication.FONT_STYLE, savedFont);
            FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, savedDateFormat);
            FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, savedTimeFormat);
            FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, savedCombinedFormat);
            MyApplication.setMapType(savedMapType);

            Utils.LogUtils.logD("Template_Activity", "Template " + current_stamp_id + " - REUSING template, loaded saved values (predefined or user-modified)");
        }

        templateAdapter.notifyDataSetChanged();
        setResult(RESULT_OK);
        onBackPressed();
    }

    public void onEditClicked(int position) {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "edit_template_open_interstitial", () -> {
                    setInterstitialShowing(false);
                    editNavigation(position);
                }, errorMsg -> {
                    setInterstitialShowing(false);
                    Utils.LogUtils.logE("EditTemplate", "Ad failed: " + errorMsg);
                });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            editNavigation(position);
        }
    }

    private void editNavigation(int position) {
        current_stamp_id = position + 1;

        // Check if template has been edited before
        boolean isTemplateEdited = msp.isTemplateEdited(this, current_stamp_id);

        if (!isTemplateEdited) {
            // Template not edited before, initialize with predefined values BEFORE opening edit screen
            initializeTemplateWithPredefinedValues(current_stamp_id);
            // DON'T mark as edited yet - let Edit_Activity handle this when user actually saves
            Utils.LogUtils.logD("Template_Activity", "Template " + current_stamp_id + " - First time edit, initialized with predefined values");
        }

        FastSave.getInstance().saveInt(MyApplication.TEMP_STAMP_LAYOUT_ID, current_stamp_id);
        startActivityForResult(new Intent(this, Edit_Activity.class), 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Edit activity returned with success (Save Template was clicked)
            // Close this Template activity and go back to MainActivity
            setResult(RESULT_OK);
            finish();
        }
    }

    public void hideOverlay() {
        templateAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "template_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("TemplateActivity", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }
}
