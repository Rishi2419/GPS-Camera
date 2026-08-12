package com.camera.gps.premium;

import android.app.Activity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.camera.gps.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/** Temporary entitlement UI. Replace its switch with the Play Billing purchase result later. */
public final class PremiumTestBottomSheet {

    public interface Listener {
        void onPremiumChanged(boolean premium);

        void onContinueFree();
    }

    private PremiumTestBottomSheet() {
    }

    public static void show(Activity activity, Listener listener) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.bottom_sheet_premium_test);

        Switch premiumSwitch = dialog.findViewById(R.id.switchPremiumTest);
        TextView status = dialog.findViewById(R.id.tvPremiumTestStatus);
        TextView continueFree = dialog.findViewById(R.id.btnContinueFree);
        View close = dialog.findViewById(R.id.btnClosePremiumTest);

        boolean premium = PremiumManager.isPremium(activity);
        if (premiumSwitch != null) {
            premiumSwitch.setChecked(premium);
        }
        updateStatus(status, premium);
        if (continueFree != null) {
            continueFree.setVisibility(premium ? View.GONE : View.VISIBLE);
            continueFree.setOnClickListener(v -> {
                PremiumManager.resetToDefaultTemplate(activity);
                if (listener != null) {
                    listener.onContinueFree();
                }
                dialog.dismiss();
            });
        }

        if (premiumSwitch != null) {
            premiumSwitch.setOnCheckedChangeListener((CompoundButton button, boolean checked) -> {
                PremiumManager.setPremium(activity, checked);
                updateStatus(status, checked);
                if (listener != null) {
                    listener.onPremiumChanged(checked);
                }
                dialog.dismiss();
            });
        }
        if (close != null) {
            close.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private static void updateStatus(TextView status, boolean premium) {
        if (status != null) {
            status.setText(premium
                    ? R.string.premium_test_enabled
                    : R.string.premium_test_disabled);
        }
    }
}
