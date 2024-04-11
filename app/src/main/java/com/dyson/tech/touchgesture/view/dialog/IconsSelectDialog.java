package com.dyson.tech.touchgesture.view.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.IconsListAdapter;
import com.dyson.tech.touchgesture.data.SettingSharedPref;

import java.util.ArrayList;
import java.util.List;

public class IconsSelectDialog extends DialogFragment implements IconsListAdapter.ActionSelectIcon {

    private AppCompatButton btnSelect;
    private RecyclerView rcvIcons;

    private int selectedIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.icon_selection_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        customDialog(getDialog());
    }

    private void initView(View view) {
        SettingSharedPref settingSharedPref = SettingSharedPref.getInstance(getContext());

        btnSelect = view.findViewById(R.id.btn_select);
        rcvIcons = view.findViewById(R.id.rcv_icons);
        IconsListAdapter adapter = new IconsListAdapter(this);
        rcvIcons.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rcvIcons.setAdapter(adapter);
        adapter.setIconsResource(getIconResource());

        btnSelect.setOnClickListener(v -> {
            settingSharedPref.setBtnHomeTheme(selectedIcon);
            dismiss();
        });
    }

    private List<Integer> getIconResource() {
        List<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.btn_home_1);
        icons.add(R.drawable.btn_home_2);
        icons.add(R.drawable.btn_home_3);
        icons.add(R.drawable.btn_home_4);
        icons.add(R.drawable.btn_home_5);
        icons.add(R.drawable.btn_home_7);
        icons.add(R.drawable.btn_home_8);
        icons.add(R.drawable.btn_home_9);
        icons.add(R.drawable.btn_home_10);
        icons.add(R.drawable.btn_home_11);
        return icons;
    }

    @Override
    public void onSelect(int iconResource) {
        new Handler(Looper.getMainLooper()).post(() -> selectedIcon = iconResource);
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
