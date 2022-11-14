package com.yrcoding.plugins

import com.yrcoding.data.dao.CustomerDao
import com.yrcoding.data.model.Customer
import com.yrcoding.routes.customer
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting() {

    routing {
        customer()
        // Static plugin. Try to access `/static/index.html`
        static("/root") {
            files("uploads")
        }

    }
}
