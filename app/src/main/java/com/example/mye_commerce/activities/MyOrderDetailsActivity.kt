package com.example.mye_commerce.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Order
import com.example.mye_commerce.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class MyOrderDetailsActivity : BaseActivity() {
    private lateinit var tv_order_details_date: TextView
    private lateinit var tv_order_status: TextView
    private lateinit var rv_my_order_items_list: RecyclerView
    private lateinit var tv_order_details_sub_total: TextView
    private lateinit var tv_order_details_shipping_charge: TextView
    private lateinit var tv_order_details_total_amount: TextView
    private lateinit var tv_my_order_details_address_type:TextView
    private lateinit var tv_my_order_details_full_name:TextView
    private lateinit var tv_my_order_details_address:TextView
    private lateinit var tv_my_order_details_additional_note:TextView
    private lateinit var tv_my_order_details_other_details:TextView
    private lateinit var tv_my_order_details_mobile_number:TextView
    private lateinit var tv_order_details_id:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order_details)
        supportActionBar?.hide()
        var myOrderDetails = Order()
        this.tv_order_details_date = findViewById(R.id.tv_order_details_date)
        this.tv_order_status = findViewById(R.id.tv_order_status)
        this.rv_my_order_items_list = findViewById(R.id.rv_my_order_items_list)
        this.tv_order_details_sub_total = findViewById(R.id.tv_order_details_sub_total)
        this.tv_order_details_shipping_charge = findViewById(R.id.tv_order_details_shipping_charge)
        this.tv_order_details_total_amount = findViewById(R.id.tv_order_details_total_amount)
        this.tv_my_order_details_address_type = findViewById(R.id.tv_my_order_details_address_type)
        this.tv_my_order_details_mobile_number = findViewById(R.id.tv_my_order_details_mobile_number)
        this.tv_my_order_details_other_details = findViewById(R.id.tv_my_order_details_other_details)
        this.tv_my_order_details_additional_note = findViewById(R.id.tv_my_order_details_additional_note)
        this.tv_my_order_details_address = findViewById(R.id.tv_my_order_details_address)
        this.tv_my_order_details_full_name = findViewById(R.id.tv_my_order_details_full_name)
        this.tv_order_details_id = findViewById(R.id.tv_order_details_id)

//        getOrderDetails()
//        setUpUI(myOrderDetails)

        val orderId = intent.getStringExtra(Constants.EXTRA_MY_ORDER_DETAILS)
//        Toast.makeText(this,intent.getStringExtra(Constants.EXTRA_MY_ORDER_DETAILS),Toast.LENGTH_LONG).show()

        if (!orderId!!.isEmpty()) {
            tv_order_details_id.text = orderId
            details(orderId)
        }
    }
    /**
    private fun setUpUI(orderDetails:Order){
    val dateFormat = "dd MMM yyyy HH:mm"
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    val calendar:Calendar = Calendar.getInstance()
    calendar.timeInMillis = orderDetails.order_datetime
    val orderDateTime = formatter.format(calendar.time)
    tv_order_details_date.text = orderDateTime
    /**
     * if order time is less than 1Hr show pending
     * if order time is greater than 1Hr but less than 2Hr show processing
     * if order time is less than 3Hr show delivered
    */
    val diffInMilliSeconds:Long = System.currentTimeMillis() - orderDetails.order_datetime
    val diffInHours:Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds);
    Log.d("Difference in Hours","$diffInHours")
    when{
    diffInHours < 1 ->{
    tv_order_status.text = resources.getString(R.string.order_status_pending)
    tv_order_status.setTextColor(ContextCompat.getColor(this@MyOrderDetailsActivity,
    R.color.colorAccent))
    }
    diffInHours < 2 ->{
    tv_order_status.text = resources.getString(R.string.order_status_in_process)
    tv_order_status.setTextColor(ContextCompat.getColor(this@MyOrderDetailsActivity,
    R.color.colorOrderStatusInProcess))
    }
    else ->{
    tv_order_status.text = resources.getString(R.string.order_status_delivered)
    tv_order_status.setTextColor(ContextCompat.getColor(this@MyOrderDetailsActivity,
    R.color.colorOrderStatusDelivered))
    }
    }
    rv_my_order_items_list.layoutManager = LinearLayoutManager(this@MyOrderDetailsActivity)
    rv_my_order_items_list.setHasFixedSize(true)
    val cartListAdapter = CartItemListAdapter(this@MyOrderDetailsActivity,
    orderDetails.items,
    false);
    rv_my_order_items_list.adapter = cartListAdapter
    }
     */
    /**
     * Get order details
     */
    private fun getOrderDetails() {
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getOrderDetails(this@MyOrderDetailsActivity)
    }

    fun successorderList() {
        hideProgressDialog()
    }

    private fun details(orderId: String) {
        val dateFormat = "dd MMM yyyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()
        FirebaseFirestore.getInstance().collection(Constants.ORDER)
            .document(orderId).get()
            .addOnSuccessListener { document ->

                calendar.timeInMillis = document.getLong("order_dateTime")!!
                val orderDateTime = formatter.format(calendar.time)
                order_status(calendar.timeInMillis)
                tv_order_details_date.text = orderDateTime
                tv_order_details_sub_total.text = "$" + document.getString("sub_total_amount")
                tv_order_details_shipping_charge.text = "$" + document.getString("shipping_charge")
                tv_order_details_total_amount.text = "$"+document.getString("total_amount")
                val address = document.get("address") as HashMap<String,Any>
                setUI(address)


            }.addOnFailureListener { err ->
                err.printStackTrace()
            }
    }
    private fun setUI(address:HashMap<String,Any>){
        tv_my_order_details_address_type.text =  address.get("type").toString()
        tv_my_order_details_mobile_number.text = address.get("mobileNumber").toString()
        tv_my_order_details_full_name.text = address.get("name").toString()
        tv_my_order_details_other_details.text = address.get("otherDetails").toString()
        tv_my_order_details_additional_note.text = address.get("additionalNote").toString()
        tv_my_order_details_address.text = address.get("address").toString()+" ,pincode : "+address.get("zipcode").toString()
    }
    private fun order_status(timeInMillis:Long){
        val diffInMilliSeconds:Long = System.currentTimeMillis() - timeInMillis
        val diffInHours:Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds);
        when{
            diffInHours < 1 ->{
                tv_order_status.text = resources.getString(R.string.order_status_pending)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(this@MyOrderDetailsActivity,
                    R.color.colorAccent))
            }
            diffInHours < 2 ->{
                tv_order_status.text = resources.getString(R.string.order_status_in_process)
                tv_order_status.setTextColor(ContextCompat.getColor(this@MyOrderDetailsActivity,
                    R.color.colorOrderStatusInProcess))
            }
            else ->{
                tv_order_status.text = resources.getString(R.string.order_status_delivered)
                tv_order_status.setTextColor(ContextCompat.getColor(this@MyOrderDetailsActivity,
                    R.color.colorOrderStatusDelivered))
            }
        }
    }
}