package com.hello.googleplaybilling.billingClient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

abstract class CustomBillingClient<S,P> : BillingClientLifecycle() {

    private val TAG = "CustomBillingClient"
    private val mSkuDetailsLiveData = MutableLiveData<Map<String,S>?>()
    private val mPurchaseLiveData = MutableLiveData<Map<String,P>?>()

    fun getSkuDetailsLiveData(): LiveData<List<S>?> {
        return mSkuDetailsLiveData.map {
            it?.values?.toList()
        }
    }

    fun getSkuByID(skuID :String) :S?{
        return mSkuDetailsLiveData.value?.get(skuID)
    }

    protected fun querySkuSuccess(skuMap: Map<String,S>?){
        Log.d(TAG, "可購買商品數量: ${skuMap?.size ?: 0}")
        skuMap?.let { list ->
            list.forEach {
                Log.d(TAG, "可購買商品: $it")
            }
        }
        mSkuDetailsLiveData.postValue(skuMap)
    }

    protected fun purchaseUpdate(purchaseMap: Map<String,P>?){
        if (purchaseMap == null){
            Log.d(TAG, "商品更新: purchase null")
        }else{
            purchaseMap.values.forEach {
                Log.d(TAG, "商品更新: $it")
            }
            mPurchaseLiveData.postValue(purchaseMap)
        }
    }

}