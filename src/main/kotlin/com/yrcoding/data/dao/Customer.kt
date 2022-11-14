package com.yrcoding.data.dao

import org.jetbrains.exposed.sql.Table

object Customer : Table() {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255)
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val city = varchar("city", 255)
    val mobile = varchar("mobile", 255)
    val password = varchar("password", 255)
    val profile = varchar("profile", 255)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}