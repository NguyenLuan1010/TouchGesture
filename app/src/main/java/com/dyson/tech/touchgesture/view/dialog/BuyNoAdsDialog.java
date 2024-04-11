package com.dyson.tech.touchgesture.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.DialogFragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.dyson.tech.touchgesture.service.GoogleBillingService;

import java.util.List;

public class BuyNoAdsDialog extends Dialog implements GoogleBillingService.GoogleBillingContract {
    private Activity mActivity;
    private AppCompatImageView imgCancel;
    private AppCompatTextView tvPrice;
    private LinearLayoutCompat priceLayout,priceItem;
    private ProgressBar progressBar;
//    private GoogleBillingService mGoogleBillingService;

    public BuyNoAdsDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context);
        mActivity = activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.buy_ads_block_dialog);
        setCancelable(true);

        initView();

        addAttrDialog(BuyNoAdsDialog.this);
    }

    private void initView() {
//        mGoogleBillingService = new GoogleBillingService(mActivity,getContext(), this);
//        mGoogleBillingService.initializeBilling();
//        mGoogleBillingService.queryProducts();
        imgCancel = findViewById(R.id.img_cancel);
        tvPrice = findViewById(R.id.tv_price);
        priceLayout = findViewById(R.id.price_layout);
        priceItem = findViewById(R.id.price_item);
        progressBar = findViewById(R.id.progress_circular);
        loadProductItem();
        imgCancel.setOnClickListener(v -> dismiss());
        priceLayout.setOnClickListener(v -> {
            dismiss();
//            mGoogleBillingService.launchBillingFlow();
        });
    }

    @Override
    public void onPurchaseUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            SettingSharedPref.getInstance(getContext()).setShowAds(false);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
        } else {
        }
    }

    @Override
    public void onProductDetailResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
        ProductDetails productDetails = productDetailsList.get(0);
        new Handler(Looper.getMainLooper()).post(() -> {
            showProductItem();
            tvPrice.setText(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
        });
    }

    private void loadProductItem(){
        progressBar.setVisibility(View.VISIBLE);
        priceItem.setVisibility(View.GONE);
    }
    private void showProductItem(){
        progressBar.setVisibility(View.GONE);
        priceItem.setVisibility(View.VISIBLE);
    }

    public void addAttrDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().
                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
