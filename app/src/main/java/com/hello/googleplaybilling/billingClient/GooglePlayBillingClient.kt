package com.hello.googleplaybilling.billingClient

import android.app.Activity
import android.app.Application
import android.util.Log
import com.android.billingclient.api.*

/**
 * Created by Brant on 2022/1/6.
 */
class GooglePlayBillingClient private constructor(private val app :Application) :
    CustomBillingClient<SkuDetails,Purchase>(),
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

    private val mBillingClient :BillingClient by lazy {
        BillingClient.newBuilder(app.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    override fun startClientConnection() {
        if (!isClientReady()) {
            Log.d(TAG, "BillingClient 開始連線")
            mBillingClient.startConnection(this)
        }
    }

    override fun endClientConnection() {
        if (isClientReady()){
            Log.d(TAG, "BillingClient 中斷連線")
            mBillingClient.endConnection()
        }
    }

    override fun querySku(skuType :String, skuID :List<String>) {
        if (skuType.isEmpty() || skuID.isEmpty()){
            Log.d(TAG, "查詢可購買商品: skyType or skuID error")
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
            Log.d(TAG, "開始購買流程: $skuDetails ")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        logBillingResultMessage(billingResult,"商品更新")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchaseUpdate(purchases)
            }
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetails: MutableList<SkuDetails>?) {
        logBillingResultMessage(billingResult,"onSkuDetailsResponse")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            querySkuSuccess(skuDetails)
        }
    }

    override fun onQueryPurchasesResponse(BillingResult: BillingResult, purchase: MutableList<Purchase>) {
        logBillingResultMessage(BillingResult,"onQueryPurchasesResponse")
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        logBillingResultMessage(billingResult,"初始化")
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "BillingClient 連線中斷")
        startClientConnection()
    }

    override fun isClientReady() = mBillingClient.isReady

    private fun logBillingResultMessage(billingResult: BillingResult, actionName :String){
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        val message = when (responseCode) {
            BillingClient.BillingResponseCode.OK -> "成功"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "服務斷線"
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            BillingClient.BillingResponseCode.ERROR -> "ERROR"
            BillingClient.BillingResponseCode.USER_CANCELED -> "用戶取消"
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "已擁有該商品"
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "未擁有該商品"
            else -> "responseCode不明"
        }
        Log.i(TAG, "$actionName responseCode: $responseCode /message: $message /debugMessage: $debugMessage")
    }
}