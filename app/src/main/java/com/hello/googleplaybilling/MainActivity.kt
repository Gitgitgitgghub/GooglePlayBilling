package com.hello.googleplaybilling

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
        lifecycle.addObserver(GooglePlayBillingClient.getInstance(application))
        initView()
        observeSkuDetail()
    }

    private fun initView(){
        mBinding.btnQuery.setOnClickListener(this)
        mBinding.btnBuy.setOnClickListener(this)
        mBinding.rvSkuDetail.layoutManager = LinearLayoutManager(this)
        mBinding.rvSkuDetail.adapter = SkuAdapter(mViewModel).also {
            mAdapter = it
        }
    }

    private fun observeSkuDetail(){
        mViewModel.mSkuDetailsListLiveData.observe(this){
            it?.let {
                mAdapter.addSku(it.toMutableList())
            }
        }
    }

    override fun onClick(v: View) {
        when(v){
            mBinding.btnQuery -> mViewModel.querySku(BillingClient.SkuType.SUBS)
            mBinding.btnBuy -> mViewModel.go2Buy(this)
        }
    }
}