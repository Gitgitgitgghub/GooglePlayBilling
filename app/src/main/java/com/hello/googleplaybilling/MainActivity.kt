package com.hello.googleplaybilling

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.hello.googleplaybilling.billingClient.GooglePlayBillingClient
import com.hello.googleplaybilling.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private lateinit var mBinding : ActivityMainBinding
    private lateinit var mViewModel :MainViewModel
    private lateinit var mAdapter: SkuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        GooglePlayBillingClient.getInstance(application).connectBillingClientWithLifecycle(lifecycle)
        initView()
        observeSkuType()
        observeSkuDetail()
        observeOpenGooglePlaySubscription()
    }

    private fun initView(){
        mBinding.btnSwitchSkuType.setOnClickListener(this)
        mBinding.btnQuery.setOnClickListener(this)
        mBinding.btnBuy.setOnClickListener(this)
        mBinding.rvSkuDetail.layoutManager = LinearLayoutManager(this)
        mBinding.rvSkuDetail.adapter = SkuAdapter(mViewModel).also {
            mAdapter = it
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeSkuType(){
        mViewModel.mSkuTypeLiveData.observe(this){
            mBinding.tvSkuType.text = "SkuType :$it"
        }
    }

    private fun observeSkuDetail(){
        mViewModel.mSkuDetailsListLiveData.observe(this){
            mAdapter.addSku(it)
        }
    }

    private fun observeOpenGooglePlaySubscription(){
        mViewModel.mOpenGooglePlayStoreSubscriptionUriLiveData.observe(this){
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = it
            })
        }
    }

    override fun onClick(v: View) {
        when(v){
            mBinding.btnSwitchSkuType -> createSwitchSkuTypeDialog()
            mBinding.btnQuery -> mViewModel.queryPurchase()
            mBinding.btnBuy -> mViewModel.go2Buy(this)
        }
    }

    private fun createSwitchSkuTypeDialog(){
        val items = arrayOf(BillingClient.SkuType.INAPP,BillingClient.SkuType.SUBS)
        AlertDialog.Builder(this)
            .setTitle("切換SkuType")
            .setItems(items,DialogInterface.OnClickListener { _, which ->
                mViewModel.setSkuType(items[which])
            })
            .show()
    }
}