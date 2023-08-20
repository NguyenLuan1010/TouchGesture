package com.dyson.tech.touchgesture.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.dyson.tech.touchgesture.R;

public class UsageStatsPermissionDialog extends Dialog {

    private TextView tvCancel, tvGetPermission;
    private ActionClickGetPermissionDialog listener;

    public UsageStatsPermissionDialog(@NonNull Context context) {
        super(context);
    }

    public void setListener(ActionClickGetPermissionDialog listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.app_usage_stats_permission_dialog);
        setCancelable(false);
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvGetPermission = (TextView) findViewById(R.id.tv_get_permission);

        eventClick();
        customDialog(UsageStatsPermissionDialog.this);
    }

    private void eventClick() {
        tvCancel.setOnClickListener(view -> {
            this.dismiss();
            listener.onClickCancel();
        });

        tvGetPermission.setOnClickListener(view -> {
            this.dismiss();
            listener.onClickGetPermission();
        });
    }

    private void customDialog(Dialog dialog) {
        dialog.getWindow().

                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().

                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().

                getAttributes().windowAnimations = androidx.appcompat.R.style.Animation_AppCompat_Dialog;
    }

    public interface ActionClickGetPermissionDialog {
        void onClickCancel();

        void onClickGetPermission();
    }
}
