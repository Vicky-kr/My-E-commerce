package com.example.mye_commerce.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.BaseActivity
import com.example.mye_commerce.utils.Constants
import com.google.android.material.textfield.TextInputLayout
import com.example.mye_commerce.firestore.FireStoreClass

class AddEditAddressActivity : BaseActivity() {

    private  var mAddressDetails:com.example.mye_commerce.model.Address? = null
    private lateinit var et_full_name:EditText
    private lateinit var tv_title:TextView
    private lateinit var et_phone_number:EditText
    private lateinit var et_address:EditText
    private lateinit var et_zip_code:EditText
    private lateinit var et_additional_note:EditText
    private lateinit var rb_home:RadioButton
    private lateinit var rb_office:RadioButton
    private lateinit var rb_other:RadioButton
    private lateinit var et_other_details:EditText
    private lateinit var btn_submit_address: Button
    private lateinit var rg_type:RadioGroup
    private lateinit var til_other_details:TextInputLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)
        supportActionBar?.hide()
        tv_title = findViewById(R.id.tv_title)
        et_full_name = findViewById(R.id.et_full_name);
        et_phone_number = findViewById(R.id.et_phone_number)
        et_address = findViewById(R.id.et_address)
        et_zip_code = findViewById(R.id.et_zip_code)
        et_additional_note = findViewById(R.id.et_additional_note)
        rb_home = findViewById(R.id.rb_home)
        rb_office = findViewById(R.id.rb_office)
        rb_other = findViewById(R.id.rb_other)
        til_other_details = findViewById(R.id.til_other_details)
        et_other_details = findViewById(R.id.et_other_details);
        rg_type = findViewById(R.id.rg_type)
        btn_submit_address = findViewById(R.id.btn_submit_address)


        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)){
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)
        }

        if(mAddressDetails != null){

            if (mAddressDetails!!.id.isNotEmpty()){
                tv_title.text = resources.getString(R.string.title_edit_address)
                btn_submit_address.text = resources.getString(R.string.btn_lbl_update)
                et_full_name.setText(mAddressDetails?.name)
                et_phone_number.setText(mAddressDetails?.mobileNumber)
                et_address.setText(mAddressDetails?.address)
                et_zip_code.setText(mAddressDetails?.zipcode)
                et_additional_note.setText(mAddressDetails?.additionalNote)

                when(mAddressDetails?.type){
                    Constants.HOME ->{
                        rb_home.isChecked = true
                    }
                    Constants.OFFICE ->{
                        rb_office.isChecked = true
                    }
                    else->{
                        rb_other.isChecked = true
                        til_other_details.visibility = View.VISIBLE
                        btn_submit_address.visibility = View.VISIBLE
                        et_other_details.setText(mAddressDetails?.otherDetails)
                    }
                }
            }
        }
        /**
         * Radio Group
         */
        rg_type.setOnCheckedChangeListener {_, checkedId ->
            if (checkedId == R.id.rb_other){
                til_other_details.visibility = View.VISIBLE
            }else{
                til_other_details.visibility = View.GONE
            }
        }
        /**
         * Submit button
         */
        btn_submit_address.setOnClickListener {
            saveAddressToFirestore()
        }



    }

    /**
     * Function to save the address in Firestore
     */
    private fun saveAddressToFirestore() {

        // Here we get the text from editText and trim the space
        val fullName: String = et_full_name.text.toString().trim { it <= ' ' }
        val phoneNumber: String = et_phone_number.text.toString().trim { it <= ' ' }
        val address: String = et_address.text.toString().trim { it <= ' ' }
        val zipCode: String = et_zip_code.text.toString().trim { it <= ' ' }
        val additionalNote: String = et_additional_note.text.toString().trim { it <= ' ' }
        val otherDetails: String = et_other_details.text.toString().trim { it <= ' ' }

        if (validateData()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val addressType: String = when {
                rb_home.isChecked -> {
                    Constants.HOME
                }
                rb_office.isChecked -> {
                    Constants.OFFICE
                }
                else -> {
                    Constants.OTHER
                }
            }

            val addressModel = com.example.mye_commerce.model.Address(
               FireStoreClass().getCurrentUserID(),
                fullName,
                phoneNumber,
                address,
                zipCode,
                additionalNote,
                addressType,
                otherDetails
            )
            if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()){
                FireStoreClass().updateAddress(this@AddEditAddressActivity,
                    addressModel,
                    mAddressDetails!!.id)
            }else {
                FireStoreClass().addAddress(this, addressModel)
            }
        }
    }



    /**
     *  Function to validate the Address Data
     */

    private fun validateData(): Boolean {
        return when {

            TextUtils.isEmpty(et_full_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_please_enter_full_name),
                    true
                )
                false
            }

            TextUtils.isEmpty(et_phone_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_please_enter_phone_number),
                    true
                )
                false
            }

            TextUtils.isEmpty(et_address.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }

            TextUtils.isEmpty(et_zip_code.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }

            rb_other.isChecked && TextUtils.isEmpty(
                et_zip_code.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }
            else -> {
                true
            }
        }

    }
    /**
     * Function to add/update address details
     */

    fun addUpdateAddressSuccess() {

        hideProgressDialog()
        val notifySuccessMessage:String = if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()){
            resources.getString(R.string.msg_your_address_updated_successfully)
        }else{
            resources.getString(R.string.err_your_address_added_successfully)
        }

        Toast.makeText(
            this@AddEditAddressActivity,
            notifySuccessMessage,
            Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()

    }




}