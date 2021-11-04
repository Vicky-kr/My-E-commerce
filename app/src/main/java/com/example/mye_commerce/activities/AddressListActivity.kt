package com.example.mye_commerce.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.adapters.AddressListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Address
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.SwipeToDeleteCallback
import com.example.mye_commerce.utils.SwipeToEditCallback


class AddressListActivity : BaseActivity() {
    private var mSelectedAddress:Boolean= false

    private lateinit var mAddressList: ArrayList<Address>

    private lateinit var rv_address_list:RecyclerView
    private lateinit var tv_add_address:TextView
    private lateinit var tv_no_address_found:TextView
    private lateinit var tv_title:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)
        supportActionBar?.hide()
        rv_address_list = findViewById(R.id.rv_address_list)
        this.tv_add_address = findViewById(R.id.tv_add_address)
        this.tv_no_address_found = findViewById(R.id.tv_no_address_found)
        this.tv_title = findViewById(R.id.tv_title)
        tv_add_address.setOnClickListener {
            val intent = Intent(this@AddressListActivity,
                AddEditAddressActivity::class.java)
            startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
        }
        getAddressList()
        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)){
            mSelectedAddress = intent.getBooleanExtra(Constants.EXTRA_SELECTED_ADDRESS,false);
        }
        if (mSelectedAddress){
            tv_title.text = resources.getString(R.string.title_select_address)
            tv_add_address.visibility = View.GONE

        }

    }
    /**
     * Override the onActivityResult
     */

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            getAddressList()
        }
    }

    fun successAddressListFromFireStore(addressList: ArrayList<Address>) {
        hideProgressDialog()
        mAddressList = addressList
        if (addressList.size>0){
            rv_address_list.visibility = View.VISIBLE
            tv_no_address_found.visibility = View.GONE
            rv_address_list.layoutManager = LinearLayoutManager(this@AddressListActivity)
            rv_address_list.setHasFixedSize(true)

            val addressListAdapter = AddressListAdapter(this,addressList,mSelectedAddress)
            rv_address_list.adapter = addressListAdapter

            if (!mSelectedAddress) {

                /**
                 * Method to edit the address by swiping right
                 */
                val editSwipeHandler = object : SwipeToEditCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val adapter = rv_address_list.adapter as AddressListAdapter
                        adapter.notifyEditItem(
                            this@AddressListActivity,
                            viewHolder.adapterPosition
                        )
                    }
                }

                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(rv_address_list)

                /**
                 * Method to delete the address by swiping left
                 */
                val deleteSwipeHandler = object : SwipeToDeleteCallback(this@AddressListActivity) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog(resources.getString(R.string.please_wait))
                        FireStoreClass().deleteAddress(this@AddressListActivity,
                            addressList[viewHolder.adapterPosition].id)
                    }
                }
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(rv_address_list)
            }

        }
        else{
            rv_address_list.visibility = View.GONE
            tv_no_address_found.visibility = View.VISIBLE
        }

//        FirestoreClass().getAddressesList(this@AddressListActivity)
    }
    private fun getAddressList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStoreClass().getAddressesList(this@AddressListActivity)
    }
    /**
     * Function when the deleting of address is successfull
     */
    fun deleteAddressSuccess(){
        hideProgressDialog()
        Toast.makeText(this,
            resources.getText(R.string.err_your_address_deleted_successfully),
            Toast.LENGTH_SHORT).show()
        getAddressList()
    }

}