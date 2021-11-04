package com.example.mye_commerce.activities


import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.User
import com.example.mye_commerce.utils.Constants
import com.example.mye_commerce.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private lateinit var tv_name:TextView
    private lateinit var tv_gender:TextView
    private lateinit var tv_email:TextView
    private lateinit var tv_mobile_number:TextView
    private lateinit var iv_user_photo:ImageView
    private lateinit var btn_logout:Button
    private lateinit var mUserDetails: User
    private lateinit var tv_edit:TextView
    private lateinit var ll_address:LinearLayout


//    private lateinit var toolbar_settings_activity:Toolbar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        tv_email = findViewById(R.id.tv_email)
        tv_gender = findViewById(R.id.tv_gender)
        tv_mobile_number = findViewById(R.id.tv_mobile_number)
        tv_name = findViewById(R.id.tv_name)
        iv_user_photo = findViewById(R.id.iv_user_photo)
        ll_address = findViewById(R.id.ll_address)

        this.tv_edit = findViewById(R.id.tv_edit)
        this.btn_logout = findViewById(R.id.btn_logout)
//        toolbar_settings_activity = findViewById(R.id.toolbar_settings_activity)
//        setupActionBar()

        /**
         *      Setting the edit profile screen
         */
        tv_edit.setOnClickListener {
            val intent = Intent(this@SettingsActivity,UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS,mUserDetails)
            startActivity(intent)
        }

        /**
         *    button to logout
         */

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingsActivity,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        /**
         * Textview to add address
         */
        ll_address.setOnClickListener {
            val intent = Intent(this@SettingsActivity,AddressListActivity::class.java)
            startActivity(intent)
        }

    }

    // TODO Step 11: Override the onResume function and call the getUserDetails function init.
    // START
    override fun onResume() {
        super.onResume()
        getUserDetails()
    }
    // END

    // TODO Step 2: Create a function to setup action bar.
    // START
    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {
//        setSupportActionBar(this.toolbar_settings_activity)
//        if(activity is AppCompatActivity){
//            (activity as AppCompatActivity).setSupportActionBar(toolbar_settings_activity)
//        }
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_button_white_24)
        }

//        this.toolbar_settings_activity.setNavigationOnClickListener {
//            onBackPressed()
//        }
    }

    // END

    // TODO Step 4: Create a function to get the user details from firestore.
    // START
    /**
     * A function to get the user details from firestore.
     */
    private fun getUserDetails() {

        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class to get the user details from firestore which is already created.
        FireStoreClass().getUserDetails(this@SettingsActivity)
    }
    // END

    // TODO Step 6: Create a function to receive the success result.
    // START
    /**
     * A function to receive the user details and populate it in the UI.
     */
    fun userDetailsSuccess(user: User) {

        mUserDetails = user
        // TODO Step 9: Set the user details to UI.
        // START
        // Hide the progress dialog
        hideProgressDialog()

        // Load the image using the Glide Loader class.
        user.image.let { GlideLoader(this@SettingsActivity).loadUserProfile(it, iv_user_photo) }

        tv_name.text = "${user.firstName} ${user.lastName}"
        tv_gender.text = user.gender
        tv_email.text = user.email
        tv_mobile_number.text = "${user.mobile}"
        // END
    }
    // END
}