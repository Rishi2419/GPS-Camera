package com.camera.gps.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.camera.gps.R;
import com.camera.gps.activity.Template_Activity;

public final class TemporarySettingsHint {

    private TemporarySettingsHint() {
    }

    public static void bind(Dialog dialog) {
        TextView hint = dialog.findViewById(R.id.tvTemporaryChangesHint);
        if (hint == null) {
            return;
        }

        Context context = dialog.getContext();
        CharSequence localizedText = context.getText(R.string.temporary_changes_template_hint);
        String message = localizedText.toString();
        String templateLink = context.getString(R.string.temporary_changes_template_link);
        int linkStart = message.indexOf(templateLink);
        SpannableString spannable = new SpannableString(message);

        if (linkStart >= 0) {
            int linkEnd = linkStart + templateLink.length();
            int linkColor = ContextCompat.getColor(context, R.color.blue_primary);
            spannable.setSpan(new ForegroundColorSpan(linkColor), linkStart, linkEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new UnderlineSpan(), linkStart, linkEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    dialog.dismiss();
                    context.startActivity(new Intent(context, Template_Activity.class));
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(linkColor);
                    ds.setUnderlineText(true);
                }
            }, linkStart, linkEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        hint.setText(spannable, TextView.BufferType.SPANNABLE);
        hint.setMovementMethod(LinkMovementMethod.getInstance());
        hint.setLinksClickable(true);
        hint.setHighlightColor(Color.TRANSPARENT);
        hint.setVisibility(View.VISIBLE);
    }
}
