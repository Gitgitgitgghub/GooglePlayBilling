package com.hello.googleplaybilling.billingClient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

abstract class CustomBillingClient<S,P> : BillingClientLifecycle<S>() {

    private val TAG = "CustomBillingClient"
    private val mSkuDetailsLiveData = MutableLiveData<List<S>?>()

    fun getSkuDetailsLiveData(): LiveData<List<S>?> {
        return mSkuDetailsLiveData
    }

    protected fun querySkuSuccess(skuList: MutableList<S>?){
        skuList?.let { list ->
            list.forEach {
                Log.d(TAG, "可購買商品: $it")
            }
        }
        Log.d(TAG, "可購買商品數量: ${skuList?.size ?: 0}")
        mSkuDetailsLiveData.postValue(skuList)
    }

    protected fun purchaseUpdate(purchaseList :MutableList<P>?){
        if (purchaseList == null){
            Log.d(TAG, "商品更新: purchase null")
        }else{
            purchaseList.forEach {
                Log.d(TAG, "商品更新: $it")
            }
        }
    }

}