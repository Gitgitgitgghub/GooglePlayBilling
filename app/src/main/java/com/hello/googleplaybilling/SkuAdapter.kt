package com.hello.googleplaybilling

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.hello.googleplaybilling.data.SkuDetailItem
import com.hello.googleplaybilling.databinding.ItemSkuDetailBinding

/**
 * Created by Brant on 2022/1/6.
 */
class SkuAdapter(private val clickListener :ISkuDetailItemClickListener) : RecyclerView.Adapter<SkuAdapter.Companion.SkuViewHolder>() {

    companion object{
        class SkuViewHolder(private val binding: ItemSkuDetailBinding) : RecyclerView.ViewHolder(binding.root){

            fun bindSku(skuDetailItem: SkuDetailItem ,clickListener :ISkuDetailItemClickListener){
                val skuDetails = skuDetailItem.skuDetail
                binding.tvProductID.text = skuDetails.sku
                binding.tvProductTitle.text = skuDetails.title.substringBefore("(")
                binding.tvProductPrice.text = skuDetails.price
                binding.rbSelect.isChecked = skuDetailItem.isSelect
                binding.root.setOnClickListener {
                    clickListener.onSkuDetailItemClick(layoutPosition)
                }
                if (skuDetails.type == BillingClient.SkuType.SUBS){
                    binding.tvAutoRenewind.visibility = View.VISIBLE
                    binding.tvAutoRenewind.text = "續訂 :${if (skuDetailItem.isAutoRenewing()) "是" else "否"}"
                }else{
                    binding.tvAutoRenewind.visibility = View.GONE
                }
            }
        }
    }

    private var mData :MutableList<SkuDetailItem>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkuViewHolder {
        return SkuViewHolder(ItemSkuDetailBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: SkuViewHolder, position: Int) {
        mData ?:return
        holder.bindSku(mData!![position],clickListener)
    }

    override fun getItemCount() = if (mData == null) 0 else mData!!.size

    fun clearSku(){
        addSku(null)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addSku(skuDetailsList: MutableList<SkuDetailItem>?){
        mData = skuDetailsList
        notifyDataSetChanged()
    }
}