package com.dyson.tech.touchgesture.service;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.dyson.tech.touchgesture.R;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;

public class GoogleBillingService {
    private Activity mActivity;
    private Context mContext;
    private BillingClient mBillingClient;
    private GoogleBillingContract mCallBack;

    public GoogleBillingService(@NonNull Activity activity, @NonNull Context context,GoogleBillingContract callBack) {
        mActivity = activity;
        mContext = context;
        mCallBack = callBack;
    }

    public interface GoogleBillingContract {
        void onPurchaseUpdated(BillingResult billingResult, List<Purchase> purchases);

        void onProductDetailResponse(BillingResult billingResult,
                                     List<ProductDetails> productDetailsList);
    }

    public void initializeBilling() {
        mBillingClient = BillingClient.newBuilder(mContext)
                .setListener((billingResult, purchases) -> mCallBack.onPurchaseUpdated(billingResult, purchases))
                .enablePendingPurchases()
                .build();
    }

    public void queryProducts() {
        if (mBillingClient != null) {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    queryProducts();
                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        QueryProductDetailsParams queryProductDetailsParams =
                                QueryProductDetailsParams.newBuilder()
                                        .setProductList(
                                                ImmutableList.of(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                                .setProductId(mContext.getString(R.string.purchase_id))
                                                                .setProductType(BillingClient.ProductType.INAPP)
                                                                .build()))
                                        .build();

                        mBillingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                (billingResultParam, productDetailsList) -> {
                                    mCallBack.onProductDetailResponse(billingResultParam, productDetailsList);
                                }
                        );
                    }
                }
            });
        }
    }

    public void launchBillingFlow() {
        if (mBillingClient != null) {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    launchBillingFlow();
                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        QueryProductDetailsParams queryProductDetailsParams =
                                QueryProductDetailsParams.newBuilder()
                                        .setProductList(
                                                ImmutableList.of(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                                .setProductId(mContext.getString(R.string.purchase_id))
                                                                .setProductType(BillingClient.ProductType.INAPP)
                                                                .build()))
                                        .build();
//                        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
//                                .setProductList(Collections.singletonList(QueryProductDetailsParams.Product.newBuilder()
//                                        .setProductId(mContext.getString(R.string.purchase_id))
//                                        .setProductType(BillingClient.ProductType.INAPP)
//                                        .build()))
//                                .build();
                        mBillingClient.queryProductDetailsAsync(
                                queryProductDetailsParams,
                                (billingResultParam, productDetailsList) -> {
                                    List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                                            Collections.singletonList(BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetailsList.get(0))
                                                    .setOfferToken(String.valueOf(productDetailsList.get(0).getSubscriptionOfferDetails()))
                                                    .build());

                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setProductDetailsParamsList(productDetailsParamsList)
                                            .build();

                                    mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
                                }
                        );
                    }
                }
            });
        }
    }
}
