package com.hello.googleplaybilling

import android.app.Application
import com.hello.googleplaybilling.billingClient.GooglePlayBillingClient

/**
 * Created by Brant on 2022/1/6.
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        GooglePlayBillingClient.getInstance(this)
    }
}