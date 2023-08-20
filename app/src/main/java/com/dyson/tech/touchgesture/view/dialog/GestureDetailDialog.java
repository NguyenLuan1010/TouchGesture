package com.dyson.tech.touchgesture.view.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.model.Apps;

public class GestureDetailDialog extends DialogFragment {

    private final Apps app;

    public GestureDetailDialog(Apps app) {
        this.app = app;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gesture_detail_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bitmap bitmap = app.getGesture()
                .toBitmap(200, 200, 0, getContext().getColor(R.color.orange));
        ImageView gestureView  = getDialog().findViewById(R.id.img_gesture);
        gestureView.setImageBitmap(bitmap);
        customDialog(getDialog());
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
}
