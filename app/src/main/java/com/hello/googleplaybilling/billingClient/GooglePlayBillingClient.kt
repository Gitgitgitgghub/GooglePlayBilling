package com.hello.googleplaybilling.billingClient

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.hello.googleplaybilling.data.InAppProduct
import com.hello.googleplaybilling.data.SubProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.NullPointerException

/**
 * Created by Brant on 2022/1/6.
 */
class GooglePlayBillingClient private constructor(private val app :Application) :
    BillingClientLifecycle(),
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

    private var mSkuType = BillingClient.SkuType.SUBS
    private val mSkuDetailsListLiveData = MutableLiveData<List<SkuDetails>?>()
    private val mPurchaseListLiveData = MutableLiveData<List<Purchase>?>()


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

    override fun setSkuType(skuType :String) {
        mSkuType = skuType
    }

    override fun querySku() {
        if (!isClientReady()) return
        Log.d(TAG, "查詢可購買商品 skuType :$mSkuType")
        val skuIDList = getSkuIDList() ?: return
        val param = SkuDetailsParams.newBuilder()
            .setSkusList(skuIDList)
            .setType(mSkuType)
            .build()
        launch(Dispatchers.IO){
            mBillingClient.querySkuDetailsAsync(param,this@GooglePlayBillingClient)
        }
    }

    override fun queryPurchase() {
        if (!isClientReady()) return
        if (mSkuType.isEmpty()){
            Log.d(TAG, "查詢商品: skyType error")
            return
        }else{
            launch(Dispatchers.IO){
                Log.d(TAG, "查詢商品 skuType :$mSkuType")
                mBillingClient.queryPurchasesAsync(mSkuType,this@GooglePlayBillingClient)
            }
        }
    }

    override fun buy(activity: Activity, skuID :String) :Int {
        val skuDetails = getSkuByID(skuID)
        if (skuDetails != null){
            val param = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            val responseCode = mBillingClient.launchBillingFlow(activity, param).responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK){
                Log.d(TAG, "開始購買流程: $skuDetails ")
            }
            return responseCode
        }
        throw NullPointerException("開始購買流程: skuDetails is null")
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        logBillingResultMessage(billingResult,"商品更新: 數量${purchases?.size}")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                updatePurchases(purchases)
                handlePurchase(purchases)
            }
        }
    }

    override fun onSkuDetailsResponse(billingResult: BillingResult, skuDetails: MutableList<SkuDetails>?) {
        logBillingResultMessage(billingResult,"可購買商品數量 :${skuDetails?.size}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            mSkuDetailsListLiveData.postValue(skuDetails)
            skuDetails?.forEach {
                Log.d(TAG, "可購買商品 :$it")
            }
        }
    }

    override fun onQueryPurchasesResponse(billingResult: BillingResult, purchase: MutableList<Purchase>) {
        logBillingResultMessage(billingResult,"onQueryPurchasesResponse")
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                updatePurchases(purchase)
                handlePurchase(purchase)
            }
        }
    }

    /**
     * 重要
     * 收到訂單更新後
     * 1.消耗型商品要執行consumeAsync，否則無法再次購買
     * 2.非消耗型(訂閱也算非消耗)要執行acknowledgePurchase
     */
    private fun handlePurchase(purchaseList :MutableList<Purchase>?){
        purchaseList?.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
                val type = getSkuByID(purchase.skus.first())?.type ?:return
                when (type){
                    BillingClient.SkuType.INAPP -> consumePurchase(purchase)
                    BillingClient.SkuType.SUBS -> acknowledgePurchase(purchase)
                }
            }
        }
    }

    private fun consumePurchase(purchase :Purchase){
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        mBillingClient.consumeAsync(consumeParams) { billingResult, token ->
            logBillingResultMessage(billingResult, "消耗商品 token:${token}")
        }
    }

    private fun acknowledgePurchase(purchase :Purchase){
        if (!purchase.isAcknowledged){
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            mBillingClient.acknowledgePurchase(params) { billingResult ->
                logBillingResultMessage(billingResult,"確認訂單 :${purchase.skus.first()}")
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        logBillingResultMessage(billingResult,"初始化完成")
        querySku()
        queryPurchase()
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "BillingClient 連線中斷")
        startClientConnection()
    }

    private fun getSkuIDList() = when(mSkuType) {
        BillingClient.SkuType.INAPP -> InAppProduct.skuIDList
        BillingClient.SkuType.SUBS -> SubProduct.skuIDList
        else -> null
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


    fun getSkuDetailsLiveData(): LiveData<List<SkuDetails>?> {
        return mSkuDetailsListLiveData
    }

    fun getPurchaseLiveData() :LiveData<List<Purchase>?> {
        return mPurchaseListLiveData
    }

    fun updatePurchases(purchase: MutableList<Purchase>?){
        mPurchaseListLiveData.postValue(purchase)
        purchase?.forEach {
            Log.d(TAG, "商品更新 ${it.skus.first()} : $it")
        }
    }

    fun getSkuByID(skuID :String) :SkuDetails?{
        mSkuDetailsListLiveData.value?.forEach {
            if (it.sku == skuID) return it
        }
        return null
    }
}