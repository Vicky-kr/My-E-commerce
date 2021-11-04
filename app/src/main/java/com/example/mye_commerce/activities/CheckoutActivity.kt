package com.example.mye_commerce.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.adapters.CartItemListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Address
import com.example.mye_commerce.model.CartItem
import com.example.mye_commerce.model.Order
import com.example.mye_commerce.model.Product
import com.example.mye_commerce.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class CheckoutActivity : BaseActivity() {

    private  var mAddressDetails: Address? = null;
    private lateinit var mProductsList:ArrayList<Product>
    private lateinit var mCartItemsList:ArrayList<CartItem>
    private var mSubTotal: Double = 0.0
    private var mTotalAmount: Double = 0.0
    private lateinit var mOrderDetails: Order


    private lateinit var tv_checkout_address_type:TextView
    private lateinit var tv_checkout_address:TextView
    private lateinit var tv_checkout_full_name:TextView
    private lateinit var tv_checkout_additional_note:TextView
    private lateinit var tv_checkout_other_details:TextView
    private lateinit var tv_checkout_mobile_number:TextView
    private lateinit var rv_cart_list_items:RecyclerView
    private lateinit var btn_place_order:Button
    private lateinit var tv_checkout_sub_total:TextView
    private lateinit var tv_checkout_shipping_charge:TextView
    private lateinit var tv_checkout_total_amount:TextView
    private lateinit var ll_checkout_place_order:LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        supportActionBar?.hide()
        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)){
            mAddressDetails = intent.getParcelableExtra<Address>(Constants.EXTRA_SELECT_ADDRESS);
        }
        this.tv_checkout_address_type = findViewById(R.id.tv_checkout_address_type);
        this.tv_checkout_address = findViewById(R.id.tv_checkout_address);
        this.tv_checkout_full_name = findViewById(R.id.tv_checkout_full_name);
        this.tv_checkout_additional_note = findViewById(R.id.tv_checkout_additional_note)
        this.tv_checkout_other_details= findViewById(R.id.tv_checkout_other_details);
        this.tv_checkout_mobile_number= findViewById(R.id.tv_mobile_number)
        this.rv_cart_list_items = findViewById(R.id.rv_cart_list_items);
        this.btn_place_order = findViewById(R.id.btn_place_order);
        this.tv_checkout_sub_total = findViewById(R.id.tv_checkout_sub_total);
        this.ll_checkout_place_order = findViewById(R.id.ll_checkout_place_order)
        tv_checkout_shipping_charge = findViewById(R.id.tv_checkout_shipping_charge)
        tv_checkout_total_amount = findViewById(R.id.tv_checkout_total_amount)

        /**
         * Displaying the Address Details
         */
        DisplayAddress();
        getProductList()
        btn_place_order.setOnClickListener {
            placeAnOrder()
        }


    }

    private fun DisplayAddress(){
        if (mAddressDetails != null){
            tv_checkout_address_type.text = mAddressDetails?.type
            tv_checkout_address.text = "${mAddressDetails?.address}, ${mAddressDetails?.zipcode}"
            tv_checkout_full_name.text = mAddressDetails?.name;
            tv_checkout_additional_note.text = mAddressDetails?.additionalNote;

            tv_checkout_mobile_number.text = mAddressDetails?.mobileNumber;
            if(mAddressDetails?.otherDetails!!.isNotEmpty()){
                tv_checkout_other_details.text = mAddressDetails?.otherDetails;
            }
        }

    }

    /**
     * Function to get the productList
     */
    private fun getProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAllProductsList(this@CheckoutActivity);
    }
    /**
     * Function to get successProduct List From FireStore
     */
    fun successProductsListFromFireStore(productList:ArrayList<Product>){
        mProductsList = productList
        getCartItemsList()
    }
    /**
     *
     */
    private fun getCartItemsList(){
        FireStoreClass().getCartList(this@CheckoutActivity)
    }
    fun successCartItemsList(cartList:ArrayList<CartItem>){
        hideProgressDialog()
        for (product in mProductsList){
            for (cart in cartList){
                if (product.product_id == cart.product_id){
                    cart.stock_quantity = product.stock_quantity
                }
            }
        }
        mCartItemsList = cartList;
        rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        rv_cart_list_items.setHasFixedSize(true)
        val cartListAdapter = CartItemListAdapter(this@CheckoutActivity,mCartItemsList,false);
        rv_cart_list_items.adapter = cartListAdapter


        for (item in mCartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }

        tv_checkout_sub_total.text = "$$mSubTotal"
        // Here we have kept Shipping Charge is fixed as $1 but in your case it may cary. Also, it depends on the location and total amount.
        tv_checkout_shipping_charge.text = "$4.0"

        if (mSubTotal > 0) {
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + 4.0
            tv_checkout_total_amount.text = "$$mTotalAmount"
        } else {
            ll_checkout_place_order.visibility = View.GONE
        }
    }

    private fun placeAnOrder(){
        showProgressDialog(resources.getString(R.string.please_wait))

        mOrderDetails = Order(
            FireStoreClass().getCurrentUserID(),
            mCartItemsList,
            mAddressDetails!!,
            "My order ${System.currentTimeMillis()}",
            mCartItemsList.get(0).image,
            mSubTotal.toString(),
            "4.0",
            mTotalAmount.toString(),
            System.currentTimeMillis()

        )
        FireStoreClass().placeOrder(this@CheckoutActivity,mOrderDetails)

    }
    /**
     * Function to call after order is placed
     */
    fun orderPlaceSuccess(){
        FireStoreClass().updateAllDetails(this@CheckoutActivity,mCartItemsList,mOrderDetails)

    }
    fun allDetailsUpdateSuccess(){
        hideProgressDialog()

        Toast.makeText(
            this@CheckoutActivity,
            "Your order was placed successfully",
            Toast.LENGTH_LONG
        ).show()

        intent = Intent(this@CheckoutActivity,DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }
}