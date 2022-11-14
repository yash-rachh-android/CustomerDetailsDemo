package com.yrcoding.routes

import com.yrcoding.data.dao.CustomerDao
import com.yrcoding.data.model.Customer
import com.yrcoding.data.model.Response
import com.yrcoding.data.model.UploadImage
import com.yrcoding.util.Constant.BASE_URL
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Route.customer() {

    val dao =
        CustomerDao(Database.connect("jdbc:h2:file:./build/db", driver = "org.h2.Driver", user = "root", password = ""))
    dao.init()

    post("/add-customer") {

        //receive parameters
        val parameters = call.receiveParameters()
        print("Parameters==> $parameters")

        //check if any of the required field is missing or not
        if (parameters["email"].isNullOrEmpty() || parameters["firstName"].isNullOrEmpty()
            || parameters["lastName"].isNullOrEmpty() || parameters["mobile"].isNullOrEmpty()
            || parameters["city"].isNullOrEmpty() || parameters["password"].isNullOrEmpty()
        ) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }

        //if email not registered then add to database else show error message
        val customerThroughEmail = dao.getCustomer(parameters["email"]!!)
        val customerThroughMobile = dao.getCustomerThroughMobile(parameters["mobile"]!!)
        if (customerThroughEmail == null && customerThroughMobile == null) {
            val customer = dao.createCustomer(
                email = parameters["email"]!!,
                first_name = parameters["firstName"]!!,
                last_name = parameters["lastName"]!!,
                city = parameters["city"]!!,
                mobile = parameters["mobile"]!!,
                password = parameters["password"]!!
            )
            val response = Response<Customer>(HttpStatusCode.OK.value, "Customer registered successfully.", customer!!)
            call.respond(HttpStatusCode.OK, response)
        } else {
            if (customerThroughEmail != null) {
                val response = Response<Customer>(
                    HttpStatusCode.BadRequest.value,
                    "The email is already registered with us.",
                    null
                )
                call.respond(HttpStatusCode.BadRequest, response)
            }
            if (customerThroughMobile != null) {
                val response = Response<Customer>(
                    HttpStatusCode.BadRequest.value,
                    "The mobile number is already registered with us.",
                    null
                )
                call.respond(HttpStatusCode.BadRequest, response)
            }
        }
    }

    get("/list-customer") {
        call.respond(HttpStatusCode.OK, dao.getCustomerList())
    }

    get("/list-other-customer") {
        if (call.request.queryParameters["id"].isNullOrEmpty()) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }else{
            call.respond(HttpStatusCode.OK, dao.getCustomerListExcludingCurrent(call.request.queryParameters["id"]!!.toInt()))
        }
    }

    post("/login") {

        //receive parameters
        val parameters = call.receiveParameters()
        print("Parameters==> $parameters")


        //check if any of the required field is missing or not
        if (parameters["email"].isNullOrEmpty() || parameters["password"].isNullOrEmpty()
        ) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }

        //check if the email is registered or not
        val user = dao.getCustomer(parameters["email"]!!)
        if (user != null) {
            //check if the password matches or not
            if (parameters["password"]!!.equals(user.password, false)) {
                user.password = null
                val response = Response<Customer>(HttpStatusCode.OK.value, "Login successful.", user)
                call.respond(HttpStatusCode.OK, response)
            } else {
                val response = Response<Customer>(HttpStatusCode.BadRequest.value, "Invalid Credentials.", null)
                call.respond(HttpStatusCode.BadRequest, response)
            }
        } else {
            val response =
                Response<Customer>(HttpStatusCode.BadRequest.value, "The user is not registered with us.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }
    }

    post("/remove-account") {

        val parameters = call.receiveParameters()
        print("Parameters==> $parameters")

        if (parameters["id"] == null) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }

        val user = dao.getCustomerThroughId(parameters["id"]!!.toInt())
        if (user != null) {
            dao.removeCustomer(parameters["id"]!!.toInt())
            val response = Response<Customer>(HttpStatusCode.OK.value, "Account Deleted.", null)
            call.respond(HttpStatusCode.OK, response)
        } else {
            val response =
                Response<Customer>(HttpStatusCode.BadRequest.value, "The user is not registered with us.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        }
    }

    post("/upload-file") {
        var fileName = ""
        val parameters = call.receiveMultipart()
        print("Parameters==> $parameters")

        parameters.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    if (part.name != null && part.name == "image") {
                        if (!part.originalFileName.isNullOrEmpty()) {
                            fileName = getFilenameWithDate(part.originalFileName!!, ".", "yyyyMMMdd_HHmmSS")
                            var fileBytes = part.streamProvider().readBytes()
                            File("uploads/$fileName").writeBytes(fileBytes)

                            val fileData = UploadImage(
                                fileName = fileName,
                                url = "${BASE_URL}/root/$fileName"
                            )
                            val response =
                                Response<UploadImage>(
                                    HttpStatusCode.OK.value,
                                    "Image Uploaded Successfully",
                                    fileData
                                )
                            call.respond(HttpStatusCode.OK, response)
                        } else {
                            val response =
                                Response<String>(
                                    HttpStatusCode.BadRequest.value,
                                    "One or more parameters missing.",
                                    null
                                )
                            call.respond(HttpStatusCode.BadRequest, response)
                        }
                    } else {
                        val response =
                            Response<String>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
                        call.respond(HttpStatusCode.BadRequest, response)
                    }
                }

                else -> {}
            }
        }
    }

    post("/update-profile/{id}") {
        val id = call.parameters["id"]
        if (id.isNullOrEmpty()) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        } else {
            val parameters = call.receiveParameters()
            print("Parameters==> $parameters")
            val user = dao.getCustomerThroughId(id.toInt())
            if (user == null) {
                val response =
                    Response<Customer>(HttpStatusCode.BadRequest.value, "No user found with the given id.", null)
                call.respond(HttpStatusCode.BadRequest, response)
            } else {
                if (parameters.isEmpty()) {
                    val response = Response<Customer>(
                        HttpStatusCode.BadRequest.value,
                        "You need to pass at-least one field to update the profile.",
                        null
                    )
                    call.respond(HttpStatusCode.BadRequest, response)
                } else {
                    if (!parameters["firstName"].isNullOrEmpty()) {
                        user.firstName = parameters["firstName"]
                    }
                    if (!parameters["lastName"].isNullOrEmpty()) {
                        user.lastName = parameters["lastName"]
                    }
                    if (!parameters["city"].isNullOrEmpty()) {
                        user.city = parameters["city"]
                    }
                    if (!parameters["profile"].isNullOrEmpty()) {
                        user.profile = parameters["profile"]
                    }
                    val customer = dao.updateCustomer(user)
                    val response = Response<Customer>(HttpStatusCode.OK.value, "Customer details updated.", customer!!)
                    call.respond(HttpStatusCode.OK, response)
                }
            }
        }
    }

    get("/get-profile") {
        /*val parameters = call.receiveParameters()
        print("Parameters==> $parameters")*/

        if (call.request.queryParameters["id"].isNullOrEmpty()) {
            val response = Response<Customer>(HttpStatusCode.BadRequest.value, "One or more parameters missing.", null)
            call.respond(HttpStatusCode.BadRequest, response)
        } else {
            val user = dao.getCustomerThroughId(call.request.queryParameters["id"]!!.toInt())
            if (user == null) {
                val response =
                    Response<Customer>(HttpStatusCode.BadRequest.value, "No user found with the given id.", null)
                call.respond(HttpStatusCode.BadRequest, response)
            } else {
                user.password = null
                val response = Response<Customer>(HttpStatusCode.OK.value, "", user)
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }
}

fun getFilenameWithDate(fileName: String, fileSeparator: String, dateFormat: String): String {
//    var fileNamePrefix = fileName.substring(0, fileName.lastIndexOf(fileSeparator))
    var fileNamePrefix = "profile_image"
    var fileNameSuffix = fileName.substring(fileName.lastIndexOf(fileSeparator) + 1, fileName.length)

    val newFileName =
        SimpleDateFormat("'" + fileNamePrefix + "_'" + dateFormat + "'" + fileSeparator + fileNameSuffix + "'").format(
            Date()
        )
    return newFileName
}
