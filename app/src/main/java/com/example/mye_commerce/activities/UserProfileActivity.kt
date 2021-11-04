package com.example.mye_commerce.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.User
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class UserProfileActivity : BaseActivity() {

    private lateinit var iv_user_photo: ImageView
    private lateinit var btn_save: Button
    private lateinit var et_mobile_number: EditText
    private lateinit var rb_male: RadioButton
    private lateinit var et_first_name: EditText
    private lateinit var et_last_name: EditText
    private lateinit var et_email: EditText
    private lateinit var rb_female: RadioButton
    private lateinit var tv_title: TextView


    // Instance of User data model class. We will initialize it later on.
    private lateinit var mUserDetails: User

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private var mUserProfileImageURL: String = ""

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_user_profile)
        supportActionBar?.hide()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        this.iv_user_photo = findViewById(R.id.iv_user_photo)
        this.btn_save = findViewById(R.id.btn_save)
        this.et_mobile_number = findViewById(R.id.et_mobile_number)
        this.rb_male = findViewById(R.id.rb_male)
        this.rb_female = findViewById(R.id.rb_female)
        this.tv_title = findViewById(R.id.tv_title)

        this.et_email = findViewById(R.id.et_email)
        this.et_first_name = findViewById(R.id.et_first_name)
        this.et_last_name = findViewById(R.id.et_last_name)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        et_first_name.setText(mUserDetails.firstName)
        et_last_name.setText(mUserDetails.lastName)
        et_email.setText(mUserDetails.email)
        et_mobile_number.setText(mUserDetails.mobile.toString())
        et_email.isEnabled = false


        /*
                This line is for the first time user is registered
         */
        if (mUserDetails.profileCompleted == 0) {
            et_first_name.isEnabled = false

            et_last_name.isEnabled = false


        }
        /*
                This line is for when user wants to edit his/her profile
         */
        else {
            tv_title.text = getString(R.string.title_edit_profile)
            mUserDetails.image.let {
                GlideLoader(this@UserProfileActivity).loadUserProfile(
                    it,
                    iv_user_photo
                )
            }

            if (mUserDetails.mobile != 0L) {
                et_mobile_number.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender == Constants.MALE) {
                rb_male.isChecked = true
            } else {
                rb_female.isChecked = true
            }


        }


        //User Photo

        iv_user_photo.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@UserProfileActivity)
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
        //save button
        btn_save.setOnClickListener {

            if (validateUserProfileDetails()) {

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))

                if (mSelectedImageFileUri != null) {

                    FireStoreClass().uploadImageToCloudStorage(
                        this@UserProfileActivity,
                        mSelectedImageFileUri,
                        Constants.USER_PROFILE_IMAGE
                    )
                } else {

                    updateUserProfileDetails()
                }
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
                Constants.showImageChooser(this@UserProfileActivity)
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {

                        // The uri of selected image from phone storage.
                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this@UserProfileActivity).loadUserProfile(
                            mSelectedImageFileUri!!,
                            iv_user_photo
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UserProfileActivity,
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

    private fun validateUserProfileDetails(): Boolean {
        return when {

            // We have kept the user profile picture is optional.
            // The FirstName, LastName, and Email Id are not editable when they come from the login screen.
            // The Radio button for Gender always has the default selected value.

            // Check if the mobile number is not empty as it is mandatory to enter.
            TextUtils.isEmpty(et_mobile_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun updateUserProfileDetails() {

        val userHashMap = HashMap<String, Any>()
        val firstName = et_first_name.text.toString().trim() { it <= ' ' }
        val lastName = et_last_name.text.toString().trim() { it <= ' ' }

        // Here the field which are not editable needs no update. So, we will update user Mobile Number and Gender for now.

        // Here we get the text from editText and trim the space
        val mobileNumber = et_mobile_number.text.toString().trim { it <= ' ' }

        var gender: String
        if (rb_male.isChecked){
            gender = Constants.MALE
        }else{
            gender = Constants.FEMALE
        }

        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }
        if (firstName != mUserDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }
        if (lastName != mUserDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = lastName
        }

        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }
        if (gender.isEmpty() && gender != mUserDetails.gender) {
            userHashMap[Constants.GENDER] = gender
        }


        // 0: User profile is incomplete.
        // 1: User profile is completed.
        userHashMap[Constants.COMPLETE_PROFILE] = 1

        // call the registerUser function of FireStore class to make an entry in the database.
        FireStoreClass().updateUserProfileData(
            this@UserProfileActivity,
            userHashMap
        )
    }

    fun userProfileUpdateSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@UserProfileActivity,
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_SHORT
        ).show()


        // Redirect to the Main Screen after profile completion.
        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    fun imageUploadSuccess(imageURL: String) {

        mUserProfileImageURL = imageURL

        updateUserProfileDetails()
    }
}
