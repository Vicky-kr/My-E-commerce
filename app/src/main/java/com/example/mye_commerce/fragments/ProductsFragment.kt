package com.example.mye_commerce.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mye_commerce.R
import com.example.mye_commerce.activities.AddProductActivity
import com.example.mye_commerce.adapters.MyProductsListAdapter
import com.example.mye_commerce.firestore.FireStoreClass
import com.example.mye_commerce.model.Product


class ProductsFragment : BaseFragment() {
    private lateinit var rv_my_product_items:RecyclerView
    private lateinit var tv_no_products_found:TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_products, container, false)
        rv_my_product_items = root.findViewById(R.id.rv_my_product_items) as RecyclerView
        tv_no_products_found = root.findViewById(R.id.tv_no_products_found) as TextView

        rv_my_product_items.layoutManager = LinearLayoutManager(activity)
        rv_my_product_items.itemAnimator = DefaultItemAnimator()
        return root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_products, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {

            R.id.action_add -> {

                startActivity(Intent(activity, AddProductActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()

        getProductListFromFireStore()
    }

    private fun getProductListFromFireStore() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class.
        FireStoreClass().getProductsList(this@ProductsFragment)
    }

    /**
     * A function to get the successful product list from cloud firestore.
     *
     * @param productsList Will receive the product list from cloud firestore.
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
//        myProductsListAdapter = MyProductsListAdapter(productsList)

        // Hide Progress dialog.
        hideProgressDialog()

        if (productsList.size > 0) {

            rv_my_product_items.visibility = View.VISIBLE
            tv_no_products_found.visibility = View.GONE

            rv_my_product_items.layoutManager = LinearLayoutManager(activity)
            rv_my_product_items.setHasFixedSize(true)

            // TODO Step 7: Pass the third parameter value.
            // START
            val adapterProducts =
                MyProductsListAdapter(requireActivity(), productsList, this@ProductsFragment)
            // END
            rv_my_product_items.adapter = adapterProducts
        } else {
            rv_my_product_items.visibility = View.GONE
            tv_no_products_found.visibility = View.VISIBLE
        }
    }

    // TODO Step 5: Create a function that will call the delete function of FirestoreClass that will delete the product added by the user.
    // START
    /**
     * A function that will call the delete function of FirestoreClass that will delete the product added by the user.
     *
     * @param productID To specify which product need to be deleted.
     */
    fun deleteProduct(productID: String) {

        // Here we will call the delete function of the FirestoreClass. But, for now lets display the Toast message and call this function from adapter class.

//        Toast.makeText(
//            requireActivity(),
//            "You can now delete the product. $productID",
//            Toast.LENGTH_SHORT
//        ).show()
        showAlertDialogToDeleteProduct(productID)
    }
    fun productDeleteSuccess(){
        hideProgressDialog()
        Toast.makeText(
            requireActivity(),
            resources.getString((R.string.product_delete_success_message)),
            Toast.LENGTH_SHORT
        ).show()
        getProductListFromFireStore()
    }

    //function to show alert Dialog when user delete the product

    private fun showAlertDialogToDeleteProduct(productID:String){
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(resources.getString(R.string.delete_dialog_title))
        builder.setMessage(resources.getString(R.string.delete_dialog_message))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton(resources.getString(R.string.yes)){
                dialogInterface, _ ->
            showProgressDialog(resources.getString(R.string.please_wait))

            FireStoreClass().deleteProduct(this,productID)
            dialogInterface.dismiss()
        }
        builder.setNegativeButton(resources.getString(R.string.no)){
                dialogInterface, _ ->
            dialogInterface.dismiss()


        }

        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

}