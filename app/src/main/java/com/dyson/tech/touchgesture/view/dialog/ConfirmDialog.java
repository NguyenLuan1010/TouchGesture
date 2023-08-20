package com.dyson.tech.touchgesture.view.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

public class ConfirmDialog {

    private AlertDialog.Builder builder;
    private Dialog dialog;
    private Context context;
    @SuppressLint("StaticFieldLeak")
    private static ConfirmDialog confirmDialog;
    private OnClickPositiveButton onClickButtonDialog;


    public static ConfirmDialog getInstance(Context context) {
        if (confirmDialog == null) {
            confirmDialog = new ConfirmDialog();
        }
        confirmDialog.builder = new AlertDialog.Builder(context);
        return confirmDialog;
    }

    public ConfirmDialog setTitle(String appName) {
        builder.setTitle(appName);
        return confirmDialog;
    }

    public ConfirmDialog setMessage(String message) {
        builder.setMessage(message);
        return confirmDialog;
    }

    public ConfirmDialog setIcon(int icon) {
        builder.setIcon(icon);
        return confirmDialog;
    }

    public ConfirmDialog setPositiveButton(String titlePositiveButton,
                                           OnClickPositiveButton onClickButtonDialog) {
        builder.setPositiveButton(titlePositiveButton, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if(onClickButtonDialog != null){
                onClickButtonDialog.onClick();
            }
        });
        return confirmDialog;
    }

    public ConfirmDialog setNegativeButton(String titleNegativeButton,
                                           OnClickNegativeButton listener) {
        builder.setNegativeButton(titleNegativeButton, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if(listener != null){
                listener.onClick();
            }
        });
        return confirmDialog;
    }

    public ConfirmDialog create() {
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return confirmDialog;
    }

    public interface OnClickNegativeButton {
        void onClick();
    }

    public interface OnClickPositiveButton {
        void onClick();
    }
}
