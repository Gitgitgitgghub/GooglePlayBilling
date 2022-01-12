package com.hello.googleplaybilling.data

import android.net.Uri
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.hello.googleplaybilling.MyApp

/**
 * Created by Brant on 2022/1/7.
 */
data class SkuDetailItem(var skuDetail :SkuDetails,
                         var purchase :Purchase? = null,
                         var isSelect :Boolean = false, ){
    /**
     * 自動續訂
     */
    fun isAutoRenewing() = purchase?.isAutoRenewing ?: false

    fun needOpenGooglePlaySubscriptionPage() :Boolean{
        purchase?.let {
            return it.isAutoRenewing
        }
        return false
    }

    fun getGooglePlaySubscriptionPageUri() :Uri? {
        purchase?.let {
            return Uri.parse("https://play.google.com/store/account/subscriptions?sku=${skuDetail.sku}" +
                    "&package=${it.packageName}")
        }
        return null
    }
}
