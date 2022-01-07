package com.hello.googleplaybilling.data

import com.android.billingclient.api.SkuDetails

/**
 * Created by Brant on 2022/1/7.
 */
data class SkuDetailItem(val data :SkuDetails,var isSelect :Boolean = false)
