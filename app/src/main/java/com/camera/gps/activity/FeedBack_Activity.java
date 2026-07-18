//package com.camera.gps.activity;
//
//import static com.camera.gps.adsmanager.InterstitialAdManager.setInterstitialShowing;
//
//import android.app.ProgressDialog;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.camera.gps.MyApplication;
//import com.camera.gps.adsmanager.InterstitialAdManager;
//import com.camera.gps.databinding.ActivityFeedBackBinding;
//import com.camera.gps.util.Utils;
//import com.google.gson.JsonElement;
//
//import java.util.Objects;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//import com.camera.gps.R;
//import com.camera.gps.api.Api_Client;
//import com.camera.gps.api.Api_Interface;
//
//public class FeedBack_Activity extends AppCompatActivity {
//
//    private ActivityFeedBackBinding binding;
//
//    // Selected rating (1-5)
//    private int selectedRating = 0;
//
//    // Optional fields (commented for now)
//    // private String experience;
//    // private String contact;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityFeedBackBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setupClickListeners();
//    }
//
//    private void setupClickListeners() {
//        binding.btBack.setOnClickListener(v -> onBackPressed());
//
//        // Emoji selection
//        binding.ivTerrible.setOnClickListener(v -> selectRating(1));
//        binding.ivBad.setOnClickListener(v -> selectRating(2));
//        binding.ivBetter.setOnClickListener(v -> selectRating(3));
//        binding.ivGood.setOnClickListener(v -> selectRating(4));
//        binding.ivLovedIt.setOnClickListener(v -> selectRating(5));
//
//        binding.btnSendFeedBack.setOnClickListener(v -> submitFeedback());
//    }
//
//    private void selectRating(int rating) {
//        selectedRating = rating;
//
//        // Reset all emoji backgrounds
//        binding.ivTerrible.setBackgroundResource(R.drawable.bg_card_unselected);
//        binding.ivBad.setBackgroundResource(R.drawable.bg_card_unselected);
//        binding.ivBetter.setBackgroundResource(R.drawable.bg_card_unselected);
//        binding.ivGood.setBackgroundResource(R.drawable.bg_card_unselected);
//        binding.ivLovedIt.setBackgroundResource(R.drawable.bg_card_unselected);
//
//        // Highlight selected emoji
//        switch (rating) {
//            case 1:
//                binding.ivTerrible.setBackgroundResource(R.drawable.bg_card_selected);
//                break;
//            case 2:
//                binding.ivBad.setBackgroundResource(R.drawable.bg_card_selected);
//                break;
//            case 3:
//                binding.ivBetter.setBackgroundResource(R.drawable.bg_card_selected);
//                break;
//            case 4:
//                binding.ivGood.setBackgroundResource(R.drawable.bg_card_selected);
//                break;
//            case 5:
//                binding.ivLovedIt.setBackgroundResource(R.drawable.bg_card_selected);
//                break;
//        }
//    }
//
//    private void submitFeedback() {
//        String feedbackType = binding.etFeedbackType.getText().toString().trim();
//        String comment = binding.etComment.getText().toString().trim();
//        String contactInfo = binding.etContactInfo.getText().toString().trim();
//
//        if (comment.isEmpty()) {
//            Toast.makeText(this, getString(R.string.please_enter_description), Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String appVersion = "";
//        try {
//            appVersion = Objects.requireNonNull(getPackageManager()
//                    .getPackageInfo(getPackageName(), 0)).versionName;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String appName = getString(R.string.app_name);
//        String androidVersion = String.valueOf(Build.VERSION.SDK_INT);
//        String deviceName = Build.MODEL;
//        String packageName = getPackageName();
//
//        // commented fields for future use
//        // experience = String.valueOf(selectedRating);
//        // contact = contactInfo;
//
//        postFeedback(appVersion, appName, androidVersion, deviceName, packageName, feedbackType, comment);
//    }
//
//    private void postFeedback(String version, String appName, String androidVersion,
//                              String deviceName, String packageName, String title, String description) {
//
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Sending feedback...");
//        progressDialog.show();
//
//        android.util.Log.d("FeedbackRequest",
//                "appName=" + appName +
//                        ", packageName=" + packageName +
//                        ", title=" + title +
//                        ", description=" + description +
//                        ", deviceName=" + deviceName +
//                        ", androidVersion=" + androidVersion +
//                        ", version=" + version);
//
//
//        Api_Interface apiInterface = Api_Client.getInstance();
//        apiInterface.sendfeedback(appName, packageName, title, description, deviceName, androidVersion, version)
//                .enqueue(new Callback<JsonElement>() {
//
//                    @Override
//                    public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
//                        if (progressDialog.isShowing()) progressDialog.dismiss();
//
//                        if (response.isSuccessful() && response.body() != null) {
//                            boolean status = response.body().getAsJsonObject().get("status").getAsBoolean();
//                            String msg = response.body().getAsJsonObject().get("message").getAsString();
//                            if (status) {
//                                Toast.makeText(FeedBack_Activity.this, msg, Toast.LENGTH_SHORT).show();
//                                binding.etFeedbackType.setText("");
//                                binding.etComment.setText("");
//                                binding.etContactInfo.setText("");
//                                selectedRating = 0;
//                                selectRating(0); // reset highlight
//                            }
//                        } else {
//                            Toast.makeText(FeedBack_Activity.this,
//                                    getString(R.string.please_send_feedback_in_playstore),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
//                        if (progressDialog.isShowing()) progressDialog.dismiss();
//                        Toast.makeText(FeedBack_Activity.this,
//                                getString(R.string.please_send_feedback_in_playstore),
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void closeKeyboard() {
//        try {
//            View view = getCurrentFocus();
//            if (view != null) {
//                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
//                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
//            if (!InterstitialAdManager.isInterstitialShowing()) {
//                setInterstitialShowing(true);
//                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "feedback_close_interstitial",
//                        () -> {
//                            setInterstitialShowing(false);
//                            finish();
//                        },
//                        errorMsg -> {
//                            setInterstitialShowing(false);
//                            Utils.LogUtils.logE("Feedback", "Ad failed: " + errorMsg);
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

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.camera.gps.MyApplication;
import com.camera.gps.R;
import com.camera.gps.adsmanager.InterstitialAdManager;
import com.camera.gps.databinding.ActivityFeedBackBinding;
import com.camera.gps.util.Utils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FeedBack_Activity extends AppCompatActivity {

    private ActivityFeedBackBinding binding;
    private int selectedRating = 0; // keeping as before if needed later
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btBack.setOnClickListener(v -> onBackPressed());
        binding.btnSendFeedBack.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        String feedbackType = binding.etFeedbackType.getText().toString().trim();
        String comment = binding.etComment.getText().toString().trim();

        if (feedbackType.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_feedback_type), Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_description), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!MyApplication.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            return;
        }

        String appVersion = "";
        try {
            appVersion = Objects.requireNonNull(getPackageManager()
                    .getPackageInfo(getPackageName(), 0)).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String androidVersion = String.valueOf(Build.VERSION.SDK_INT);
        String deviceName = Build.MODEL;

        // Pretty readable timestamp
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        postFeedback(appVersion, androidVersion, deviceName, feedbackType, comment, readableTime);
    }

    private void postFeedback(String version, String androidVersion,
                              String deviceName, String title, String description,
                              String readableTime) {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.sending_feedback));
        progressDialog.show();

        Log.d("FeedbackRequest",
                "title=" + title +
                        ", description=" + description +
                        ", deviceName=" + deviceName +
                        ", androidVersion=" + androidVersion +
                        ", version=" + version);

        // Firestore data
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("title", title);
        feedback.put("description", description);
        feedback.put("deviceName", deviceName);
        feedback.put("androidVersion", androidVersion);
        feedback.put("appVersion", version);
        feedback.put("timestamp", readableTime);

        firestore.collection("feedbacks")
                .add(feedback)
                .addOnSuccessListener(documentReference -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(FeedBack_Activity.this, getString(R.string.feedback_submitted), Toast.LENGTH_SHORT).show();

                    // Reset fields
                    binding.etFeedbackType.setText("");
                    binding.etComment.setText("");
                })
                .addOnFailureListener(e -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    Toast.makeText(FeedBack_Activity.this,
                            getString(R.string.please_send_feedback_in_playstore),
                            Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreFeedback", "Error adding feedback", e);
                });
    }

    private void closeKeyboard() {
        try {
            View view = getCurrentFocus();
            if (view != null) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @android.annotation.SuppressLint("MissingSuperCall")
    public void onBackPressed() {
        if (MyApplication.isNetworkAvailable(this) && !Utils.getIsPremium(this)) {
            if (!InterstitialAdManager.isInterstitialShowing()) {
                setInterstitialShowing(true);
                InterstitialAdManager.getInstance().loadAndShowInterstitialAd(this, "feedback_close_interstitial",
                        () -> {
                            setInterstitialShowing(false);
                            finish();
                        },
                        errorMsg -> {
                            setInterstitialShowing(false);
                            Utils.LogUtils.logE("Feedback", "Ad failed: " + errorMsg);
                        });
            } else {
                Utils.LogUtils.logD("MapActivity", "Interstitial already showing, ignoring click");
            }
        } else {
            finish();
        }
    }
}
