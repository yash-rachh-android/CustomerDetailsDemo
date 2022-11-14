package com.yrcoding.data.dao

import com.yrcoding.data.model.Customer
import io.ktor.utils.io.core.*

interface DAOInterface : Closeable {
    fun init()
    fun createCustomer(
        email: String,
        first_name: String,
        last_name: String,
        city: String,
        mobile: String,
        password: String
    ): Customer?
    fun getCustomer(email: String): Customer?
    fun getCustomerThroughMobile(mobile: String): Customer?
    fun getCustomerThroughId(id: Int): Customer?
    fun getCustomerList(): ArrayList<Customer>
    fun getCustomerListExcludingCurrent(id : Int): ArrayList<Customer>
    fun removeCustomer(id: Int)
    fun updateCustomer(user : Customer): Customer?
}