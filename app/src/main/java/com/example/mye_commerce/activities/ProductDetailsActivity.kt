package com.example.mye_commerce.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.CartItem
import com.example.mye_commerce.model.Product
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {
    private lateinit var mProductId:String
    private var mProductOwnerId:String = ""
    private lateinit var iv_product_image_details:ImageView
    private lateinit var  tv_product_details_price:TextView
    private lateinit var tv_product_details_description:TextView
    private lateinit var tv_details_available_quantity:TextView
    private lateinit var tv_product_details_title:TextView
    private lateinit var btn_lbl_go_to_cart:Button
    private lateinit var btn_add_to_cart: Button

    /**
     * create an object of product
     */
    private lateinit var mProductDetails: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        supportActionBar?.hide()
        iv_product_image_details = findViewById(R.id.iv_product_detail_image)
        tv_product_details_price = findViewById(R.id.tv_product_details_price)
        tv_product_details_description = findViewById(R.id.tv_product_details_description)
        tv_details_available_quantity = findViewById(R.id.tv_product_details_available_quantity)
        tv_product_details_title = findViewById(R.id.tv_product_details_title)
        btn_lbl_go_to_cart = findViewById(R.id.btn_go_to_cart)
        btn_add_to_cart = findViewById(R.id.btn_add_to_cart)
        var extraUserId:String = ""

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            mProductId =
                intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }

//        var productOwnerId: String = ""

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            mProductOwnerId =
                intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

//        setupActionBar()

        if (FireStoreClass().getCurrentUserID() == mProductOwnerId) {
            btn_add_to_cart.visibility = View.GONE
            btn_lbl_go_to_cart.visibility = View.GONE
        } else {
            btn_add_to_cart.visibility = View.VISIBLE
        }

        btn_add_to_cart.setOnClickListener(this)
        btn_lbl_go_to_cart.setOnClickListener(this)

        getProductDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.btn_add_to_cart -> {
                    addToCart()
                }

                R.id.btn_go_to_cart->{
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
            }
        }
    }

    /**
     * A function to prepare the cart item to add it to the cart in cloud firestore.
     */
    private fun addToCart() {

        val addToCart = CartItem(
            FireStoreClass().getCurrentUserID(),
            mProductOwnerId,
            mProductId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )

        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().addCartItems(this@ProductDetailsActivity, addToCart)
    }

    /**
     * A function for actionBar Setup.
     */
//    private fun setupActionBar() {
//
//        setSupportActionBar(toolbar_product_details_activity)
//
//        val actionBar = supportActionBar
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
//        }
//
//        toolbar_product_details_activity.setNavigationOnClickListener { onBackPressed() }
//    }

    /**
     * A function to call the firestore class function that will get the product details from cloud firestore based on the product id.
     */
    private fun getProductDetails() {

        // Show the product dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of FirestoreClass to get the product details.
        FireStoreClass().getProductDetails(this@ProductDetailsActivity, mProductId)
    }

    /**
     * A function to notify the success result of the product details based on the product id.
     *
     * @param product A model class with product details.
     */
    fun productDetailsSuccess(product: Product) {

        mProductDetails = product

        // Populate the product details in the UI.
        GlideLoader(this@ProductDetailsActivity).loadProductPicture(
            product.image,
            iv_product_image_details
        )

        tv_product_details_title.text = product.title
        tv_product_details_price.text = "$${product.price}"
        tv_product_details_description.text = product.description
        tv_details_available_quantity.text = product.stock_quantity


        if(product.stock_quantity.toInt() == 0){

            // Hide Progress dialog.
            hideProgressDialog()

            // Hide the AddToCart button if the item is already in the cart.
            btn_add_to_cart.visibility = View.GONE

            tv_details_available_quantity.text =
                resources.getString(R.string.lbl_out_of_stock)

            tv_details_available_quantity.setTextColor(
                ContextCompat.getColor(
                    this@ProductDetailsActivity,
                    R.color.colorSnackBarError
                )
            )
        }else{

            // There is no need to check the cart list if the product owner himself is seeing the product details.
            if (FireStoreClass().getCurrentUserID() == product.user_id) {
                // Hide Progress dialog.
                hideProgressDialog()
            } else {
                FireStoreClass().checkIfItemExistInCart(this@ProductDetailsActivity, mProductId)
            }
        }
    }

    /**
     * A function to notify the success result of item exists in the cart.
     */
    fun productExistsInCart() {

        // Hide the progress dialog.
        hideProgressDialog()

        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_lbl_go_to_cart.visibility = View.VISIBLE
    }

    /**
     * A function to notify the success result of item added to the to cart.
     */
    fun addToCartSuccess() {
        // Hide the progress dialog.
        hideProgressDialog()

        Toast.makeText(
            this@ProductDetailsActivity,
            resources.getString(R.string.success_message_item_added_to_cart),
            Toast.LENGTH_SHORT
        ).show()

        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_lbl_go_to_cart.visibility = View.VISIBLE
    }
}