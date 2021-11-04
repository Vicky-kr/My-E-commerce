package com.example.mye_commerce.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.CartListActivity
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.CartItem
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader

class CartItemListAdapter(
    private val context: Context,
    private val list: List<CartItem>,
    private var updateCartItem: Boolean
):RecyclerView.Adapter<CartItemListAdapter.MyViewHolder>() {

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        var tv_cart_item_title:TextView = view.findViewById(R.id.tv_cart_item_title);
        var tv_cart_item_price:TextView = view.findViewById(R.id.tv_cart_item_price);
        var iv_cart_item_image:ImageView = view.findViewById(R.id.iv_cart_item_image);
        var ib_remove_cart_item:ImageButton = view.findViewById(R.id.ib_remove_cart_item);
        var ib_add_cart_item:ImageButton = view.findViewById(R.id.ib_add_cart_item);
        var tv_cart_quantity:TextView = view.findViewById(R.id.tv_cart_quantity);
        var ib_delete_cart_item:ImageButton = view.findViewById(R.id.ib_delete_cart_item);


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(
            R.layout.item_cart_layout,
            parent,
            false)
        )
    }

    override fun getItemCount(): Int {
        return list.size ;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            GlideLoader(context).loadProductPicture(model.image!!, holder.iv_cart_item_image)

            holder.tv_cart_item_title.text = model.title
            holder.tv_cart_item_price.text = "$${model.price}"
            holder.tv_cart_quantity.text = model.cart_quantity

            if (model.cart_quantity == "0") {
                holder.ib_remove_cart_item.visibility = View.GONE
                holder.ib_add_cart_item.visibility = View.GONE
                if (updateCartItem){
                    holder.ib_delete_cart_item.visibility = View.VISIBLE
                }else{
                    holder.ib_remove_cart_item.visibility = View.GONE
                }

                holder.tv_cart_quantity.text =
                    context.resources.getString(R.string.lbl_out_of_stock)

                holder.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSnackBarError
                    )
                )
            } else {
                if (updateCartItem){
                    holder.ib_remove_cart_item.visibility = View.VISIBLE
                    holder.ib_add_cart_item.visibility = View.VISIBLE
                    holder.ib_delete_cart_item.visibility = View.VISIBLE

                }else{
                    holder.ib_remove_cart_item.visibility = View.GONE
                    holder.ib_add_cart_item.visibility = View.GONE
                    holder.ib_delete_cart_item.visibility = View.GONE

                }


                holder.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )
            }

            // TODO Step 1: Assign the click event to the ib_remove_cart_item.
            // START
            holder.ib_remove_cart_item.setOnClickListener {

                // TODO Step 6: Call the update or remove function of firestore class based on the cart quantity.
                // START
                if (model.cart_quantity == "1") {
                    FireStoreClass().removeItemFromCart(context, model.id!!)
                } else {

                    val cartQuantity: Int = model.cart_quantity!!.toInt()

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                    // Show the progress dialog.

                    if (context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FireStoreClass().updateMyCart(context, model.id!!, itemHashMap)
                }
                // END
            }
            // END

            // TODO Step 7: Assign the click event to the ib_add_cart_item.
            // START
            holder.ib_add_cart_item.setOnClickListener {

                // TODO Step 8: Call the update function of firestore class based on the cart quantity.
                // START
                val cartQuantity: Int = model.cart_quantity!!.toInt()

                if (cartQuantity < model.stock_quantity!!.toInt()) {

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    // Show the progress dialog.
                    if (context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FireStoreClass().updateMyCart(context, model.id!!, itemHashMap)
                } else {
                    if (context is CartListActivity) {
                        context.showErrorSnackBar(
                            context.resources.getString(
                                R.string.msg_for_available_stock,
                                model.stock_quantity
                            ),
                            true
                        )
                    }
                }
                // END
            }
            // END


            holder.ib_delete_cart_item.setOnClickListener {

                when (context) {
                    is CartListActivity -> {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }
                }

                FireStoreClass().removeItemFromCart(context, model.id!!)
            }
        }

    }


}