package com.example.mye_commerce.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.SoldProductsDetailsActivity
import com.example.mye_commerce.model.SoldProduct
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader

open class SoldProductListAdapter(
    private val context: Context,
    private var list:ArrayList<SoldProduct>
):RecyclerView.Adapter<SoldProductListAdapter.MyViewHolder>(){

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        var tv_item_name: TextView = view.findViewById(R.id.tv_item_name)
        var tv_item_price: TextView = view.findViewById(R.id.tv_item_price)
        var iv_item_image: ImageView = view.findViewById(R.id.iv_item_image)
        var ib_delete_product: ImageButton = view.findViewById(R.id.ib_delete_product)

    }

    override fun onBindViewHolder(holder: SoldProductListAdapter.MyViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            GlideLoader(context).loadProductPicture(
                model.image,
                holder.iv_item_image
            )
            holder.tv_item_name.text = model.title
            holder.tv_item_price.text = "${model.price}"
            holder.ib_delete_product.visibility = View.GONE
            holder.itemView.setOnClickListener {
                val intent = Intent(context, SoldProductsDetailsActivity::class.java)
//                intent.putExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS,model)
                context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return  list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list_layout,
                parent,
                false)
        )
    }

}
