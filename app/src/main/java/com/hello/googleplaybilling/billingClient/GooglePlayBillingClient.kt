package com.hello.googleplaybilling.billingClient

import android.app.Activity
import android.app.Application
import android.util.Log
import com.android.billingclient.api.*

/**
 * Created by Brant on 2022/1/6.
 */
class GooglePlayBillingClient private constructor(private val app :Application) :
    BaseBillingClient<SkuDetails,Purchase>(app),
    PurchasesUpdatedListener, BillingClientStateListener,SkuDetailsResponseListener,PurchasesResponseListener{

    companion object{

        @Volatile
        private var INSTANCE : GooglePlayBillingClient? = null
        private const val TAG = "GooglePlayBillingClient"

        fun getInstance(app :Application) : GooglePlayBillingClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GooglePlayBillingClient(app).also { INSTANCE = it }
            }
        }
    }

    private lateinit var mBillingClient :BillingClient

    override fun startClientConnection() {
        mBillingClient = BillingClient.newBuilder(app.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (!mBillingClient.isReady) {
            Log.d(TAG, "BillingClient 開始連線")
            mBillingClient.startConnection(this)
        }
    }

    override fun endClientConnection() {
        if (mBillingClient.isReady){
            Log.d(TAG, "BillingClient 中斷連線")
            mBillingClient.endConnection()
        }
    }

    override fun querySku(skuType :String, skuID :List<String>) {
        if (skuType.isEmpty() || skuID.isEmpty()){
            Log.d(TAG, "查詢可購買商品: skyType or skuID error")
            return
        }else{
            Log.d(TAG, "查詢可購買商品 skuType :$skuType")
            val param = SkuDetailsParams.newBuilder()
                .setSkusList(skuID)
                .setType(skuType)
                .build()
            mBillingClient.querySkuDetailsAsync(param,this)
        }
    }

    override fun queryPurchase(skuType: String) {
        if (skuType.isEmpty()){
            Log.d(TAG, "查詢商品: skyType error")
            return
        }else{
            Log.d(TAG, "查詢商品 skuType :$skuType")
            mBillingClient.queryPurchasesAsync(skuType,this)
        }
    }

    override fun buy(activity: Activity, skuDetails: SkuDetails) {
        val param = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val launchBillingFlow = mBillingClient.launchBillingFlow(activity, param)
        if (launchBillingFlow.responseCode == BillingClient.BillingResponseCode.OK){
            Log.d(TAG, "開始購買: $skuDetails ")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (val responseCode = billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases == null) {
                    Log.d(TAG, "商品更新 :null purchase")
                    purchaseUpdate(null)
                } else {
                    purchases.forEach {
                        Log.d(TAG, "商品更新 :$it")
                    }
                    purchaseUpdate(purchases)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "商品更新 responseCode :$responseCode 用戶取消購買")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "商品更新 responseCode :$responseCode 用戶已經擁有該商品")
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.d(TAG, "商品更新 responseCode :$responseCode DEVELOPER_ERROR")
            }
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetails: MutableList<SkuDetails>?) {
        Log.d(TAG, "querySkuDetails responseCode: ${billingResult.responseCode} skuDetailCount: ${skuDetails?.size}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null){
            skuDetails.forEach {
                Log.d(TAG, "可購買商品: $it")
            }
            querySkuSuccess(skuDetails)
        }
    }

    override fun onQueryPurchasesResponse(BillingResult: BillingResult, purchase: MutableList<Purchase>) {

    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            Log.d(TAG, "BillingClient 初始化完成")
        }else{
            Log.d(TAG, "BillingClient 初始化失敗")
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "BillingClient 連線中斷")
    }
}