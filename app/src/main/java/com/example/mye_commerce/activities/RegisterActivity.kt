package com.example.mye_commerce.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.*
import com.example.mye_commerce.R
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        val btn_register:Button = findViewById(R.id.btn_register)
        val tv_login:TextView = findViewById(R.id.tv_login)
//    private var toolbar_register_activity:Toolbar = findViewById(R.id.toolbar_register_activity)
        val et_email:EditText = findViewById(R.id.et_email)
        val et_first_name:EditText = findViewById(R.id.et_first_name)
        val et_last_name:EditText = findViewById(R.id.et_last_name)
        val et_password:EditText = findViewById(R.id.et_password)
        val et_confirm_password:EditText = findViewById(R.id.et_confirm_password)
        val cb_terms_and_condition:CheckBox = findViewById(R.id.cb_terms_and_condition)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        btn_register.setOnClickListener {
            registerUser(
                et_first_name,
                et_last_name,
                et_email,
                et_password,
                et_confirm_password,
                cb_terms_and_condition
            )
        }
        tv_login.setOnClickListener {
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            finish()
        }


    }
    /*
       Function to register user
     */

    private fun registerUser(
        et_first_name:EditText,
        et_last_name:EditText,
        et_email:EditText,
        et_password:EditText,
        et_confirm_password: EditText,
        cb_terms_and_condition: CheckBox
    ) {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails(et_first_name,et_last_name,et_email,et_password
                ,et_confirm_password,cb_terms_and_condition)) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = et_email.text.toString().trim { it <= ' ' }
            val password: String = et_password.text.toString().trim { it <= ' ' }

            // Create an instance and create a register a user with email and password.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        // If the registration is successfully done
                        if (task.isSuccessful) {

                            // Firebase registered user
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            // Instance of User data model class.
                            val user = User(
                                firebaseUser.uid,
                                et_first_name.text.toString().trim { it <= ' ' },
                                et_last_name.text.toString().trim { it <= ' ' },
                                et_email.text.toString().trim { it <= ' ' }
                            )

                            // Pass the required values in the constructor.
                            FireStoreClass().registerUser(this@RegisterActivity, user)
                        } else {

                            // Hide the progress dialog
                            hideProgressDialog()

                            // If the registering is not successful then show error message.
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    })
        }
    }

    /*
       Function to validate the user
     */
    private fun validateRegisterDetails(et_first_name:EditText,et_last_name:EditText,
                                        et_email:EditText,et_password:EditText
                                        ,et_confirm_password:EditText,cb_terms_and_condition:CheckBox): Boolean {
        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_confirm_password),
                    true
                )
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                    true
                )
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_agree_terms_and_condition),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }
    /*
       Function to notify if the registration is successfull
     */
    fun userRegistrationSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.register_success),
            Toast.LENGTH_SHORT
        ).show()


//            FirebaseAuth.getInstance().signOut()
        // Finish the Register Screen
        startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
        finish()
    }
}