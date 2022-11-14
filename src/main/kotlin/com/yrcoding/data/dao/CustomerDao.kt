package com.yrcoding.data.dao

import com.yrcoding.util.Constant.BASE_URL
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction

class CustomerDao(val db: Database) : DAOInterface {
    override fun init() = transaction(db) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Customer)
    }

    override fun createCustomer(
        email: String,
        first_name: String,
        last_name: String,
        city: String,
        mobile: String,
        password: String
    ) = transaction(db) {
        Customer.insert {
            it[Customer.email] = email
            it[Customer.firstName] = first_name
            it[Customer.lastName] = last_name
            it[Customer.city] = city
            it[Customer.mobile] = mobile
            it[Customer.password] = password
            it[Customer.profile] = ""
        }
        Customer.select { Customer.email eq email }.map {
            com.yrcoding.data.model.Customer(
                it[Customer.id],
                it[Customer.email],
                it[Customer.firstName],
                it[Customer.lastName],
                it[Customer.city],
                it[Customer.mobile],
                null,
                if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
            )
        }.singleOrNull()
    }

    override fun getCustomer(email: String) = transaction(db) {
        Customer.select { Customer.email eq email }.map {
            com.yrcoding.data.model.Customer(
                it[Customer.id],
                it[Customer.email],
                it[Customer.firstName],
                it[Customer.lastName],
                it[Customer.city],
                it[Customer.mobile],
                it[Customer.password],
                if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
            )
        }.singleOrNull()
    }

    override fun getCustomerThroughMobile(mobile: String) = transaction(db) {
        Customer.select { Customer.mobile eq mobile }.map {
            com.yrcoding.data.model.Customer(
                it[Customer.id],
                it[Customer.email],
                it[Customer.firstName],
                it[Customer.lastName],
                it[Customer.city],
                it[Customer.mobile],
                null,
                if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
            )
        }.singleOrNull()
    }

    override fun getCustomerThroughId(id: Int) = transaction(db) {
        Customer.select { Customer.id eq id }.map {
            com.yrcoding.data.model.Customer(
                it[Customer.id],
                it[Customer.email],
                it[Customer.firstName],
                it[Customer.lastName],
                it[Customer.city],
                it[Customer.mobile],
                null,
                if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
            )
        }.singleOrNull()
    }

    override fun getCustomerList() = transaction(db) {
        val customers: ArrayList<com.yrcoding.data.model.Customer> = arrayListOf()
        Customer.selectAll().map {
            customers.add(
                com.yrcoding.data.model.Customer(
                    id = it[Customer.id],
                    email = it[Customer.email],
                    firstName = it[Customer.firstName],
                    lastName = it[Customer.lastName],
                    city = it[Customer.city],
                    mobile = it[Customer.mobile],
                    profile = if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
                )
            )
        }
        customers
    }

    override fun getCustomerListExcludingCurrent(id : Int) = transaction(db) {
        val customers: ArrayList<com.yrcoding.data.model.Customer> = arrayListOf()
        Customer.select { Customer.id neq id }.map{
            customers.add(
                com.yrcoding.data.model.Customer(
                    id = it[Customer.id],
                    email = it[Customer.email],
                    firstName = it[Customer.firstName],
                    lastName = it[Customer.lastName],
                    city = it[Customer.city],
                    mobile = it[Customer.mobile],
                    profile = if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
                )
            )
        }
        customers
    }

    override fun removeCustomer(id: Int) = transaction(db) {
        Customer.deleteWhere { Customer.id eq id }
        Unit
    }

    override fun updateCustomer(user: com.yrcoding.data.model.Customer) = transaction(db) {
        Customer.update({ Customer.id eq user.id!!.toInt() }) {
            it[email] = user.email!!
            it[firstName] = user.firstName!!
            it[lastName] = user.lastName!!
            it[city] = user.city!!
            it[mobile] = user.mobile!!
            it[profile] = user.profile!!
        }
        Customer.select { Customer.id eq user.id!!.toInt() }.map {
            com.yrcoding.data.model.Customer(
                it[Customer.id],
                it[Customer.email],
                it[Customer.firstName],
                it[Customer.lastName],
                it[Customer.city],
                it[Customer.mobile],
                null,
                if (it[Customer.profile].isNotEmpty()) "${BASE_URL}/root/${it[Customer.profile]}" else ""
            )
        }.singleOrNull()
    }

    override fun close() {

    }
}