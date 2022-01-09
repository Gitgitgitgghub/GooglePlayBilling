package com.hello.googleplaybilling.billingClient

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Created by Brant on 2022/1/6.
 */
interface IBillingClient<S> :IBillingClientLifecycle{

    /**
     * 查詢可供購買的商品
     * skuType = BillingClient.SkuType
     * skuID 對應googlePlay的產品id
     */
    fun querySku(skuType :String,skuID :List<String>)

    /**
     * 查詢購買的商品
     * skuType = BillingClient.SkuType
     */
    fun queryPurchase(skuType :String)

    /**
     * 購買商品
     */
    fun buy(activity: Activity,skuDetails: S)

    fun isClientReady() :Boolean


}