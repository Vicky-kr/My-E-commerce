package com.example.mye_commerce.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.User
import com.example.mye_commerce.utils.Constants
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : BaseActivity(){

    private lateinit var tv_forgot_password:TextView
    private lateinit var  tv_register:TextView
    private lateinit var btn_login: Button
    private lateinit var et_email:EditText
    private lateinit var et_password:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        this.tv_register = findViewById(R.id.tv_register)
        this.tv_forgot_password = findViewById(R.id.tv_forgot_password)
        this.et_email = findViewById(R.id.et_email)
        this.et_password = findViewById(R.id.et_password)
        this.btn_login = findViewById(R.id.btn_login)


        // This is used to hide the status bar and make the login screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        tv_register.setOnClickListener {
            startActivity(Intent(this@LoginActivity,RegisterActivity::class.java))
            finish()
        }
        tv_forgot_password.setOnClickListener {
            startActivity(Intent(this@LoginActivity,ForgotPasswordActivity::class.java))
        }
        btn_login.setOnClickListener {
            logInRegisteredUser()
        }

    }
    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Get the text from editText and trim the space
            val email = et_email.text.toString().trim { it <= ' ' }
            val password = et_password.text.toString().trim { it <= ' ' }

            // Log-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
//                        FireStoreClass().getUserDetails(this@LoginActivity)
                        startActivity(Intent(this@LoginActivity,DashboardActivity::class.java))
                        finish()
                    } else {
                        // Hide the progress dialog
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }
    fun userLoggedInSuccess(user: User) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Dashboard Screen after log in.
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }
}