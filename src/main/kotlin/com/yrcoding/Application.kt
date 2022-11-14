package com.yrcoding

import com.yrcoding.data.dao.CustomerDao
import io.ktor.server.application.*
import com.yrcoding.plugins.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
