package com.hello.googleplaybilling.billingClient

import android.app.Application
import android.util.Log
import androidx.lifecycle.*

/**
 * Created by Brant on 2022/1/7.
 * 主要用來定義用生命週期控管client的連線
 */
abstract class BillingClientLifecycle<S>() : IBillingClient<S>{

    override fun connectBillingClientWithLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        startClientConnection()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        endClientConnection()
        owner.lifecycle.removeObserver(this)
    }

}