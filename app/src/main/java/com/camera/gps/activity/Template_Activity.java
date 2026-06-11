//package com.camera.gps.activity;
//
//import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;
//
//import android.content.Intent;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.appizona.yehiahd.fastsave.FastSave;
//import com.camera.gps.MyApplication;
//import com.camera.gps.R;
//import com.camera.gps.adapter.Template_Adapter;
//import com.camera.gps.adsmanager.InterstitialAdManager;
//import com.camera.gps.databinding.ActivityTemplateBinding;
//import com.camera.gps.util.SP;
//import com.camera.gps.util.Utils;
//
//import java.util.ArrayList;
//
//public class Template_Activity extends AppCompatActivity implements Template_Adapter.TemplateClicksListener {
//
//    private ActivityTemplateBinding binding;
//    int current_bg_alpha;
//    int current_bg_color;
//    int current_date_color;
//    private SP msp;
//    RelativeLayout rel_category_layouts_select;
//    int current_map_type;
//    public static int current_stamp_id;
//    int current_text_color;
//    ArrayList<Integer> arrayList = new ArrayList<>();
//    private Template_Adapter templateAdapter;
//
//    public void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//        SetContentView();
//        msp = new SP(this);
//    }
//
//    private void SetContentView() {
//        binding = ActivityTemplateBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        binding.btBack.setOnClickListener(v -> {
//            onBackPressed();
//        });
//
//        rel_category_layouts_select = binding.layoutRelLayoutSelect;
//
//        current_stamp_id = FastSave.getInstance().getInt(MyApplication.STAMP_LAYOUT_ID, 1);
//
//        arrayList.add(R.drawable.theme1);
//        arrayList.add(R.drawable.theme5);
//        arrayList.add(R.drawable.theme6);
//        arrayList.add(R.drawable.theme4);
//        arrayList.add(R.drawable.theme7);
//        arrayList.add(R.drawable.theme8);
//        arrayList.add(R.drawable.theme9);
//        arrayList.add(R.drawable.theme10);
//        arrayList.add(R.drawable.theme11);
//
//        templateAdapter = new Template_Adapter(arrayList, this, this);
//        binding.viewPagerMain.setAdapter(templateAdapter);
//    }
//
//    @Override
//    public void onThemeClick(int position) {
//        // Just here for interface implementation
//    }
//
//    public void onUseClicked(int position) {
//        current_stamp_id = position + 1;
//        FastSave.getInstance().saveInt(MyApplication.STAMP_LAYOUT_ID, current_stamp_id);
//        templateAdapter.notifyDataSetChanged();
//        msp.setTemplateEdited(this, current_stamp_id, true);
//        onBackPressed();
//    }
//
//    public void onEditClicked(int position) {
//
//        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
//            if (!InterstitialAdManager.isInterstitialShowing()) {
//                setInterstitialShowing(true);
//                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "edit_template_open_interstitial", () -> {
//                    setInterstitialShowing(false);
//                    editNavigation(position);
//                }, errorMsg -> {
//                    setInterstitialShowing(false);
//                    Utils.LogUtils.logE("EditTemplate", "Ad failed: " + errorMsg);
//                });
//            } else {
//                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
//            }
//        } else {
//            editNavigation(position);
//        }
//
//    }
//
//    private void editNavigation(int position) {
//        current_stamp_id = position + 1;
//        FastSave.getInstance().saveInt(MyApplication.TEMP_STAMP_LAYOUT_ID, current_stamp_id);
//        startActivityForResult(new Intent(this, Edit_Activity.class), 100);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK) {
//            // Edit activity returned with success (Use Template was clicked)
//            // Close this Template activity and go back to MainActivity
//            finish();
//        }
//    }
//
//    public void hideOverlay() {
//        templateAdapter.notifyDataSetChanged();
//    }
//
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
//                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "template_close_interstitial",
//                        () -> {
//                            setInterstitialShowing(false);
//                            finish();
//                        },
//                        errorMsg -> {
//                            setInterstitialShowing(false);
//                            Utils.LogUtils.logE("TemplateActivity", "Ad failed: " + errorMsg);
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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adapter.Template_Adapter;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.databinding.ActivityTemplateBinding;
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
        arrayList.add(R.drawable.theme5);
        arrayList.add(R.drawable.theme6);
        arrayList.add(R.drawable.theme4);
        arrayList.add(R.drawable.theme7);
        arrayList.add(R.drawable.theme8);
        arrayList.add(R.drawable.theme9);
        arrayList.add(R.drawable.theme10);
        arrayList.add(R.drawable.theme11);

        templateAdapter = new Template_Adapter(arrayList, this, this);
        binding.viewPagerMain.setAdapter(templateAdapter);
    }

    // NEW METHOD: Get predefined values for each template
    private void initializeTemplateWithPredefinedValues(int templateId) {
        String fontStyle;
        int bgColor, textColor, dateTimeColor, mapType;
        String dateFormat, timeFormat, combinedFormat;

        switch (templateId) {
//            case 1:
//                fontStyle = "SF Pro Display.otf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.WHITE;
//                dateTimeColor = Color.WHITE;
//                mapType = 1; // Normal map
//                dateFormat = "dd-MM-yyyy";
//                timeFormat = "HH:mm:ss a";
//                combinedFormat = "dd-MM-yyyy HH:mm:ss a";
//                break;
//            case 2:
//                fontStyle = "Roboto-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.BLACK;
//                dateTimeColor = Color.BLACK;
//                mapType = 2; // Satellite map
//                dateFormat = "MM/dd/yyyy";
//                timeFormat = "hh:mm a";
//                combinedFormat = "MM/dd/yyyy hh:mm a";
//                break;
//            case 3:
//                fontStyle = "OpenSans-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.BLUE;
//                dateTimeColor = Color.BLUE;
//                mapType = 3; // Terrain map
//                dateFormat = "yyyy-MM-dd";
//                timeFormat = "HH:mm";
//                combinedFormat = "yyyy-MM-dd HH:mm";
//                break;
//            case 4:
//                fontStyle = "Lato-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.RED;
//                dateTimeColor = Color.RED;
//                mapType = 4; // Hybrid map
//                dateFormat = "dd/MM/yyyy";
//                timeFormat = "HH:mm:ss";
//                combinedFormat = "dd/MM/yyyy HH:mm:ss";
//                break;
//            case 5:
//                fontStyle = "Montserrat-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.GREEN;
//                dateTimeColor = Color.GREEN;
//                mapType = 1; // Normal map
//                dateFormat = "MMM dd, yyyy";
//                timeFormat = "hh:mm:ss a";
//                combinedFormat = "MMM dd, yyyy hh:mm:ss a";
//                break;
//            case 6:
//                fontStyle = "SourceSansPro-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.MAGENTA;
//                dateTimeColor = Color.MAGENTA;
//                mapType = 2; // Satellite map
//                dateFormat = "EEEE, MMMM dd, yyyy";
//                timeFormat = "HH:mm a";
//                combinedFormat = "EEEE, MMMM dd, yyyy HH:mm a";
//                break;
//            case 7:
//                fontStyle = "Poppins-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.CYAN;
//                dateTimeColor = Color.CYAN;
//                mapType = 3; // Terrain map
//                dateFormat = "dd.MM.yyyy";
//                timeFormat = "HH:mm:ss";
//                combinedFormat = "dd.MM.yyyy HH:mm:ss";
//                break;
//            case 8:
//                fontStyle = "NunitoSans-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                textColor = Color.YELLOW;
//                dateTimeColor = Color.YELLOW;
//                mapType = 4; // Hybrid map
//                dateFormat = "yyyy/MM/dd";
//                timeFormat = "hh:mm a";
//                combinedFormat = "yyyy/MM/dd hh:mm a";
//                break;
//            case 9:
//                fontStyle = "Inter-Regular.ttf";
//                bgColor = ContextCompat.getColor(this, R.color.bg_glass);
//                textColor = Color.WHITE;
//                dateTimeColor = Color.WHITE;
//                mapType = 1; // Normal map
//                dateFormat = "dd-MMM-yyyy";
//                timeFormat = "HH:mm:ss a";
//                combinedFormat = "dd-MMM-yyyy HH:mm:ss a";
//                break;
            default:
                // Default template values
                fontStyle = "SF Pro Display.otf";
                bgColor = ContextCompat.getColor(this, R.color.transparent_30);
                textColor = Color.WHITE;
                dateTimeColor = Color.WHITE;
                mapType = 1; // Normal map
                dateFormat = "dd-MM-yyyy";
                timeFormat = "HH:mm:ss a";
                combinedFormat = "dd-MM-yyyy HH:mm:ss a";
                break;
        }

        // Save predefined values to SharedPreferences
        msp.setTemplateFontStyle(this, templateId, fontStyle);
        msp.setTemplateBgColor(this, templateId, bgColor);
        msp.setTemplateTextColor(this, templateId, textColor);
        msp.setTemplateDateTimeColor(this, templateId, dateTimeColor);
        msp.setTemplateMapType(this, templateId, mapType);
        msp.setTemplateDateFormat(this, templateId, dateFormat);
        msp.setTemplateTimeFormat(this, templateId, timeFormat);
        msp.setTemplateDateTimeCombinedFormat(this, templateId, combinedFormat);

        // Update global settings with selected template's predefined values
        FastSave.getInstance().saveString(MyApplication.FONT_STYLE, fontStyle);
        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, dateFormat);
        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, timeFormat);
        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, combinedFormat);
        MyApplication.setMapType(mapType);

        Utils.LogUtils.logD("Template_Activity", "Template " + templateId + " initialized with predefined values");
        Utils.LogUtils.logD("Template_Activity", "Font: " + fontStyle + ", DateTime: " + combinedFormat);
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
            // Mark as edited so it uses these values from now on
            msp.setTemplateEdited(this, current_stamp_id, true);
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