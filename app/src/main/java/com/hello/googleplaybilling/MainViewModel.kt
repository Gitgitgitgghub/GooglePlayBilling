package com.hello.googleplaybilling

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.hello.googleplaybilling.billingClient.GooglePlayBillingClient
import com.hello.googleplaybilling.data.SkuDetailItem

/**
 * Created by Brant on 2022/1/6.
 */
class MainViewModel(application: Application) : AndroidViewModel(application),ISkuDetailItemClickListener {


    private val mBillingClient = GooglePlayBillingClient.getInstance(application)

    /**
     * adapter的資料來源 skuDetails轉成SkuDetailItem
     */
    val mSkuDetailsListLiveData = MediatorLiveData<MutableList<SkuDetailItem>?>().apply {
        addSource(mBillingClient.getSkuDetailsLiveData()){ list ->
            value = list?.map {
                SkuDetailItem(it)
            }?.toMutableList()
        }
    }


    fun querySku(skyType :String){
        mBillingClient.querySku(skyType, listOf(SUB_WEEK, SUB_Month))
    }

    override fun onSkuDetailItemClick(position: Int) {
        mSkuDetailsListLiveData.value?.let {
            it.forEachIndexed{ index, skuDetailItem ->
                if (position == index){
                    skuDetailItem.isSelect = !skuDetailItem.isSelect
                }else{
                    skuDetailItem.isSelect = false
                }
            }
            mSkuDetailsListLiveData.value = it
        }
    }

    fun go2Buy(activity: Activity){
        mSkuDetailsListLiveData.value?.let { list ->
            list.find {
                it.isSelect
            }?.data?.let {
                mBillingClient.buy(activity,it)
            }
        }
    }
}