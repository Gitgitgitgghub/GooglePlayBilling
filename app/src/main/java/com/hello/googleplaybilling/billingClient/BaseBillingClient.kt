package com.hello.googleplaybilling.billingClient

import android.app.Application
import android.util.Log
import androidx.lifecycle.*

/**
 * Created by Brant on 2022/1/7.
 */
abstract class BaseBillingClient<S,P>(private val app: Application) : IBillingClient<S,P>,DefaultLifecycleObserver{

    private val mSkuDetailsLiveData = MutableLiveData<List<S>?>()

    override fun getSkuDetailsLiveData(): LiveData<List<S>?> {
        return mSkuDetailsLiveData.map {
            it
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        startClientConnection()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        endClientConnection()
    }

    protected fun querySkuSuccess(skuList: MutableList<S>?){
        mSkuDetailsLiveData.value = skuList
    }

    protected fun purchaseUpdate(purchaseList :MutableList<P>?){

    }

    /**
     * 初始化client
     */
    abstract fun startClientConnection()

    /**
     * 結束client連線
     */
    abstract fun endClientConnection()
}