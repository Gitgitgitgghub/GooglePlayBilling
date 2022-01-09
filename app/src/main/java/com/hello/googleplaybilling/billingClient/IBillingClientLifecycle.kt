package com.hello.googleplaybilling.billingClient

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle

interface IBillingClientLifecycle :DefaultLifecycleObserver {

    /**
     * client連線交給生命週期控制
     */
    fun connectBillingClientWithLifecycle(lifecycle :Lifecycle)

    /**
     * 初始化client
     */
    fun startClientConnection()

    /**
     * 結束client連線
     */
    fun endClientConnection()

}