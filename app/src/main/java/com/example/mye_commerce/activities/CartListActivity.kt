package com.example.mye_commerce.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.adapters.CartItemListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.CartItem
import com.example.mye_commerce.model.Product
import com.example.mye_commerce.utils.Constants

class CartListActivity : BaseActivity() {
    private lateinit var mProductsList: ArrayList<Product>

    // A global variable for the cart list items.
    private lateinit var mCartListItems: ArrayList<CartItem>

    private lateinit var rv_cart_items_list:RecyclerView
    private lateinit var ll_checkout:LinearLayout
    private lateinit var tv_no_cart_item_found:TextView
    private lateinit var tv_sub_total:TextView
    private lateinit var tv_shipping_charge:TextView
    private lateinit var tv_total_amount:TextView
    private lateinit var btn_checkout:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        rv_cart_items_list = findViewById(R.id.rv_cart_items_list);
        ll_checkout = findViewById(R.id.ll_checkout);
        tv_no_cart_item_found = findViewById(R.id.tv_no_cart_item_found);
        tv_sub_total = findViewById(R.id.tv_sub_total)
        tv_shipping_charge = findViewById(R.id.tv_shipping_charge)
        tv_total_amount = findViewById(R.id.tv_total_amount)
        btn_checkout = findViewById(R.id.btn_checkout)
        btn_checkout.setOnClickListener {
            val intent = Intent(this@CartListActivity,AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS,true)
            startActivity(intent)
        }


    }
    override fun onResume() {
        super.onResume()

        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
//    private fun setupActionBar() {
//
//        setSupportActionBar(toolbar_cart_list_activity)
//
//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
//        }
//
//        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
//    }

    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().getAllProductsList(this@CartListActivity)
    }

    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        mProductsList = productsList

        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FireStoreClass().getCartList(this@CartListActivity)
    }

    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    fun successCartItemsList(cartList: ArrayList<CartItem>) {

        // Hide progress dialog.
        hideProgressDialog()

        for (product in mProductsList) {
            for (cart in cartList) {
                if (product.product_id == cart.product_id) {

                    cart.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cart.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        mCartListItems = cartList

        if (mCartListItems.size > 0) {

            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemListAdapter(this@CartListActivity, mCartListItems,true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal: Double = 0.0

            for (item in mCartListItems) {

                val availableQuantity = item.stock_quantity?.toInt()

                if (availableQuantity!! > 0) {
                    val price = item.price?.toDouble()
                    val quantity = item.cart_quantity?.toInt()

                    subTotal += (price?.times(quantity!!)!!)
                }
            }

            tv_sub_total.text = "$${subTotal}"
            // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
            tv_shipping_charge.text = "$4.0"

            if (subTotal > 0) {
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + 4
                tv_total_amount.text = "$$total"
            } else {
                ll_checkout.visibility = View.GONE
            }

        } else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    /**
     * A function to notify the user about the item removed from the cart list.
     */
    fun itemRemovedSuccess() {

        hideProgressDialog()

        Toast.makeText(
            this@CartListActivity,
            resources.getString(R.string.msg_item_removed_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getCartItemsList()
    }

    // TODO Step 3: Create a function to notify the user about the item quantity updated in the cart list.
    // START
    /**
     * A function to notify the user about the item quantity updated in the cart list.
     */
    fun itemUpdateSuccess() {

        hideProgressDialog()

        getCartItemsList()
    }
}