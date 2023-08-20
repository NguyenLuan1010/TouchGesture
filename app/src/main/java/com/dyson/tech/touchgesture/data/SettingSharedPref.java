package com.dyson.tech.touchgesture.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.dyson.tech.touchgesture.R;


public class SettingSharedPref {

    private static final String SHARED_NAME = "SettingSharedPref";
    private static final String BTN_HOME_THEME = "BTN_HOME_THEME";
    private static final String LANGUAGE = "LANGUAGE";

    private static SettingSharedPref instance;

    private SettingSharedPref(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_NAME, 0);
    }

    public static synchronized SettingSharedPref getInstance(Context context) {
        if (instance == null) {
            instance = new SettingSharedPref(context);
        }
        return instance;
    }

    private final SharedPreferences mSharedPreferences;
    private BtnHomeChangeListener listener;

    private final SharedPreferences.OnSharedPreferenceChangeListener
            onSharedPreferenceChangeListener = (sharedPreferences, key) -> {
                if (key.equals(BTN_HOME_THEME) && listener != null) {
                    listener.onChange(getBtnHomeTheme());
                }
            };

    public void setBtnHomeTheme(@DrawableRes int value) {
        mSharedPreferences.edit().putInt(BTN_HOME_THEME, value).apply();
    }

    @DrawableRes
    public int getBtnHomeTheme() {
        return mSharedPreferences.getInt(BTN_HOME_THEME, R.raw.icon_16);
    }

    public void setOnBtnHomeChangeListener(BtnHomeChangeListener mListener) {
        this.listener = mListener;
        mSharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void removeOnBtnHomeChangeListener() {
        listener = null;
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void setLanguage(String language){
        mSharedPreferences.edit().putString(LANGUAGE, language).apply();
    }

    public String getLanguage(){
        return mSharedPreferences.getString(LANGUAGE,"English");
    }

    public interface BtnHomeChangeListener {
        void onChange(int value);
    }

}
