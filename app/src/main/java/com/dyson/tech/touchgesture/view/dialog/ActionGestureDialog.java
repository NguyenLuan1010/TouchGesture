package com.dyson.tech.touchgesture.view.dialog;

import android.app.Dialog;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.GestureFilesHelper;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.utils.PermissionUtils;
import com.dyson.tech.touchgesture.view.AuthenticationCallBack;

public class ActionGestureDialog extends DialogFragment
        implements AuthenticationCallBack.AddGestureCallBack, AuthenticationCallBack.EditGestureFileCallBack {
    private GestureOverlayView gestureOverlayView;
    private AppCompatButton btnSubmit;
    private LottieAnimationView animationView;
    private ProgressBar progressBar;
    private GestureFilesHelper gestureFilesHelper;
    private OnUpdatedGesture callback;
    private final Apps app;
    private final String title;
    private final String description;

    public ActionGestureDialog(String title, String description, Apps app, OnUpdatedGesture callback) {
        this.app = app;
        this.title = title;
        this.description = description;
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        gestureFilesHelper = new GestureFilesHelper(getContext());
        return inflater.inflate(R.layout.action_gesture_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        customDialog(getDialog());
    }

    private void initView(View view) {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        progressBar = view.findViewById(R.id.progress_circular);
        gestureOverlayView = view.findViewById(R.id.gesture_overlay_view);
        btnSubmit = view.findViewById(R.id.btn_add_new);
        animationView = view.findViewById(R.id.lottie_file);
        setCancelable(true);
        new Handler().postDelayed(() -> animationView.setVisibility(View.GONE), 2000);

        tvTitle.setText(title);
        tvDescription.setText(description);

        btnSubmit.setOnClickListener(viewRoot -> {
            PermissionUtils.requestPackageWriteExternalPermission(this, (allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    onClickSubmit();
                }
            });
            onClickSubmit();
        });
    }

    private void onClickSubmit() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.INVISIBLE);

        app.setGesture(gestureOverlayView.getGesture());
        if (title.equals(getString(R.string.edit_gesture))) {
            gestureFilesHelper.editGesture(app, this);
        } else if (title.equals(getString(R.string.add_gesture))) {
            gestureFilesHelper.addToFile(app, this);
        }
    }

    @Override
    public void addSuccess(String message) {
        progressBar.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        dismiss();
    }

    @Override
    public void addFail(String message) {
        progressBar.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), message + " from version name 10.0", Toast.LENGTH_LONG).show();
        dismiss();
    }

    @Override
    public void editSuccess(Apps updatedApp, String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (callback != null)
            callback.updatedGesture(updatedApp);
        dismiss();
    }

    @Override
    public void editFail(String message) {
        progressBar.setVisibility(View.INVISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        dismiss();
    }

    private void customDialog(Dialog dialog) {
        dialog.getWindow().

                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().

                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().

                getAttributes().windowAnimations = androidx.appcompat.R.style.Animation_AppCompat_Dialog;
        dialog.getWindow().

                setGravity(Gravity.BOTTOM);
    }

    public interface OnUpdatedGesture {
        void updatedGesture(Apps updatedApp);
    }
}
