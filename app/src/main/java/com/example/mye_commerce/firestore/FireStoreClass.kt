package com.example.mye_commerce.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.mye_commerce.activities.*
import com.example.mye_commerce.fragments.DashboardFragment
import com.example.mye_commerce.fragments.OrdersFragment
import com.example.mye_commerce.fragments.ProductsFragment
import com.example.mye_commerce.fragments.SoldProductFragment
import com.example.mye_commerce.model.*
import com.example.mye_commerce.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


/**
 * A custom class where we will add the operation performed for the FireStore database.
 */
class FireStoreClass {


    // Access a Cloud Firestore instance.
    private val mFireStore = FirebaseFirestore.getInstance()


    /**
     * A function to make an entry of the registered user in the FireStore database.
     */
    fun registerUser(activity: RegisterActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id!!)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    /**
     * A function to get the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }

        return currentUserID
    }

    /**
     * A function to get the logged user details from from FireStore Database.
     */
    fun getUserDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                Log.i(activity.javaClass.simpleName, document.toString())

                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                val sharedPreferences =
                    activity.getSharedPreferences(
                        Constants.MYSHOPPAL_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                // Create an instance of the editor which is help us to edit the SharedPreference.
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.firstName} ${user.lastName}"
                )
                editor.apply()

                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userLoggedInSuccess(user)
                    }

                    is SettingsActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SettingsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }


    /**
     * A function to update the user profile data into the database.
     *
     * @param activity The activity is used for identifying the Base activity to which the result is passed.
     * @param userHashMap HashMap of fields which are to be updated.
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        // Collection Name
        mFireStore.collection(Constants.USERS)
            // Document ID against which the data to be updated. Here the document id is the current logged in user id.
            .document(getCurrentUserID())
            // A HashMap of fields which are to be updated.
            .update(userHashMap)
            .addOnSuccessListener {

                // Notify the success result.
                when (activity) {
                    is UserProfileActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                when (activity) {
                    is UserProfileActivity -> {
                        // Hide the progress dialog if there is any error. And print the error in log.
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the user details.",
                    e
                )
            }
    }

    // A function to upload the image to the cloud storage.
    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // Here call a function of base activity for transferring the result to it.
                        when (activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }

                            is AddProductActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->

                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
    }

    /**
     * A function to make an entry of the user's product in the cloud firestore database.
     */
    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {

        mFireStore.collection(Constants.PRODUCTS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.productUploadSuccess()
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while uploading the product details.",
                    e
                )
            }
    }

    /**
     * A function to get the products list from cloud firestore.
     *
     * @param fragment The fragment is passed as parameter as the function is called from fragment and need to the success result.
     */
    fun getProductsList(fragment: Fragment) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.hideProgressDialog()
                    }
                }

                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    /**
     * A function to get all the product list from the cloud firestore.
     *
     * @param activity The activity is passed as parameter to the function because it is called from activity and need to the success result.
     */
    fun getAllProductsList(activity: Activity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productsList.add(product)
                }
                when(activity){
                    is CartListActivity ->{
                        activity.successProductsListFromFireStore(productsList)
                    }
                    is CheckoutActivity ->{
                        activity.successProductsListFromFireStore(productsList)
                    }
                }


            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when(activity){
                    is CartListActivity ->{
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity ->{
                        activity.hideProgressDialog()
                    }
                }



                Log.e("Get Product List", "Error while getting all product list.", e)
            }
    }

    /**
     * A function to get the dashboard items list. The list will be an overall items list, not based on the user's id.
     */
    fun getDashboardItemsList(fragment: DashboardFragment) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e(fragment.javaClass.simpleName, document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)!!
                    product.product_id = i.id
                    productsList.add(product)
                }

                // Pass the success result to the base fragment.
                fragment.successDashboardItemsList(productsList)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error which getting the dashboard items list.
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list.", e)
            }
    }

    /**
     * A function to delete the product from the cloud firestore.
     */
    fun deleteProduct(fragment: ProductsFragment, productId: String) {

        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {

                // Notify the success result to the base class.
                fragment.productDeleteSuccess()
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is an error.
                fragment.hideProgressDialog()

                Log.e(
                    fragment.requireActivity().javaClass.simpleName,
                    "Error while deleting the product.",
                    e
                )
            }
    }

    /**
     * A function to get the product details based on the product id.
     */
    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {

        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get() // Will get the document snapshots.
            .addOnSuccessListener { document ->

                // Here we get the product details in the form of document.
                Log.e(activity.javaClass.simpleName, document.toString())

                // Convert the snapshot to the object of Product data model class.
                val product = document.toObject(Product::class.java)!!

                activity.productDetailsSuccess(product)
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is an error.
                activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e)
            }
    }

    /**
     * A function to add the item to the cart in the cloud firestore.
     *
     * @param activity
     * @param addToCart
     */
    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {

        mFireStore.collection(Constants.CART_ITEMS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addToCartSuccess()
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating the document for cart item.",
                    e
                )
            }
    }

    /**
     * A function to check whether the item already exist in the cart or not.
     */
    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {

        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->

                Log.e(activity.javaClass.simpleName, document.documents.toString())

                // If the document size is greater than 1 it means the product is already added to the cart.
                if (document.documents.size > 0) {
                    activity.productExistsInCart()
                } else {
                    activity.hideProgressDialog()
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error.
                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking the existing cart list.",
                    e
                )
            }
    }

    /**
     * A function to get the cart items list from the cloud firestore.
     *
     * @param activity
     */
    fun getCartList(activity: Activity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of cart items in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                // Here we have created a new instance for Cart Items ArrayList.
                val list: ArrayList<CartItem> = ArrayList()

                // A for loop as per the list of documents to convert them into Cart Items ArrayList.
                for (i in document.documents) {

                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id

                    list.add(cartItem)
                }

                when (activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(list)
                    }
                    is CheckoutActivity ->{
                        activity.successCartItemsList(list);
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error based on the activity instance.
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity ->{
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e)
            }
    }

    /**
     * A function to remove the cart item from the cloud firestore.
     *
     * @param activity activity class.
     * @param cart_id cart id of the item.
     */
    fun removeItemFromCart(context: Context, cart_id: String) {

        // Cart items collection name
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .delete()
            .addOnSuccessListener {

                // Notify the success result of the removed cart item from the list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e(
                    context.javaClass.simpleName,
                    "Error while removing the item from the cart list.",
                    e
                )
            }
    }

    // TODO Step 2: Create a function to update the cart item in the cloud firestore.
    // START
    /**
     * A function to update the cart item in the cloud firestore.
     *
     * @param activity activity class.
     * @param id cart id of the item.
     * @param itemHashMap to be updated values.
     */
    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {

        // Cart items collection name
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .update(itemHashMap) // A HashMap of fields which are to be updated.
            .addOnSuccessListener {

                // TODO Step 4: Notify the success result of the updated cart items list to the base class.
                // START
                // Notify the success result of the updated cart items list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }
                // END
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }

                Log.e(
                    context.javaClass.simpleName,
                    "Error while updating the cart item.",
                    e
                )
            }
    }

    /**
     *  Function to update the address
     */
    fun updateAddress(activity: AddEditAddressActivity,
                      addressInfo: Address,
                      addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while updating the address",
                    exception)
            }

    }

    /**
     * Function to add the address
     */

    fun addAddress(activity: AddEditAddressActivity,
                   addressInfo: Address
    ) {
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while adding the address",
                    exception)
            }
    }
    /**
     * Function to read address
     */
    fun getAddressesList(activity:AddressListActivity){
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document->
                Log.d("Address List : ",document.documents.toString())
                val addressList:ArrayList<Address> = ArrayList()
                for (i in document.documents){
                    val address = i.toObject(Address::class.java)
                    address!!.id = i.id
                    addressList.add(address)
                }
                activity.successAddressListFromFireStore(addressList)
            }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e("Get Address List","Error while getting all address list",exception)
            }
    }
    /**
     * Function to delete the address
     */
    fun deleteAddress(activity:AddressListActivity,addressId: String){
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(exception.javaClass.simpleName,
                    "Error while deleting the address",
                    exception)
            }
    }
    /**
     * Function to store order details in FireStore
     */

    fun placeOrder(activity: CheckoutActivity,order: Order){
        mFireStore.collection(Constants.ORDER)
            .document()
            .set(order, SetOptions.merge())
            .addOnSuccessListener {

                activity.orderPlaceSuccess()
            }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while placing an order",
                    exception
                )
            }
    }

    /**
     * Function to update the details such as stock quantity after a user placed the order
     * and removing the item from cart
     */
    fun updateAllDetails(activity: CheckoutActivity,cartList:ArrayList<CartItem>,order: Order){
        val writeBatch = mFireStore.batch()


        for (cartItem in cartList){
//            val productHashMap = HashMap<String,Any>()
//            productHashMap[Constants.STOCK_QUANTITY] =
//                    ( (cartItem.stock_quantity!!.toInt()) - (cartItem.cart_quantity!!.toInt())).toString()
            val soldProduct = SoldProduct(
                cartItem.product_owner_id,
                cartItem.title,
                cartItem.price,
                cartItem.cart_quantity,
                cartItem.image,
                order.title,
                order.order_dateTime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )

            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS)
                .document(cartItem.product_id)
            writeBatch.set(documentReference,soldProduct)

//            writeBatch.update(documentReference,soldProduct)
        }
        for(cartItem in cartList){
            val documentReference = mFireStore.collection(Constants.CART_ITEMS)
                .document(cartItem.id)

            writeBatch.delete(documentReference)
        }
        writeBatch.commit().addOnSuccessListener {
            activity.allDetailsUpdateSuccess()
        }
            .addOnFailureListener {
                    exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while updating all the details after order placed",
                    exception)
            }
    }

    /**
     * Function to get the ordersList from firestore
     */
    fun getMyOrdersList(fragment: OrdersFragment){
        mFireStore.collection(Constants.ORDER)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                val list :ArrayList<Order> = ArrayList()
                for (i in document.documents){
                    val orderItem = i.toObject(Order::class.java)
                    orderItem!!.id = i.id
                    list.add(orderItem)
                }
                fragment.populateOrdersListInUI(list)
            }
            .addOnFailureListener {
                    exception ->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName,
                    "Error while getting the orders list.",
                    exception)

            }
    }
    /**
     * Function to get the order details of the product
     */
    fun getOrderDetails(activity: MyOrderDetailsActivity){
        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i("Order Details : ",document.documents.toString())

                activity.successorderList()

            }
            .addOnFailureListener {
                    e->
                activity.hideProgressDialog()
                Log.e("Order Details","Error while getting the order list",e)
            }
    }
    /**
     * Function to get the sold product details
     */
    fun getSoldProductsList(fragment: SoldProductFragment){
        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID,getCurrentUserID())
            .get()
            .addOnSuccessListener {
                    document ->
                Log.i("Order Details : ",document.documents.toString())
                val list :ArrayList<SoldProduct> = ArrayList()
                for (i in document.documents){
                    val soldProduct = i.toObject(SoldProduct::class.java)
                    soldProduct?.id = i.id
                    list.add(soldProduct!!)
                }
                fragment.successSoldProductsList(list);


            }
            .addOnFailureListener {
                    e->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName,
                    "Error while getting the order list",
                    e)
            }
    }

}