package com.hello.googleplaybilling

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.hello.googleplaybilling.data.SkuDetailItem
import com.hello.googleplaybilling.databinding.ItemSkuDetailBinding

/**
 * Created by Brant on 2022/1/6.
 */
class SkuAdapter(val clickListener :ISkuDetailItemClickListener) : RecyclerView.Adapter<SkuAdapter.Companion.SkuViewHolder>() {

    companion object{
        class SkuViewHolder(private val binding: ItemSkuDetailBinding) : RecyclerView.ViewHolder(binding.root){

            fun bindSku(skuDetailItem: SkuDetailItem ,clickListener :ISkuDetailItemClickListener){
                val skuDetails = skuDetailItem.data
                binding.tvProductID.text = skuDetails.sku
                binding.tvProductType.text = skuDetails.type
                binding.tvProductTitle.text = skuDetails.title
                binding.tvProductPrice.text = skuDetails.price
                binding.rbSelect.isChecked = skuDetailItem.isSelect
                binding.root.setOnClickListener {
                    clickListener.onSkuDetailItemClick(layoutPosition)
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

    @SuppressLint("NotifyDataSetChanged")
    fun clearSku(){
        mData = null
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addSku(skuDetailsList: MutableList<SkuDetailItem>){
        mData = skuDetailsList
        notifyDataSetChanged()
    }
}