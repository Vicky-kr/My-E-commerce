package com.example.mye_commerce.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Product
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader

import java.io.IOException

class AddProductActivity : BaseActivity() {
    private lateinit var iv_add_update_product:ImageView
    private lateinit var et_product_title:EditText
    private lateinit var et_product_price:EditText
    private lateinit var et_product_description:EditText
    private lateinit var et_product_quantity:EditText
    private lateinit var btn_submit:Button
    private lateinit var iv_add_product_image:ImageView
    private lateinit var toolbar_add_product_activity:Toolbar


    private var mSelectedImageFileUri: Uri? = null
    private var mProductImageURL: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        this.iv_add_update_product = findViewById(R.id.iv_add_update_product)
        this.et_product_title = findViewById(R.id.et_product_title)
        this.et_product_price = findViewById(R.id.et_product_price)
        this.et_product_description = findViewById(R.id.et_product_description)
        this.et_product_quantity = findViewById(R.id.et_product_quantity)
        this.btn_submit = findViewById(R.id.btn_submit)
        this.iv_add_product_image = findViewById(R.id.iv_product_image)
        this.toolbar_add_product_activity = findViewById(R.id.toolbar_add_product_activity)
        //Allows user to upload the product photo
//        setupActionBar()

        iv_add_update_product.setOnClickListener{
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@AddProductActivity)
            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in your manifest, they should not be granted to your app,
                 and they should have protection level*/
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        //Submit button
        btn_submit.setOnClickListener {
            if(validateProductDetails()){
                uploadProductImage()
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@AddProductActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button_white_24)
        }

        toolbar_add_product_activity.setNavigationOnClickListener { onBackPressed() }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    iv_add_update_product.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_vector_edit_24))

                    try {

//                         The uri of selected image from phone storage.

                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this@AddProductActivity).loadUserProfile(
                            mSelectedImageFileUri!!,
                            iv_add_product_image
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddProductActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    // Validate the product details
    private fun validateProductDetails(): Boolean {
        return when {

            mSelectedImageFileUri == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }

            TextUtils.isEmpty(et_product_title.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }

            TextUtils.isEmpty(et_product_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(et_product_description.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_product_description),
                    true
                )
                false
            }

            TextUtils.isEmpty(et_product_quantity.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_product_quantity),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }
    private fun uploadProductImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        FireStoreClass().uploadImageToCloudStorage(
            this@AddProductActivity,
            mSelectedImageFileUri,
            Constants.PRODUCT_IMAGE
        )
    }

    /**
     * A function to get the successful result of product image upload.
     */
    fun imageUploadSuccess(imageURL: String) {

        // Initialize the global image url variable.
        mProductImageURL = imageURL

        uploadProductDetails()
    }

    private fun uploadProductDetails() {

        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
            this.getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USERNAME, "")!!

        // Here we get the text from editText and trim the space
        val product = Product(
            FireStoreClass().getCurrentUserID(),
            username,
            et_product_title.text.toString().trim { it <= ' ' },
            et_product_price.text.toString().trim { it <= ' ' },
            et_product_description.text.toString().trim { it <= ' ' },
            et_product_quantity.text.toString().trim { it <= ' ' },
            mProductImageURL
        )

        FireStoreClass().uploadProductDetails(this@AddProductActivity, product)
    }

    /**
     * A function to return the successful result of Product upload.
     */
    fun productUploadSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@AddProductActivity,
            resources.getString(R.string.product_uploaded_success_message),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}