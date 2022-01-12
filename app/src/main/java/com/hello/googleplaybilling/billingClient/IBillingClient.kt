package com.hello.googleplaybilling.billingClient

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Created by Brant on 2022/1/6.
 */
interface IBillingClient :IBillingClientLifecycle{

    fun setSkuType(skuType :String)
    /**
     * 查詢可供購買的商品
     * skuID 對應googlePlay的產品id
     */
    fun querySku()

    /**
     * 查詢購買的商品
     */
    fun queryPurchase()

    /**
     * 購買商品
     */
    fun buy(activity: Activity,skuID :String) :Int

    fun isClientReady() :Boolean


}