package com.hello.googleplaybilling

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.android.billingclient.api.BillingClient
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
        addSource(mBillingClient.getPurchaseLiveData()){ list ->
            value?.forEach { item ->
                item.purchase = null
                list?.forEach {
                    if (it.skus.first() == item.skuDetail.sku){
                        item.purchase = it
                    }
                }
                value = value
            }
        }
    }

    val mOpenGooglePlayStoreSubscriptionUriLiveData = SingleLiveEvent<Uri>()

    val mSkuTypeLiveData = MutableLiveData(BillingClient.SkuType.SUBS)

    fun setSkuType(skuType :String){
        mSkuTypeLiveData.value = skuType
        mBillingClient.setSkuType(skuType)
        query()
    }


    fun query(){
        querySku()
        queryPurchase()
    }

    fun querySku(){
        mBillingClient.querySku()
    }

    fun queryPurchase(){
        mBillingClient.queryPurchase()
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
            }?.let {
                if (it.needOpenGooglePlaySubscriptionPage()){
                    it.getGooglePlaySubscriptionPageUri()?.let { uri ->
                        mOpenGooglePlayStoreSubscriptionUriLiveData.value = uri
                    }
                }else{
                    mBillingClient.buy(activity,it.skuDetail.sku)
                }
            }
        }
    }
}