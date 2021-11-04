package com.example.mye_commerce.model

import android.os.Parcel
import android.os.Parcelable
import com.example.mye_commerce.model.Address
import com.example.mye_commerce.model.CartItem

data class Order (
    val user_id : String = "",
    val items : ArrayList<CartItem> = ArrayList(),
    val address: Address = Address(),
    val title : String = "",
    val image : String = "",
    val sub_total_amount : String = "",
    val shipping_charge : String = "",
    val total_amount : String = "",
    val order_dateTime : Long = 0L,
    var id : String = ""
        ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        TODO("items"),
        parcel.readParcelable(Address::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}