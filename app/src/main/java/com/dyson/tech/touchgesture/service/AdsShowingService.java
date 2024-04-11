package com.dyson.tech.touchgesture.service;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Random;

public class AdsShowingService {
    private final Activity mActivity;
    private InterstitialAd mInterstitialAd;

    public AdsShowingService(Activity activity) {
        this.mActivity = activity;

        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(mActivity, mActivity.getString(R.string.intermediate_ads_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public void showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(mActivity);
        } else {
            Log.d("DEBUG", "The interstitial ad wasn't ready yet.");
        }
    }

    public boolean canShowAds() {
        Random random = new Random();
        boolean canShowAds = SettingSharedPref.getInstance(mActivity).getShowAds();
        int randomNumber = random.nextInt(20);
        return randomNumber % 2 == 0 && canShowAds;
    }
}
