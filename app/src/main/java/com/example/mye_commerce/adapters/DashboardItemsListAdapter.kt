package com.example.mye_commerce.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.ProductDetailsActivity
import com.example.mye_commerce.model.Product
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader


open class DashboardItemsListAdapter(
    private val context: Context,
    private var list: ArrayList<Product>
) : RecyclerView.Adapter<DashboardItemsListAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var iv_dashboard_item_image:ImageView = view.findViewById(R.id.iv_dashboard_item_image)
        var tv_dashboard_item_title:TextView = view.findViewById(R.id.tv_dashboard_item_title)
        var tv_dashboard_item_price:TextView = view.findViewById(R.id.tv_dashboard_item_price)

    }


    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_dashboard_layout,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            GlideLoader(context).loadProductPicture(
                model.image,
                holder.iv_dashboard_item_image
            )
            holder.tv_dashboard_item_title.text = model.title
            holder.tv_dashboard_item_price.text = "$${model.price}"
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID,model.product_id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID,model.user_id)
                context.startActivity(intent)
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
}