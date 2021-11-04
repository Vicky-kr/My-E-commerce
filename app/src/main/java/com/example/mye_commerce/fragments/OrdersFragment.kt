package com.example.mye_commerce.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.adapters.MyOrdersListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Order

class OrdersFragment : BaseFragment() {
    private lateinit var tv_no_order_found:TextView
    private  lateinit var rv_my_order_item:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_orders, container, false)
        tv_no_order_found = root.findViewById(R.id.tv_no_orders_found)
        rv_my_order_item = root.findViewById(R.id.rv_my_order_items);

        return root
    }
    fun populateOrdersListInUI(ordersList:ArrayList<Order>){
        hideProgressDialog()
        if (ordersList.size >0){
            tv_no_order_found.visibility = View.GONE
            rv_my_order_item.visibility = View.VISIBLE
            rv_my_order_item.layoutManager = LinearLayoutManager(activity)
            rv_my_order_item.setHasFixedSize(true);
            val myOrdersListAdapter = MyOrdersListAdapter(requireActivity(),ordersList)
            rv_my_order_item.adapter = myOrdersListAdapter
        }
        else{
            rv_my_order_item.visibility = View.GONE
            tv_no_order_found.visibility = View.VISIBLE
        }



    }
    private fun getMyOrdersList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getMyOrdersList(this@OrdersFragment)
    }

    override fun onResume() {
        super.onResume()
        getMyOrdersList()
    }
}