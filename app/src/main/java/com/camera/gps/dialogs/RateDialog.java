package com.camera.gps.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Objects;

import com.camera.gps.R;
import com.camera.gps.activity.FeedBack_Activity;
import com.camera.gps.databinding.DialogRateUsBinding;
import com.camera.gps.util.Utils;

public class RateDialog extends Dialog implements View.OnClickListener {
    private final Activity context;
    private final boolean exit;
    private int star_number;
    private final SharedPreferences sharedPreferences;

    private final DialogRateUsBinding binding;

    public RateDialog(@NonNull Activity activity, boolean z) {
        super(activity, android.R.style.Theme_Material_Dialog);
        this.context = activity;
        this.exit = z;

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = DialogRateUsBinding.inflate(activity.getLayoutInflater());
        setContentView(binding.getRoot());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getWindow().setAttributes(lp);

        initView();
        this.star_number = 0;
        sharedPreferences = activity.getSharedPreferences("mypref3", 0);
    }

    private void initView() {
        binding.tvSubmit.setOnClickListener(this);
        binding.tvLater.setOnClickListener(this);
        binding.closeRatusDialog.setOnClickListener(this);

        binding.star1.setOnClickListener(this);
        binding.star2.setOnClickListener(this);
        binding.star3.setOnClickListener(this);
        binding.star4.setOnClickListener(this);
        binding.star5.setOnClickListener(this);

        // Start animations
        binding.star4.startAnimation(createDotAnimation());
        binding.star5.startAnimation(createDotAnimation());
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tvLater || id == R.id.close_ratus_dialog) {
            if (this.exit) {
                this.context.finish();
            } else {
                dismiss();
            }
        } else if (id == R.id.tvSubmit) {
            if (this.star_number >= 4) {
                this.context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + "com.camera.gps")));
                Utils.setRated(this.context, true);
                dismiss();
            } else if (this.star_number > 0) {
                context.startActivity(new Intent(context, FeedBack_Activity.class));
                dismiss();
            }
            else {
                Toast.makeText(context, context.getString(R.string.please_select_stars), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.star1) {
            this.star_number = 1;
            setStarBar();
            binding.star4.startAnimation(createDotAnimation());
            binding.star5.startAnimation(createDotAnimation());
        } else if (id == R.id.star2) {
            this.star_number = 2;
            setStarBar();
            binding.star4.startAnimation(createDotAnimation());
            binding.star5.startAnimation(createDotAnimation());
        } else if (id == R.id.star3) {
            this.star_number = 3;
            setStarBar();
            binding.star4.startAnimation(createDotAnimation());
            binding.star5.startAnimation(createDotAnimation());
        } else if (id == R.id.star4) {
            this.star_number = 4;
            setStarBar();
            if (binding.star4.getAnimation() != null) {
                binding.star4.getAnimation().cancel();
            }
            binding.star5.startAnimation(createDotAnimation());
        } else if (id == R.id.star5) {
            this.star_number = 5;
            setStarBar();
            if (binding.star4.getAnimation() != null) {
                binding.star4.getAnimation().cancel();
            }
            if (binding.star5.getAnimation() != null) {
                binding.star5.getAnimation().cancel();
            }
        }
    }

    private void setStarBar() {
        if (star_number >= 1) binding.star1.setImageResource(R.drawable.ic_star_selected);
        else binding.star1.setImageResource(R.drawable.ic_star_unselected);

        if (star_number >= 2) binding.star2.setImageResource(R.drawable.ic_star_selected);
        else binding.star2.setImageResource(R.drawable.ic_star_unselected);

        if (star_number >= 3) binding.star3.setImageResource(R.drawable.ic_star_selected);
        else binding.star3.setImageResource(R.drawable.ic_star_unselected);

        if (star_number >= 4) binding.star4.setImageResource(R.drawable.ic_star_selected);
        else binding.star4.setImageResource(R.drawable.ic_star_unselected);

        if (star_number >= 5) binding.star5.setImageResource(R.drawable.ic_star_selected);
        else binding.star5.setImageResource(R.drawable.ic_star_unselected);

        // Update bottom text according to rating
        switch (star_number) {
            case 1:
                binding.tvBetter.setText(R.string.oops);
                break;
            case 2:
                binding.tvBetter.setText(R.string.could_be_beter);
                break;
            case 3:
                binding.tvBetter.setText(R.string.better);
                break;
            case 4:
                binding.tvBetter.setText(R.string.good);
                break;
            case 5:
                binding.tvBetter.setText(R.string.awesome);
                break;
            default:
                binding.tvBetter.setText(""); // hide if not selected
                break;
        }

        // Change submit button text
        if (this.star_number < 4) {
            binding.tvSubmit.setText(R.string.feedback);
        } else {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("orientation", true);
            edit.apply();
            binding.tvSubmit.setText(R.string.submit);
        }
    }

    public Animation createDotAnimation() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 0.8f,
                1.0f, 0.8f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setDuration(300L);
        scaleAnimation.setInterpolator(new AccelerateInterpolator());
        return scaleAnimation;
    }
}
