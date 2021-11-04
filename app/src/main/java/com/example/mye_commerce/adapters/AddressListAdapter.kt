package com.example.mye_commerce.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.AddEditAddressActivity
import com.example.mye_commerce.activities.CheckoutActivity
import com.example.mye_commerce.model.Address
import com.example.mye_commerce.utils.Constants

open class AddressListAdapter(
    private val context: Context,
    private val list: List<Address>,
    private val selectAddress:Boolean
):RecyclerView.Adapter<AddressListAdapter.MyViewHolder>(){

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view) {
        var tv_address_full_name:TextView = view.findViewById(R.id.tv_address_full_name)
        var tv_address_details:TextView = view.findViewById(R.id.tv_address_details)
        var tv_address_mobile_number:TextView = view.findViewById(R.id.tv_address_mobile_number)
        var tv_address_type:TextView = view.findViewById(R.id.tv_address_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(
            R.layout.item_address_layout,
            parent,
            false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            holder.tv_address_full_name.text = model.name
            holder.tv_address_type.text = model.type
            holder.tv_address_details.text = "${model.address}, ${model.zipcode}"
            holder.tv_address_mobile_number.text = model.mobileNumber

            if (selectAddress){
                holder.itemView.setOnClickListener {
//                    Toast.makeText(context,
//                    "Selected address : ${model.address} , ${model.zipCode}",
//                    Toast.LENGTH_LONG).show()
                    val intent = Intent(context, CheckoutActivity::class.java);
                    intent.putExtra(Constants.EXTRA_SELECT_ADDRESS,model)
                    context.startActivity(intent);
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun notifyEditItem(activity: Activity, position: Int){
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS,list[position])
        activity.startActivityForResult(intent,Constants.ADD_ADDRESS_REQUEST_CODE)
        notifyItemChanged(position)
    }

}