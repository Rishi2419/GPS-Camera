//package com.camera.gps.activity;
//
//import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;
//
//import android.app.Dialog;
//import android.graphics.Color;
//import android.graphics.Typeface;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.GradientDrawable;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.appizona.yehiahd.fastsave.FastSave;
//import com.camera.gps.MyApplication;
//import com.camera.gps.R;
//import com.camera.gps.adsmanager.InterstitialAdManager;
//import com.camera.gps.databinding.ActivityEditBinding;
//import com.camera.gps.dialogs.DateTimeDialog;
//import com.camera.gps.dialogs.FontStyleDialog;
//import com.camera.gps.dialogs.MapTypeDialog;
//import com.camera.gps.dialogs.ColorDialog;
//import com.camera.gps.dialogs.TemplateDateTimeDialog;
//import com.camera.gps.listener.OnDateTimeSelectedListener;
//import com.camera.gps.listener.OnFontSelectedListener;
//import com.camera.gps.listener.OnMapTypeSelectedListener;
//import com.camera.gps.model.DateFormatModel;
//import com.camera.gps.util.HelperClass;
//import com.camera.gps.util.SP;
//import com.camera.gps.util.Utils;
//import com.camera.gps.viewmodel.FontStyleViewModel;
//
//public class Edit_Activity extends AppCompatActivity {
//
//    private ActivityEditBinding binding;
//    private int tempstamp_type;
//    private int current_stamp_id;
//    private FontStyleDialog fontStyleDialog;
//    private FontStyleViewModel fontViewModel;
//    private TemplateDateTimeDialog templateDateTimeDialog;
//
//    private ColorDialog stampBgColorDialog;
//    private ColorDialog textColorDialog;
//    private ColorDialog stampDateTimeColorDialog;
//    private MapTypeDialog mapTypeDialog;
//
//    int current_map_type;
//    private String currentFontStyle = "SF Pro Display.otf";
//    private int currentFontPosition = 0;
//
//    private int currentBgColor;
//    private int currentTextColor;
//    private int currentDateTimeColor;
//    private Dialog dialog;
//    private String currentDateFormat = "";
//    private String currentTimeFormat = "";
//    private String currentCombinedFormat = "";
//    private int currentDateTimePosition = 0;
//
//    private SP msp;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityEditBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        fontViewModel = new ViewModelProvider(this).get(FontStyleViewModel.class);
//
//        msp = new SP(this);
//
//        setupViews();
//        getStampType();
//        setStampLayout();
//        loadTemplateSettings();
//        updatePreviewUI(); // Update UI after loading settings
//    }
//
//    private void setupViews() {
//        binding.btBack.setOnClickListener(v -> {
//            onBackPressed();
//        });
//
//        binding.btnSaveSettings.setOnClickListener(v -> {
//            saveTemplate();
//        });
//
//        binding.btnResetSettings.setOnClickListener(v -> {
//            if (dialog != null && dialog.isShowing()) {
//                return;
//            }
//
//            dialog = new Dialog(this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.dialog_delete_item);
//
//            Window window = dialog.getWindow();
//            if (window != null) {
//                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                window.setGravity(Gravity.BOTTOM);
//            }
//
//            TextView title = dialog.findViewById(R.id.tvDeleteTitle);
//            TextView message = dialog.findViewById(R.id.tvDeleteMessage);
//            TextView btnDelete = dialog.findViewById(R.id.btnDelete);
//            TextView btnCancel = dialog.findViewById(R.id.btnCancel);
//
//            title.setText(getString(R.string.reset_this_template));
//            message.setText(getString(R.string.reset_template_message));
//            btnDelete.setText(getString(R.string.reset));
//
//
//            btnDelete.setOnClickListener(view -> {
//                resetTemplate();
//                dialog.dismiss();
//            });
//
//            btnCancel.setOnClickListener(view -> dialog.dismiss());
//
//            dialog.show();
//        });
//
//        binding.layoutFontStyle.setOnClickListener(v -> {
//            showFontStyleDialog();
//        });
//
//        binding.layoutDateTimeFormat.setOnClickListener(v -> {
//            showDateTimeDialog();
//        });
//
//        binding.layoutBgColor.setOnClickListener(v -> {
//            showBgColorDialog();
//        });
//
//        binding.layoutTextColor.setOnClickListener(v -> {
//            showTextColorDialog();
//        });
//
//        binding.layoutDateTimeColor.setOnClickListener(v -> {
//            showDateTimeColorDialog();
//        });
//
//        binding.layoutMapType.setOnClickListener(this::showMapType);
//    }
//
//    private void loadTemplateSettings() {
//        String defaultFont = FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf");
//        currentFontStyle = msp.getTemplateFontStyle(this, current_stamp_id, defaultFont);
//
//        String[] fontList = getResources().getStringArray(R.array.font_name_array);
//
//        // Find the position of the current font in the array
//        currentFontPosition = 0; // fallback to first font if not found
//        for (int i = 0; i < fontList.length; i++) {
//            if (fontList[i].equalsIgnoreCase(currentFontStyle)) {
//                currentFontPosition = i;
//                break;
//            }
//        }
//
//        int defaultMapType = MyApplication.getMapType();
//        current_map_type = msp.getTemplateMapType(this, current_stamp_id, defaultMapType);
//        Log.d("Edit_Activity_Rishi_Map", "Loaded MapType: " + current_map_type);
//
//        int defaultColor = ContextCompat.getColor(this, R.color.transparent_30);
//        currentBgColor = msp.getTemplateBgColor(this, current_stamp_id, defaultColor);
//        Log.d("Edit_Activity_Rishi", "Loaded BG Color: #" + Integer.toHexString(currentBgColor));
//
//        // Load saved text color, fallback to white
//        int defaultTextColor = Color.WHITE;
//        currentTextColor = msp.getTemplateTextColor(this, current_stamp_id, defaultTextColor);
//        Log.d("Edit_Activity_Rishi", "Loaded Text Color: #" + Integer.toHexString(currentTextColor));
//
//        // Load saved DateTime color, fallback to white
//        int defaultDateTimeColor = Color.WHITE;
//        currentDateTimeColor = msp.getTemplateDateTimeColor(this, current_stamp_id, defaultDateTimeColor);
//        Log.d("Edit_Activity_Rishi", "Loaded DateTime Color: #" + Integer.toHexString(currentDateTimeColor));
//
//        // LOAD DATETIME FORMATS FIRST - BEFORE finding position
//        String defaultDateFormat = FastSave.getInstance().getString(MyApplication.FORMAT_DATE, "dd-MM-yyyy");
//        String defaultTimeFormat = FastSave.getInstance().getString(MyApplication.FORMAT_TIME, "HH:mm:ss a");
//        String defaultCombinedFormat = FastSave.getInstance().getString(MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
//
//        // Get template-specific formats or use defaults
//        currentDateFormat = msp.getTemplateDateFormat(this, current_stamp_id, defaultDateFormat);
//        currentTimeFormat = msp.getTemplateTimeFormat(this, current_stamp_id, defaultTimeFormat);
//        currentCombinedFormat = msp.getTemplateDateTimeCombinedFormat(this, current_stamp_id, defaultCombinedFormat);
//
//        Log.d("Edit_Activity_DateTime", "Loaded Template DateTime Formats:");
//        Log.d("Edit_Activity_DateTime", "Date: " + currentDateFormat);
//        Log.d("Edit_Activity_DateTime", "Time: " + currentTimeFormat);
//        Log.d("Edit_Activity_DateTime", "Combined: " + currentCombinedFormat);
//
//        // NOW find the position AFTER loading the actual format
//        currentDateTimePosition = 0; // Default fallback
//        String[] timeFormatArray = getResources().getStringArray(R.array.time_formate_array);
//        for (int i = 0; i < timeFormatArray.length; i++) {
//            Log.d("Edit_Activity_DateTime", "Comparing [" + i + "]: '" + timeFormatArray[i] + "' with '" + currentCombinedFormat + "'");
//            if (timeFormatArray[i].equals(currentCombinedFormat)) {
//                currentDateTimePosition = i;
//                Log.d("Edit_Activity_DateTime", "MATCH FOUND at position: " + i);
//                break;
//            }
//        }
//
//        // Update UI
//        updateDateTimeFormatText();
//    }
//
//
//    private void showFontStyleDialog() {
//
//        if (fontStyleDialog != null && fontStyleDialog.isShowing()) {
//            return;
//        }
//
//        String[] fontList = fontViewModel.getFontList().getValue();
//        if (fontList == null) {
//            // Fallback
//            fontList = getResources().getStringArray(R.array.font_name_array);
//        }
//        fontStyleDialog = new FontStyleDialog(this, fontList, new OnFontSelectedListener() {
//            @Override
//            public void onFontSelected(String fontName, int position) {
//                currentFontStyle = fontName;
//                currentFontPosition = position;
//
//                // Update UI
//                applyFontToTextView(binding.txtFontStyle, fontName);
//
//                Log.d("Edit_Activity_Rishi", "Font selected for template " + current_stamp_id +
//                        ": " + fontName + " (position: " + position + ")");
//                msp.setInteger(getApplicationContext(), SP.LOCATION_FONT_POSITION, currentFontPosition);
//            }
//
//            @Override
//            public void onDialogDismissed() {
//                fontStyleDialog = null;
//            }
//        });
//
//        fontStyleDialog.setSelectedPosition(currentFontPosition);
//        new HelperClass().setBottomDialog(fontStyleDialog);
//        fontStyleDialog.show();
//    }
//
//    private void showDateTimeDialog() {
//
//        if (templateDateTimeDialog != null && templateDateTimeDialog.isShowing()) {
//            return;
//        }
//
//        templateDateTimeDialog = new TemplateDateTimeDialog(this, current_stamp_id, new OnDateTimeSelectedListener() {
//            @Override
//            public void onDateTimeSelected(DateFormatModel selectedFormat, int position) {
//                // Update current values with separate formats
//                currentDateFormat = selectedFormat.getFormat_Date();
//                currentTimeFormat = selectedFormat.getFormat_Time();
//                currentCombinedFormat = selectedFormat.getFormat_Combined();
//                currentDateTimePosition = position;
//
//                // Update UI immediately
//                updateDateTimeFormatText();
//
//                Log.d("Edit_Activity_DateTime", "DateTime format selected for template " + current_stamp_id);
//                Log.d("Edit_Activity_DateTime", "Date: " + currentDateFormat);
//                Log.d("Edit_Activity_DateTime", "Time: " + currentTimeFormat);
//                Log.d("Edit_Activity_DateTime", "Combined: " + currentCombinedFormat);
//                Log.d("Edit_Activity_DateTime", "Position: " + position);
//            }
//
//            @Override
//            public void onDialogDismissed() {
//                templateDateTimeDialog = null;
//            }
//        });
//
//        // Set the position BEFORE showing the dialog
//        Log.d("Edit_Activity_DateTime", "Setting position: " + currentDateTimePosition + " for format: " + currentCombinedFormat);
//        if (currentDateTimePosition >= 0) {
//            templateDateTimeDialog.setSelectedPosition(currentDateTimePosition);
//        }
//
//        // Show the dialog
//        new HelperClass().setBottomDialog(templateDateTimeDialog);
//        templateDateTimeDialog.show();
//    }
//
//    private void showBgColorDialog() {
//
//        if (stampBgColorDialog != null && stampBgColorDialog.isShowing()) {
//            return;
//        }
//        stampBgColorDialog = new ColorDialog(this);
//        stampBgColorDialog.setDialogTitle(getString(R.string.bg_color));
//        stampBgColorDialog.setInitialColor(currentBgColor);
//
//        stampBgColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
//            @Override
//            public void onColorSelected(int color) {
//                currentBgColor = color; // update current color in memory
//                updateColorPreview(binding.selectedBgColor, currentBgColor); // Update UI preview
//                Log.d("Edit_Activity_Rishi", "Selected BG Color: #" + Integer.toHexString(color));
//            }
//        });
//        stampBgColorDialog.show();
//        new HelperClass().setFullscreenBottomDialog(stampBgColorDialog);
//    }
//
//    private void showTextColorDialog() {
//        if (textColorDialog != null && textColorDialog.isShowing()) {
//            return;
//        }
//
//        textColorDialog = new ColorDialog(this);
//        textColorDialog.setDialogTitle(getString(R.string.txt_color));
//        textColorDialog.setInitialColor(currentTextColor);
//
//        textColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
//            @Override
//            public void onColorSelected(int color) {
//                currentTextColor = color; // update current color in memory
//                updateColorPreview(binding.selectedTextColor, currentTextColor); // Update UI preview
//                Log.d("Edit_Activity_Rishi", "Selected Text Color: #" + Integer.toHexString(color));
//            }
//        });
//        textColorDialog.show();
//        new HelperClass().setFullscreenBottomDialog(textColorDialog);
//    }
//
//    private void showDateTimeColorDialog() {
//
//        if (stampDateTimeColorDialog != null && stampDateTimeColorDialog.isShowing()) {
//            return;
//        }
//
//        stampDateTimeColorDialog = new ColorDialog(this);
//        stampDateTimeColorDialog.setDialogTitle(getString(R.string.date_time_color));
//        stampDateTimeColorDialog.setInitialColor(currentDateTimeColor);
//
//        stampDateTimeColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
//            @Override
//            public void onColorSelected(int color) {
//                currentDateTimeColor = color; // Save to memory
//                updateColorPreview(binding.selectedDateTimeColor, currentDateTimeColor); // Update UI preview
//                Log.d("Edit_Activity_Rishi", "Selected DateTime Color: #" + Integer.toHexString(color));
//            }
//        });
//
//        stampDateTimeColorDialog.show();
//        new HelperClass().setFullscreenBottomDialog(stampDateTimeColorDialog);
//    }
//
//    private void showMapType(View view) {
//
//
//        if (mapTypeDialog != null && mapTypeDialog.isShowing()) {
//            return;
//        }
//        mapTypeDialog = new MapTypeDialog(this, current_map_type, new OnMapTypeSelectedListener() {
//            @Override
//            public void onMapTypeSelected(int mapType) {
//                current_map_type = mapType;
//                updateMapTypeText(); // Update UI
//                Log.d("Edit_Activity_Rishi_Map", "Selected MapType: " + current_map_type);
//            }
//
//            @Override
//            public void onDialogDismissed() {
//
//            }
//        });
//
//        Window window = mapTypeDialog.getWindow();
//        if (window != null) {
//            window.setGravity(Gravity.BOTTOM);
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//            window.getAttributes().windowAnimations = R.style.BottomDialogSlideAnimation;
//        }
//        mapTypeDialog.show();
//    }
//
//    private void resetTemplate() {
//        String defaultFontStyle = FastSave.getInstance().getString(MyApplication.FONT_STYLE, "SF Pro Display.otf");
//        currentFontStyle = defaultFontStyle;
//
//        // Reset font
//        msp.setTemplateFontStyle(this, current_stamp_id, defaultFontStyle);
//
//        if (current_stamp_id == 9) {
//            currentBgColor = ContextCompat.getColor(this, R.color.bg_glass);
//        } else {
//            currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//        }
//        msp.setTemplateBgColor(this, current_stamp_id, currentBgColor);
//
//        // Reset text color to white
//        currentTextColor = ContextCompat.getColor(this, R.color.white);
//        msp.setTemplateTextColor(this, current_stamp_id, currentTextColor);
//
//        // Reset DateTime color to white
//        currentDateTimeColor = ContextCompat.getColor(this, R.color.white);
//        msp.setTemplateDateTimeColor(this, current_stamp_id, currentDateTimeColor);
//
//        current_map_type = MyApplication.getMapType();
//        msp.setTemplateMapType(this, current_stamp_id, current_map_type);
//
//        // Reset edited flag
//        msp.setTemplateEdited(this, current_stamp_id, false);
//
//
//        String defaultDateFormat = FastSave.getInstance().getString(MyApplication.FORMAT_DATE, "dd-MM-yyyy");
//        String defaultTimeFormat = FastSave.getInstance().getString(MyApplication.FORMAT_TIME, "HH:mm:ss a");
//        String defaultCombinedFormat = FastSave.getInstance().getString(MyApplication.TIME_FORMAT, "dd-MM-yyyy HH:mm:ss a");
//
//        currentDateFormat = defaultDateFormat;
//        currentTimeFormat = defaultTimeFormat;
//        currentCombinedFormat = defaultCombinedFormat;
//        currentDateTimePosition = 0;
//
//        // Save reset values
//        msp.setTemplateDateFormat(this, current_stamp_id, currentDateFormat);
//        msp.setTemplateTimeFormat(this, current_stamp_id, currentTimeFormat);
//        msp.setTemplateDateTimeCombinedFormat(this, current_stamp_id, currentCombinedFormat);
//
//
//        // Update UI after reset
//        updatePreviewUI();
//        updateDateTimeFormatText();
//    }
//
////    private void resetTemplate() {
////        // Default values
////        String defaultFontStyle = "SF Pro Display.otf";
////        String defaultDateFormat = "dd-MM-yyyy";
////        String defaultTimeFormat = "HH:mm:ss a";
////        String defaultCombinedFormat = "dd-MM-yyyy HH:mm:ss a";
////
////
////        if (current_stamp_id == 9) {
////            currentBgColor = ContextCompat.getColor(this, R.color.bg_glass);
////        } else {
////            currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
////        }
////        msp.setTemplateBgColor(this, current_stamp_id, currentBgColor);
////
////        // Reset text color to white
////        currentTextColor = ContextCompat.getColor(this, R.color.white);
////        msp.setTemplateTextColor(this, current_stamp_id, currentTextColor);
////
////        // Reset DateTime color to white
////        currentDateTimeColor = ContextCompat.getColor(this, R.color.white);
////        msp.setTemplateDateTimeColor(this, current_stamp_id, currentDateTimeColor);
////
////        // Reset map type
////        current_map_type = MyApplication.getMapType();
////        msp.setTemplateMapType(this, current_stamp_id, current_map_type);
////
////        // Reset edited flag
////        msp.setTemplateEdited(this, current_stamp_id, true);
////
////        // Reset font
////        currentFontStyle = defaultFontStyle;
////        msp.setTemplateFontStyle(this, current_stamp_id, defaultFontStyle);
////
////
////        // Reset date/time formats
////        currentDateFormat = defaultDateFormat;
////        currentTimeFormat = defaultTimeFormat;
////        currentCombinedFormat = defaultCombinedFormat;
////        currentDateTimePosition = 0;
////
////        msp.setTemplateDateFormat(this, current_stamp_id, defaultDateFormat);
////        msp.setTemplateTimeFormat(this, current_stamp_id, defaultTimeFormat);
////        msp.setTemplateDateTimeCombinedFormat(this, current_stamp_id, defaultCombinedFormat);
////
////        // Update UI after reset
////        updatePreviewUI();
////        updateDateTimeFormatText();
////    }
//
//
//    private void updateDateTimeFormatText() {
//        if (binding.txtDateTimeFormat != null) {
//            // Show both date and time formats
//            String displayText = currentDateFormat + " | " + currentTimeFormat;
//            binding.txtDateTimeFormat.setText(displayText);
//
//            // Or show just the combined format
//            // binding.txtDateTimeFormat.setText(currentCombinedFormat);
//        }
//    }
//
//    private void saveTemplate() {
//        // Save template-specific font position
//        msp.setTemplateFontStyle(this, current_stamp_id, currentFontStyle);
//
//        // Save background color
//        msp.setTemplateBgColor(this, current_stamp_id, currentBgColor);
//
//        // Save text color
//        msp.setTemplateTextColor(this, current_stamp_id, currentTextColor);
//
//        // Save DateTime color
//        msp.setTemplateDateTimeColor(this, current_stamp_id, currentDateTimeColor);
//
//        // Save Map Type
//        msp.setTemplateMapType(this, current_stamp_id, current_map_type);
//
//
//        msp.setTemplateDateFormat(this, current_stamp_id, currentDateFormat);
//        msp.setTemplateTimeFormat(this, current_stamp_id, currentTimeFormat);
//        msp.setTemplateDateTimeCombinedFormat(this, current_stamp_id, currentCombinedFormat);
//
//        // Mark template as edited
//        msp.setTemplateEdited(this, current_stamp_id, true);
//
//        FastSave.getInstance().saveInt(MyApplication.STAMP_LAYOUT_ID, current_stamp_id);
//
//        setResult(RESULT_OK);
//        finish();
//
//        if (getParent() != null) {
//            getParent().finish();
//        }
//    }
//
//    private void getStampType() {
//        tempstamp_type = FastSave.getInstance().getInt(MyApplication.TEMP_STAMP_LAYOUT_ID, 1);
//        current_stamp_id = tempstamp_type;
//        Log.d("Rishi_Savedstamptemplate", "STAMP_LAYOUT_ID - getStampType: " + tempstamp_type);
//    }
//
//    private void setStampLayout() {
//        switch (tempstamp_type) {
//            case 1:
//                binding.ThemePreview.setImageResource(R.drawable.theme1);
//                break;
//            case 2:
//                binding.ThemePreview.setImageResource(R.drawable.theme5);
//                break;
//            case 3:
//                binding.ThemePreview.setImageResource(R.drawable.theme6);
//                break;
//            case 4:
//                binding.ThemePreview.setImageResource(R.drawable.theme4);
//                break;
//            case 5:
//                binding.ThemePreview.setImageResource(R.drawable.theme7);
//                break;
//            case 6:
//                binding.ThemePreview.setImageResource(R.drawable.theme8);
//                break;
//            case 7:
//                binding.ThemePreview.setImageResource(R.drawable.theme9);
//                break;
//            case 8:
//                binding.ThemePreview.setImageResource(R.drawable.theme10);
//                break;
//            case 9:
//                binding.ThemePreview.setImageResource(R.drawable.theme11);
//                break;
//            default:
//                binding.ThemePreview.setImageResource(R.drawable.theme1);
//                break;
//        }
//    }
//
//    private void updatePreviewUI() {
//        // Update font style text
//        applyFontToTextView(binding.txtFontStyle, currentFontStyle);
//
//        // Update background color preview
//        updateColorPreview(binding.selectedBgColor, currentBgColor);
//
//        // Update text color preview
//        updateColorPreview(binding.selectedTextColor, currentTextColor);
//
//        // Update date time color preview
//        updateColorPreview(binding.selectedDateTimeColor, currentDateTimeColor);
//
//        // Update map type text (you might need to implement this based on your map type values)
//        updateMapTypeText();
//    }
//
//    private void updateColorPreview(View colorView, int color) {
//        GradientDrawable drawable = new GradientDrawable();
//        drawable.setShape(GradientDrawable.OVAL);
//        drawable.setColor(color);
//        drawable.setStroke(2, ContextCompat.getColor(this, R.color.grey_layout));
//        colorView.setBackground(drawable);
//    }
//
//    private void applyFontToTextView(TextView textView, String fontName) {
//        textView.setTypeface(new HelperClass().getFontStyle(this, fontName));
//        textView.setText("Text Style");
//    }
//
//    private void updateMapTypeText() {
//        String mapTypeText = getString(R.string.normal_map); // Default
//        switch (current_map_type) {
//            case 1:
//                mapTypeText = getString(R.string.normal_map);
//                break;
//            case 2:
//                mapTypeText = getString(R.string.satellite_map);
//                break;
//            case 3:
//                mapTypeText = getString(R.string.terrain_map);
//                break;
//            case 4:
//                mapTypeText = getString(R.string.hybrid_map);
//                break;
//            // Add more cases as needed
//        }
//        binding.txtMapType.setText(mapTypeText);
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
//            if (!InterstitialAdManager.isInterstitialShowing()) {
//                setInterstitialShowing(true);
//                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "edit_template_close_interstitial",
//                        () -> {
//                            setInterstitialShowing(false);
//                            super.onBackPressed();
//                        },
//                        errorMsg -> {
//                            setInterstitialShowing(false);
//                            Utils.LogUtils.logE("EditTemplate", "Ad failed: " + errorMsg);
//                        });
//            } else {
//                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
//            }
//        } else {
//            super.onBackPressed();
//        }
//    }
//}

package com.camera.gps.activity;

import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.appizona.yehiahd.fastsave.FastSave;
import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.databinding.ActivityEditBinding;
import com.camera.gps.dialogs.DateTimeDialog;
import com.camera.gps.dialogs.FontStyleDialog;
import com.camera.gps.dialogs.MapTypeDialog;
import com.camera.gps.dialogs.ColorDialog;
import com.camera.gps.dialogs.TemplateDateTimeDialog;
import com.camera.gps.listener.OnDateTimeSelectedListener;
import com.camera.gps.listener.OnFontSelectedListener;
import com.camera.gps.listener.OnMapTypeSelectedListener;
import com.camera.gps.model.DateFormatModel;
import com.camera.gps.util.HelperClass;
import com.camera.gps.util.SP;
import com.camera.gps.util.Utils;
import com.camera.gps.viewmodel.FontStyleViewModel;

public class Edit_Activity extends AppCompatActivity {

    private ActivityEditBinding binding;
    private int tempstamp_type;
    private int current_stamp_id;
    private FontStyleDialog fontStyleDialog;
    private FontStyleViewModel fontViewModel;
    private TemplateDateTimeDialog templateDateTimeDialog;

    private ColorDialog stampBgColorDialog;
    private ColorDialog textColorDialog;
    private ColorDialog stampDateTimeColorDialog;
    private MapTypeDialog mapTypeDialog;

    int current_map_type;
    private String currentFontStyle = "SF Pro Display.otf";
    private int currentFontPosition = 0;

    private int currentBgColor;
    private int currentTextColor;
    private int currentDateTimeColor;
    private Dialog dialog;
    private String currentDateFormat = "";
    private String currentTimeFormat = "";
    private String currentCombinedFormat = "";
    private int currentDateTimePosition = 0;

    private SP msp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fontViewModel = new ViewModelProvider(this).get(FontStyleViewModel.class);

        msp = new SP(this);

        setupViews();
        getStampType();
        setStampLayout();
        loadTemplateSettings();
        updatePreviewUI(); // Update UI after loading settings
    }

    private void setupViews() {
        binding.btBack.setOnClickListener(v -> {
            onBackPressed();
        });

        binding.btnSaveSettings.setOnClickListener(v -> {
            saveTemplate();
        });

        binding.btnResetSettings.setOnClickListener(v -> {
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

            title.setText(getString(R.string.reset_this_template));
            message.setText(getString(R.string.reset_template_message));
            btnDelete.setText(getString(R.string.reset));

            btnDelete.setOnClickListener(view -> {
                resetTemplate();
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

        binding.layoutFontStyle.setOnClickListener(v -> {
            showFontStyleDialog();
        });

        binding.layoutDateTimeFormat.setOnClickListener(v -> {
            showDateTimeDialog();
        });

        binding.layoutBgColor.setOnClickListener(v -> {
            showBgColorDialog();
        });

        binding.layoutTextColor.setOnClickListener(v -> {
            showTextColorDialog();
        });

        binding.layoutDateTimeColor.setOnClickListener(v -> {
            showDateTimeColorDialog();
        });

        binding.layoutMapType.setOnClickListener(this::showMapType);
    }

    // NEW METHOD: Get predefined values for each template
    private void getPredefinedTemplateDefaults() {
        switch (current_stamp_id) {
//            case 1:
//                currentFontStyle = "SF Pro Display.otf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.WHITE;
//                currentDateTimeColor = Color.WHITE;
//                current_map_type = 1; // Normal map
//                currentDateFormat = "dd-MM-yyyy";
//                currentTimeFormat = "HH:mm:ss a";
//                currentCombinedFormat = "dd-MM-yyyy HH:mm:ss a";
//                break;
//            case 2:
//                currentFontStyle = "Roboto-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.BLACK;
//                currentDateTimeColor = Color.BLACK;
//                current_map_type = 2; // Satellite map
//                currentDateFormat = "MM/dd/yyyy";
//                currentTimeFormat = "hh:mm a";
//                currentCombinedFormat = "MM/dd/yyyy hh:mm a";
//                break;
//            case 3:
//                currentFontStyle = "OpenSans-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.BLUE;
//                currentDateTimeColor = Color.BLUE;
//                current_map_type = 3; // Terrain map
//                currentDateFormat = "yyyy-MM-dd";
//                currentTimeFormat = "HH:mm";
//                currentCombinedFormat = "yyyy-MM-dd HH:mm";
//                break;
//            case 4:
//                currentFontStyle = "Lato-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.RED;
//                currentDateTimeColor = Color.RED;
//                current_map_type = 4; // Hybrid map
//                currentDateFormat = "dd/MM/yyyy";
//                currentTimeFormat = "HH:mm:ss";
//                currentCombinedFormat = "dd/MM/yyyy HH:mm:ss";
//                break;
//            case 5:
//                currentFontStyle = "Montserrat-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.GREEN;
//                currentDateTimeColor = Color.GREEN;
//                current_map_type = 1; // Normal map
//                currentDateFormat = "MMM dd, yyyy";
//                currentTimeFormat = "hh:mm:ss a";
//                currentCombinedFormat = "MMM dd, yyyy hh:mm:ss a";
//                break;
//            case 6:
//                currentFontStyle = "SourceSansPro-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.MAGENTA;
//                currentDateTimeColor = Color.MAGENTA;
//                current_map_type = 2; // Satellite map
//                currentDateFormat = "EEEE, MMMM dd, yyyy";
//                currentTimeFormat = "HH:mm a";
//                currentCombinedFormat = "EEEE, MMMM dd, yyyy HH:mm a";
//                break;
//            case 7:
//                currentFontStyle = "Poppins-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.CYAN;
//                currentDateTimeColor = Color.CYAN;
//                current_map_type = 3; // Terrain map
//                currentDateFormat = "dd.MM.yyyy";
//                currentTimeFormat = "HH:mm:ss";
//                currentCombinedFormat = "dd.MM.yyyy HH:mm:ss";
//                break;
//            case 8:
//                currentFontStyle = "NunitoSans-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
//                currentTextColor = Color.YELLOW;
//                currentDateTimeColor = Color.YELLOW;
//                current_map_type = 4; // Hybrid map
//                currentDateFormat = "yyyy/MM/dd";
//                currentTimeFormat = "hh:mm a";
//                currentCombinedFormat = "yyyy/MM/dd hh:mm a";
//                break;
//            case 9:
//                currentFontStyle = "Inter-Regular.ttf";
//                currentBgColor = ContextCompat.getColor(this, R.color.bg_glass);
//                currentTextColor = Color.WHITE;
//                currentDateTimeColor = Color.WHITE;
//                current_map_type = 1; // Normal map
//                currentDateFormat = "dd-MMM-yyyy";
//                currentTimeFormat = "HH:mm:ss a";
//                currentCombinedFormat = "dd-MMM-yyyy HH:mm:ss a";
//                break;
            default:
                // Default template values
                currentFontStyle = "SF Pro Display.otf";
                currentBgColor = ContextCompat.getColor(this, R.color.transparent_30);
                currentTextColor = Color.WHITE;
                currentDateTimeColor = Color.WHITE;
                current_map_type = 1; // Normal map
                currentDateFormat = "dd-MM-yyyy";
                currentTimeFormat = "HH:mm:ss a";
                currentCombinedFormat = "dd-MM-yyyy HH:mm:ss a";
                break;
        }

        // Find font position
        String[] fontList = getResources().getStringArray(R.array.font_name_array);
        currentFontPosition = 0;
        for (int i = 0; i < fontList.length; i++) {
            if (fontList[i].equalsIgnoreCase(currentFontStyle)) {
                currentFontPosition = i;
                break;
            }
        }

        // Find datetime position
        currentDateTimePosition = 0;
        String[] timeFormatArray = getResources().getStringArray(R.array.time_formate_array);
        for (int i = 0; i < timeFormatArray.length; i++) {
            if (timeFormatArray[i].equals(currentCombinedFormat)) {
                currentDateTimePosition = i;
                break;
            }
        }
    }

    private void loadTemplateSettings() {
        // Check if template has been edited before
        boolean isTemplateEdited = msp.isTemplateEdited(this, current_stamp_id);

        if (isTemplateEdited) {
            // Load saved template settings
            currentFontStyle = msp.getTemplateFontStyle(this, current_stamp_id, "SF Pro Display.otf");
            current_map_type = msp.getTemplateMapType(this, current_stamp_id, 1);
            currentBgColor = msp.getTemplateBgColor(this, current_stamp_id, ContextCompat.getColor(this, R.color.transparent_30));
            currentTextColor = msp.getTemplateTextColor(this, current_stamp_id, Color.WHITE);
            currentDateTimeColor = msp.getTemplateDateTimeColor(this, current_stamp_id, Color.WHITE);
            currentDateFormat = msp.getTemplateDateFormat(this, current_stamp_id, "dd-MM-yyyy");
            currentTimeFormat = msp.getTemplateTimeFormat(this, current_stamp_id, "HH:mm:ss a");
            currentCombinedFormat = msp.getTemplateDateTimeCombinedFormat(this, current_stamp_id, "dd-MM-yyyy HH:mm:ss a");

            Log.d("Edit_Activity_Template", "Loading edited template settings for ID: " + current_stamp_id);
        } else {
            // Load predefined template defaults
            getPredefinedTemplateDefaults();
            Log.d("Edit_Activity_Template", "Loading predefined template settings for ID: " + current_stamp_id);
        }

        // Find font position
        String[] fontList = getResources().getStringArray(R.array.font_name_array);
        currentFontPosition = 0;
        for (int i = 0; i < fontList.length; i++) {
            if (fontList[i].equalsIgnoreCase(currentFontStyle)) {
                currentFontPosition = i;
                break;
            }
        }

        // Find datetime position
        currentDateTimePosition = 0;
        String[] timeFormatArray = getResources().getStringArray(R.array.time_formate_array);
        for (int i = 0; i < timeFormatArray.length; i++) {
            if (timeFormatArray[i].equals(currentCombinedFormat)) {
                currentDateTimePosition = i;
                break;
            }
        }

        Log.d("Edit_Activity_Template", "Template " + current_stamp_id + " settings loaded:");
        Log.d("Edit_Activity_Template", "Font: " + currentFontStyle + " (pos: " + currentFontPosition + ")");
        Log.d("Edit_Activity_Template", "Map Type: " + current_map_type);
        Log.d("Edit_Activity_Template", "BG Color: #" + Integer.toHexString(currentBgColor));
        Log.d("Edit_Activity_Template", "Text Color: #" + Integer.toHexString(currentTextColor));
        Log.d("Edit_Activity_Template", "DateTime Color: #" + Integer.toHexString(currentDateTimeColor));
        Log.d("Edit_Activity_Template", "Date Format: " + currentDateFormat);
        Log.d("Edit_Activity_Template", "Time Format: " + currentTimeFormat);
        Log.d("Edit_Activity_Template", "Combined Format: " + currentCombinedFormat + " (pos: " + currentDateTimePosition + ")");

        // Update UI
        updateDateTimeFormatText();
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
                currentFontStyle = fontName;
                currentFontPosition = position;

                // Update UI
                applyFontToTextView(binding.txtFontStyle, fontName);

                Log.d("Edit_Activity_Template", "Font selected for template " + current_stamp_id +
                        ": " + fontName + " (position: " + position + ")");
            }

            @Override
            public void onDialogDismissed() {
                fontStyleDialog = null;
            }
        });

        fontStyleDialog.setSelectedPosition(currentFontPosition);
        new HelperClass().setBottomDialog(fontStyleDialog);
        fontStyleDialog.show();
    }

    private void showDateTimeDialog() {
        if (templateDateTimeDialog != null && templateDateTimeDialog.isShowing()) {
            return;
        }

        templateDateTimeDialog = new TemplateDateTimeDialog(this, current_stamp_id, new OnDateTimeSelectedListener() {
            @Override
            public void onDateTimeSelected(DateFormatModel selectedFormat, int position) {
                currentDateFormat = selectedFormat.getFormat_Date();
                currentTimeFormat = selectedFormat.getFormat_Time();
                currentCombinedFormat = selectedFormat.getFormat_Combined();
                currentDateTimePosition = position;

                // Update UI immediately
                updateDateTimeFormatText();


                Log.d("Edit_Activity_Template", "DateTime format selected for template " + current_stamp_id);
                Log.d("Edit_Activity_Template", "Date: " + currentDateFormat);
                Log.d("Edit_Activity_Template", "Time: " + currentTimeFormat);
                Log.d("Edit_Activity_Template", "Combined: " + currentCombinedFormat);
                Log.d("Edit_Activity_Template", "Position: " + position);
            }

            @Override
            public void onDialogDismissed() {
                templateDateTimeDialog = null;
            }
        });

        if (currentDateTimePosition >= 0) {
            templateDateTimeDialog.setSelectedPosition(currentDateTimePosition);
        }

        new HelperClass().setBottomDialog(templateDateTimeDialog);
        templateDateTimeDialog.show();
    }

    private void showBgColorDialog() {
        if (stampBgColorDialog != null && stampBgColorDialog.isShowing()) {
            return;
        }
        stampBgColorDialog = new ColorDialog(this);
        stampBgColorDialog.setDialogTitle(getString(R.string.bg_color));
        stampBgColorDialog.setInitialColor(currentBgColor);

        stampBgColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                currentBgColor = color;
                updateColorPreview(binding.selectedBgColor, currentBgColor);
                Log.d("Edit_Activity_Template", "Selected BG Color: #" + Integer.toHexString(color));
            }
        });
        stampBgColorDialog.show();
        new HelperClass().setFullscreenBottomDialog(stampBgColorDialog);
    }

    private void showTextColorDialog() {
        if (textColorDialog != null && textColorDialog.isShowing()) {
            return;
        }

        textColorDialog = new ColorDialog(this);
        textColorDialog.setDialogTitle(getString(R.string.txt_color));
        textColorDialog.setInitialColor(currentTextColor);

        textColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                currentTextColor = color;
                updateColorPreview(binding.selectedTextColor, currentTextColor);
                Log.d("Edit_Activity_Template", "Selected Text Color: #" + Integer.toHexString(color));
            }
        });
        textColorDialog.show();
        new HelperClass().setFullscreenBottomDialog(textColorDialog);
    }

    private void showDateTimeColorDialog() {
        if (stampDateTimeColorDialog != null && stampDateTimeColorDialog.isShowing()) {
            return;
        }

        stampDateTimeColorDialog = new ColorDialog(this);
        stampDateTimeColorDialog.setDialogTitle(getString(R.string.date_time_color));
        stampDateTimeColorDialog.setInitialColor(currentDateTimeColor);

        stampDateTimeColorDialog.setOnColorSelectedListener(new ColorDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                currentDateTimeColor = color;
                updateColorPreview(binding.selectedDateTimeColor, currentDateTimeColor);
                Log.d("Edit_Activity_Template", "Selected DateTime Color: #" + Integer.toHexString(color));
            }
        });

        stampDateTimeColorDialog.show();
        new HelperClass().setFullscreenBottomDialog(stampDateTimeColorDialog);
    }

    private void showMapType(View view) {
        if (mapTypeDialog != null && mapTypeDialog.isShowing()) {
            return;
        }
        mapTypeDialog = new MapTypeDialog(this, current_map_type, new OnMapTypeSelectedListener() {
            @Override
            public void onMapTypeSelected(int mapType) {
                current_map_type = mapType;
                updateMapTypeText();
                Log.d("Edit_Activity_Template", "Selected MapType: " + current_map_type);
            }

            @Override
            public void onDialogDismissed() {

            }
        });

        Window window = mapTypeDialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.getAttributes().windowAnimations = R.style.BottomDialogSlideAnimation;
        }
        mapTypeDialog.show();
    }

    // MODIFIED: Reset to predefined template values instead of global values
    private void resetTemplate() {
        Log.d("Edit_Activity_Template", "Resetting template " + current_stamp_id + " to predefined defaults");

        // Load predefined defaults for this template
        getPredefinedTemplateDefaults();

        // Save the reset values to shared preferences
        msp.setTemplateFontStyle(this, current_stamp_id, currentFontStyle);
        msp.setTemplateBgColor(this, current_stamp_id, currentBgColor);
        msp.setTemplateTextColor(this, current_stamp_id, currentTextColor);
        msp.setTemplateDateTimeColor(this, current_stamp_id, currentDateTimeColor);
        msp.setTemplateMapType(this, current_stamp_id, current_map_type);
        msp.setTemplateDateFormat(this, current_stamp_id, currentDateFormat);
        msp.setTemplateTimeFormat(this, current_stamp_id, currentTimeFormat);
        msp.setTemplateDateTimeCombinedFormat(this, current_stamp_id, currentCombinedFormat);

        // Reset edited flag
        msp.setTemplateEdited(this, current_stamp_id, false);

        Log.d("Edit_Activity_Template", "Template " + current_stamp_id + " reset to predefined values:");
        Log.d("Edit_Activity_Template", "Font: " + currentFontStyle);
        Log.d("Edit_Activity_Template", "Map Type: " + current_map_type);
        Log.d("Edit_Activity_Template", "DateTime: " + currentCombinedFormat);

        // Update UI after reset
        updatePreviewUI();
        updateDateTimeFormatText();
    }

    private void updateDateTimeFormatText() {
        if (binding.txtDateTimeFormat != null) {
            String displayText = currentDateFormat + " | " + currentTimeFormat;
            binding.txtDateTimeFormat.setText(displayText);
        }
    }

    private void saveTemplate() {
        // Save all template-specific settings
        msp.setTemplateFontStyle(this, current_stamp_id, currentFontStyle);
        msp.setTemplateBgColor(this, current_stamp_id, currentBgColor);
        msp.setTemplateTextColor(this, current_stamp_id, currentTextColor);
        msp.setTemplateDateTimeColor(this, current_stamp_id, currentDateTimeColor);
        msp.setTemplateMapType(this, current_stamp_id, current_map_type);
        msp.setTemplateDateFormat(this, current_stamp_id, currentDateFormat);
        msp.setTemplateTimeFormat(this, current_stamp_id, currentTimeFormat);
        msp.setTemplateDateTimeCombinedFormat(this, current_stamp_id, currentCombinedFormat);

        // Mark template as edited
        msp.setTemplateEdited(this, current_stamp_id, true);

        // Update global settings with current template values (as per your requirement)
        FastSave.getInstance().saveString(MyApplication.FONT_STYLE, currentFontStyle);
        FastSave.getInstance().saveString(MyApplication.FORMAT_DATE, currentDateFormat);
        FastSave.getInstance().saveString(MyApplication.FORMAT_TIME, currentTimeFormat);
        FastSave.getInstance().saveString(MyApplication.TIME_FORMAT, currentCombinedFormat);
        MyApplication.setMapType(current_map_type);

        FastSave.getInstance().saveInt(MyApplication.STAMP_LAYOUT_ID, current_stamp_id);

        Log.d("Edit_Activity_Template", "Template " + current_stamp_id + " saved and global settings updated");

        setResult(RESULT_OK);
        finish();

        if (getParent() != null) {
            getParent().finish();
        }
    }

    private void getStampType() {
        tempstamp_type = FastSave.getInstance().getInt(MyApplication.TEMP_STAMP_LAYOUT_ID, 1);
        current_stamp_id = tempstamp_type;
        Log.d("Edit_Activity_Template", "Current stamp ID: " + current_stamp_id);
    }

    private void setStampLayout() {
        switch (tempstamp_type) {
            case 1:
                binding.ThemePreview.setImageResource(R.drawable.theme1);
                break;
            case 2:
                binding.ThemePreview.setImageResource(R.drawable.theme5);
                break;
            case 3:
                binding.ThemePreview.setImageResource(R.drawable.theme6);
                break;
            case 4:
                binding.ThemePreview.setImageResource(R.drawable.theme4);
                break;
            case 5:
                binding.ThemePreview.setImageResource(R.drawable.theme7);
                break;
            case 6:
                binding.ThemePreview.setImageResource(R.drawable.theme8);
                break;
            case 7:
                binding.ThemePreview.setImageResource(R.drawable.theme9);
                break;
            case 8:
                binding.ThemePreview.setImageResource(R.drawable.theme10);
                break;
            case 9:
                binding.ThemePreview.setImageResource(R.drawable.theme11);
                break;
            default:
                binding.ThemePreview.setImageResource(R.drawable.theme1);
                break;
        }
    }

    private void updatePreviewUI() {
        applyFontToTextView(binding.txtFontStyle, currentFontStyle);
        updateColorPreview(binding.selectedBgColor, currentBgColor);
        updateColorPreview(binding.selectedTextColor, currentTextColor);
        updateColorPreview(binding.selectedDateTimeColor, currentDateTimeColor);
        updateMapTypeText();
    }

    private void updateColorPreview(View colorView, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(2, ContextCompat.getColor(this, R.color.grey_layout));
        colorView.setBackground(drawable);
    }

    private void applyFontToTextView(TextView textView, String fontName) {
        textView.setTypeface(new HelperClass().getFontStyle(this, fontName));
        textView.setText("Text Style");
    }

    private void updateMapTypeText() {
        String mapTypeText = getString(R.string.normal_map);
        switch (current_map_type) {
            case 1:
                mapTypeText = getString(R.string.normal_map);
                break;
            case 2:
                mapTypeText = getString(R.string.satellite_map);
                break;
            case 3:
                mapTypeText = getString(R.string.terrain_map);
                break;
            case 4:
                mapTypeText = getString(R.string.hybrid_map);
                break;
        }
        binding.txtMapType.setText(mapTypeText);
    }

    @Override
    public void onBackPressed() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "edit_template_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            super.onBackPressed();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("EditTemplate", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            super.onBackPressed();
        }
    }
}