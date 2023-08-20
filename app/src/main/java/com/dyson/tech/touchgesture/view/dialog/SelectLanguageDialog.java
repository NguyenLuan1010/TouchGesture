package com.dyson.tech.touchgesture.view.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.LanguageListAdapter;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.dyson.tech.touchgesture.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectLanguageDialog {
    private Activity activity;
    @SuppressLint("StaticFieldLeak")
    private static SelectLanguageDialog selectLanguageDialog;
    private Dialog dialog;
    private RecyclerView rcvLanguageList;
    private TextView tvApplyLanguage;
    private LanguageListAdapter adapter;

    public static synchronized SelectLanguageDialog init() {
        if (selectLanguageDialog == null) {
            selectLanguageDialog = new SelectLanguageDialog();
        }
        return selectLanguageDialog;
    }

    public void showDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.select_language_dialog);
        dialog.setCancelable(true);

        initView(dialog);
        dialog.show();
        customDialog(dialog);
    }

    private void initView(Dialog dialog) {
        rcvLanguageList = dialog.findViewById(R.id.rcv_language);
        tvApplyLanguage = dialog.findViewById(R.id.tv_apply);

        List<String> languages = new ArrayList<>();
        String lngEnglish = activity.getString(R.string.english);
        String lngVietName = activity.getString(R.string.viet_nam);
        languages.add(lngEnglish);
        languages.add(lngVietName);
        adapter = new LanguageListAdapter();
        rcvLanguageList.setAdapter(adapter);
        adapter.setLanguages(languages);

        tvApplyLanguage.setOnClickListener(view -> {
            String language = adapter.getLanguageSelected();

            if (language.equals(lngEnglish)) {
                selectLanguage("en");
            } else if (language.equals(lngVietName)) {
                selectLanguage("vi");
            }
        });
    }

    private void selectLanguage(String languageCode) {
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        saveLanguageChange(languageCode);
    }

    private void saveLanguageChange(String language) {
        SettingSharedPref settingSharedPref= SettingSharedPref.getInstance(activity);
        settingSharedPref.setLanguage(language);

        activity.finish();
        activity.startActivity(new Intent(activity, MainActivity.class));

        dialog.dismiss();
    }

    private void customDialog(Dialog dialog) {

        dialog.getWindow().

                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().

                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().

                getAttributes().windowAnimations = androidx.appcompat.R.style.Animation_AppCompat_Dialog;
    }

}
