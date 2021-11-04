package com.example.mye_commerce.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.adapters.SoldProductListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.SoldProduct

class SoldProductFragment : BaseFragment() {
    private lateinit var rv_sold_product_items:RecyclerView
    private lateinit var tv_no_sold_products_found:TextView


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_sold_products, container, false)
        rv_sold_product_items = root.findViewById(R.id.rv_sold_product_items) as RecyclerView
        tv_no_sold_products_found = root.findViewById(R.id.tv_no_sold_products_found) as TextView

        return root
    }

    override fun onResume() {
        super.onResume()
        getSoldProductsList()
    }
    private fun getSoldProductsList(){
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().getSoldProductsList(this@SoldProductFragment)
    }
    fun successSoldProductsList(soldProductsList:ArrayList<SoldProduct>){
        hideProgressDialog()
        if(soldProductsList.size > 0){
            rv_sold_product_items.visibility = View.VISIBLE
            tv_no_sold_products_found.visibility = View.GONE
            rv_sold_product_items.layoutManager = LinearLayoutManager(activity)
            rv_sold_product_items.setHasFixedSize(true)

            val soldProductsListAdapter = SoldProductListAdapter(requireActivity(),soldProductsList)
            rv_sold_product_items.adapter = soldProductsListAdapter
        }else{
            rv_sold_product_items.visibility = View.GONE
            tv_no_sold_products_found.visibility = View.VISIBLE
        }
    }

}