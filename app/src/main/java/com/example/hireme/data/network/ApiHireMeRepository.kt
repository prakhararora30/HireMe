package com.example.hireme.data.network

import android.util.Log
import com.example.hireme.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Live Network Repository connected to the HireMe Node.js + Express + MongoDB Atlas backend.
 *
 * Defaults to "http://10.0.2.2:5000/api" for Android Emulator testing.
 * If running on a physical device, update baseUrl to your machine's local IP (e.g., "http://192.168.1.10:5000/api")
 * or your hosted cloud domain.
 *
 * If the network is unreachable, it seamlessly falls back to InMemoryHireMeRepository so the app remains responsive.
 */
class ApiHireMeRepository(
    private val baseUrl: String = "http://10.0.2.2:5000/api",
    private val fallback: HireMeRepository = InMemoryHireMeRepository()
) : HireMeRepository {

    private val tag = "ApiHireMeRepository"
    private val scope = CoroutineScope(Dispatchers.IO)

    private var currentUser: User? = null
    private val categoriesFlow = MutableStateFlow<List<ServiceCategory>>(emptyList())
    private val subServicesCache = mutableMapOf<String, MutableStateFlow<List<SubService>>>()
    private val allSubServicesFlow = MutableStateFlow<List<SubService>>(emptyList())
    private val pendingRequestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    private val myRequestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())
    private val myWorkerRequestsFlow = MutableStateFlow<List<ServiceRequest>>(emptyList())

    init {
        // Initial fetch from MongoDB backend
        scope.launch {
            refreshCategories()
            refreshPendingRequests()
            refreshAllSubServices()
        }
    }

    // ── Auth ──────────────────────────────────────────────────────────

    override fun getCurrentUser(): User? = currentUser ?: fallback.getCurrentUser()

    override fun login(email: String, password: String, role: UserRole): User {
        val payload = buildJsonString(
            "email" to email,
            "password" to password,
            "role" to role.name
        )
        val result = executeAuthRequest("$baseUrl/auth/login", payload)
        return when (result) {
            is AuthHttpResult.Success -> {
                val data = result.data.getJSONObject("data")
                val user = parseUser(data)
                currentUser = user
                refreshUserRequests(user.id, user.role)
                user
            }
            is AuthHttpResult.AuthError -> {
                throw AuthException(result.message)
            }
            is AuthHttpResult.ServerUnreachable -> {
                // Fall back to in-memory repository when backend server is unreachable
                fallback.login(email, password, role).also {
                    currentUser = it
                }
            }
        }
    }

    override fun register(name: String, email: String, password: String, role: UserRole): User {
        val payload = buildJsonString(
            "name" to name,
            "email" to email,
            "password" to password,
            "role" to role.name
        )
        val result = executeAuthRequest("$baseUrl/auth/register", payload)
        return when (result) {
            is AuthHttpResult.Success -> {
                val data = result.data.getJSONObject("data")
                val user = parseUser(data)
                currentUser = user
                refreshUserRequests(user.id, user.role)
                user
            }
            is AuthHttpResult.AuthError -> {
                throw AuthException(result.message)
            }
            is AuthHttpResult.ServerUnreachable -> {
                // Fall back to in-memory repository when backend server is unreachable
                fallback.register(name, email, password, role).also {
                    currentUser = it
                }
            }
        }
    }

    override fun logout() {
        currentUser = null
        fallback.logout()
        myRequestsFlow.value = emptyList()
        myWorkerRequestsFlow.value = emptyList()
    }

    // ── Categories & Sub-Services ─────────────────────────────────────

    override fun getCategories(): Flow<List<ServiceCategory>> {
        scope.launch { refreshCategories() }
        return if (categoriesFlow.value.isNotEmpty()) categoriesFlow else fallback.getCategories()
    }

    override fun getCategoryById(categoryId: String): ServiceCategory? {
        return categoriesFlow.value.find { it.id == categoryId } ?: fallback.getCategoryById(categoryId)
    }

    override fun getSubServices(categoryId: String): Flow<List<SubService>> {
        val flow = subServicesCache.getOrPut(categoryId) { MutableStateFlow(emptyList()) }
        scope.launch { refreshSubServices(categoryId) }
        return flow
    }

    override fun getAllSubServices(): Flow<List<SubService>> {
        scope.launch { refreshAllSubServices() }
        return if (allSubServicesFlow.value.isNotEmpty()) allSubServicesFlow else fallback.getAllSubServices()
    }

    // ── Requests ──────────────────────────────────────────────────────

    override fun createRequest(subService: SubService, address: String, description: String): ServiceRequest {
        val user = getCurrentUser() ?: fallback.getCurrentUser() ?: User("u_guest", "Customer", "guest@hireme.com", UserRole.CUSTOMER)
        return try {
            val subJson = JSONObject().apply {
                put("id", subService.id)
                put("categoryId", subService.categoryId)
                put("name", subService.name)
                put("description", subService.description)
                put("basePrice", subService.basePrice)
            }
            val reqJson = JSONObject().apply {
                put("customerId", user.id)
                put("customerName", user.name)
                put("subService", subJson)
                put("address", address)
                put("description", description)
            }
            val response = postJson("$baseUrl/requests", reqJson)
            if (response != null && response.optBoolean("success")) {
                val request = parseRequest(response.getJSONObject("data"))
                myRequestsFlow.update { listOf(request) + it }
                scope.launch { refreshPendingRequests() }
                request
            } else {
                fallback.createRequest(subService, address, description)
            }
        } catch (e: Exception) {
            Log.w(tag, "Backend createRequest failed (${e.message}), using fallback", e)
            fallback.createRequest(subService, address, description)
        }
    }

    override fun getPendingRequests(): Flow<List<ServiceRequest>> {
        scope.launch { refreshPendingRequests() }
        return pendingRequestsFlow
    }

    override fun getMyRequests(): Flow<List<ServiceRequest>> {
        val user = getCurrentUser()
        if (user != null) {
            scope.launch { refreshCustomerRequests(user.id) }
        }
        return myRequestsFlow
    }

    override fun getMyWorkerRequests(): Flow<List<ServiceRequest>> {
        val user = getCurrentUser()
        if (user != null) {
            scope.launch { refreshWorkerRequests(user.id) }
        }
        return myWorkerRequestsFlow
    }

    override fun getRequestById(requestId: String): ServiceRequest? {
        val all = myRequestsFlow.value + pendingRequestsFlow.value + myWorkerRequestsFlow.value
        return all.find { it.id == requestId } ?: fallback.getRequestById(requestId)
    }

    override fun acceptRequest(requestId: String): ServiceRequest? {
        val user = getCurrentUser()
        return try {
            val json = JSONObject().apply {
                put("workerId", user?.id)
                put("workerName", user?.name ?: "Worker")
            }
            val response = putJson("$baseUrl/requests/$requestId/accept", json)
            if (response != null && response.optBoolean("success")) {
                val req = parseRequest(response.getJSONObject("data"))
                scope.launch {
                    refreshPendingRequests()
                    user?.id?.let { refreshWorkerRequests(it) }
                }
                req
            } else {
                fallback.acceptRequest(requestId)
            }
        } catch (e: Exception) {
            Log.w(tag, "Backend acceptRequest failed, using fallback", e)
            fallback.acceptRequest(requestId)
        }
    }

    override fun completeRequest(requestId: String): ServiceRequest? {
        val user = getCurrentUser()
        return try {
            val response = putJson("$baseUrl/requests/$requestId/complete", JSONObject())
            if (response != null && response.optBoolean("success")) {
                val req = parseRequest(response.getJSONObject("data"))
                scope.launch { user?.id?.let { refreshWorkerRequests(it) } }
                req
            } else {
                fallback.completeRequest(requestId)
            }
        } catch (e: Exception) {
            Log.w(tag, "Backend completeRequest failed, using fallback", e)
            fallback.completeRequest(requestId)
        }
    }

    // ── Rating & Payment ──────────────────────────────────────────────

    override fun submitRating(requestId: String, score: Int, comment: String): ServiceRequest? {
        val user = getCurrentUser()
        return try {
            val json = JSONObject().apply {
                put("score", score)
                put("comment", comment)
            }
            val response = postJson("$baseUrl/requests/$requestId/rate", json)
            if (response != null && response.optBoolean("success")) {
                val req = parseRequest(response.getJSONObject("data"))
                scope.launch { user?.id?.let { refreshCustomerRequests(it) } }
                req
            } else {
                fallback.submitRating(requestId, score, comment)
            }
        } catch (e: Exception) {
            Log.w(tag, "Backend submitRating failed, using fallback", e)
            fallback.submitRating(requestId, score, comment)
        }
    }

    override fun makePayment(requestId: String, method: String): ServiceRequest? {
        val user = getCurrentUser()
        return try {
            val json = JSONObject().apply {
                put("method", method)
            }
            val response = postJson("$baseUrl/requests/$requestId/pay", json)
            if (response != null && response.optBoolean("success")) {
                val req = parseRequest(response.getJSONObject("data").getJSONObject("request"))
                scope.launch { user?.id?.let { refreshCustomerRequests(it) } }
                req
            } else {
                fallback.makePayment(requestId, method)
            }
        } catch (e: Exception) {
            Log.w(tag, "Backend makePayment failed, using fallback", e)
            fallback.makePayment(requestId, method)
        }
    }

    // ── Async Background Refresh Helpers ──────────────────────────────

    private suspend fun refreshCategories() {
        try {
            val response = getJson("$baseUrl/categories")
            if (response != null && response.optBoolean("success")) {
                val array = response.getJSONArray("data")
                val list = mutableListOf<ServiceCategory>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ServiceCategory(
                            id = obj.getString("_id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            iconKey = obj.optString("iconKey", obj.getString("_id"))
                        )
                    )
                }
                categoriesFlow.value = list
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch categories from server: ${e.message}")
        }
    }

    private suspend fun refreshSubServices(categoryId: String) {
        try {
            val response = getJson("$baseUrl/categories/$categoryId/subservices")
            if (response != null && response.optBoolean("success")) {
                val array = response.getJSONArray("data")
                val list = mutableListOf<SubService>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SubService(
                            id = obj.getString("_id"),
                            categoryId = obj.getString("categoryId"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            basePrice = obj.getInt("basePrice")
                        )
                    )
                }
                val flow = subServicesCache.getOrPut(categoryId) { MutableStateFlow(emptyList()) }
                flow.value = list
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch subservices for $categoryId: ${e.message}")
            val fallbackList = fallback.getSubServices(categoryId)
            // If cache is empty, populate from fallback
            val flow = subServicesCache.getOrPut(categoryId) { MutableStateFlow(emptyList()) }
            if (flow.value.isEmpty()) {
                flow.value = (fallback as? InMemoryHireMeRepository)?.let {
                    // Let fallback provide default
                    emptyList()
                } ?: emptyList()
            }
        }
    }

    private suspend fun refreshAllSubServices() {
        try {
            val categories = listOf(
                CategoryIds.HOME_CARE,
                CategoryIds.APPLIANCES,
                CategoryIds.ELECTRIC,
                CategoryIds.PLUMBING,
                CategoryIds.AUTOMOTIVE,
                CategoryIds.BEAUTY,
                CategoryIds.HEALTH_CARE,
                CategoryIds.FURNITURE,
                CategoryIds.INSTALLATION
            )
            val allList = mutableListOf<SubService>()
            for (catId in categories) {
                val response = getJson("$baseUrl/categories/$catId/subservices")
                if (response != null && response.optBoolean("success")) {
                    val array = response.getJSONArray("data")
                    val catList = mutableListOf<SubService>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val sub = SubService(
                            id = obj.getString("_id"),
                            categoryId = obj.getString("categoryId"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            basePrice = obj.getInt("basePrice")
                        )
                        catList.add(sub)
                        allList.add(sub)
                    }
                    val flow = subServicesCache.getOrPut(catId) { MutableStateFlow(emptyList()) }
                    flow.value = catList
                }
            }
            if (allList.isNotEmpty()) {
                allSubServicesFlow.value = allList
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch all subservices from server: ${e.message}")
        }
    }

    private suspend fun refreshPendingRequests() {
        try {
            val response = getJson("$baseUrl/requests/pending")
            if (response != null && response.optBoolean("success")) {
                val array = response.getJSONArray("data")
                val list = parseRequestsArray(array)
                pendingRequestsFlow.value = list
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch pending requests: ${e.message}")
        }
    }

    private suspend fun refreshCustomerRequests(customerId: String) {
        try {
            val response = getJson("$baseUrl/requests/my?customerId=$customerId")
            if (response != null && response.optBoolean("success")) {
                val array = response.getJSONArray("data")
                myRequestsFlow.value = parseRequestsArray(array)
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch customer requests: ${e.message}")
        }
    }

    private suspend fun refreshWorkerRequests(workerId: String) {
        try {
            val response = getJson("$baseUrl/requests/worker?workerId=$workerId")
            if (response != null && response.optBoolean("success")) {
                val array = response.getJSONArray("data")
                myWorkerRequestsFlow.value = parseRequestsArray(array)
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not fetch worker requests: ${e.message}")
        }
    }

    private fun refreshUserRequests(userId: String, role: UserRole) {
        scope.launch {
            if (role == UserRole.CUSTOMER) refreshCustomerRequests(userId)
            else refreshWorkerRequests(userId)
        }
    }

    // ── JSON Parsing Helpers ──────────────────────────────────────────

    private fun parseUser(json: JSONObject): User {
        return User(
            id = json.optString("id", json.optString("_id")),
            name = json.getString("name"),
            email = json.getString("email"),
            role = if (json.optString("role") == "WORKER") UserRole.WORKER else UserRole.CUSTOMER,
            rating = json.optDouble("rating", 0.0).toFloat()
        )
    }

    private fun parseRequestsArray(array: JSONArray): List<ServiceRequest> {
        val list = mutableListOf<ServiceRequest>()
        for (i in 0 until array.length()) {
            list.add(parseRequest(array.getJSONObject(i)))
        }
        return list
    }

    private fun parseRequest(json: JSONObject): ServiceRequest {
        val subObj = json.getJSONObject("subService")
        val subService = SubService(
            id = subObj.getString("id"),
            categoryId = subObj.getString("categoryId"),
            name = subObj.getString("name"),
            description = subObj.optString("description", ""),
            basePrice = subObj.getInt("basePrice")
        )

        val statusStr = json.optString("status", "PENDING")
        val status = try {
            RequestStatus.valueOf(statusStr)
        } catch (e: Exception) {
            RequestStatus.PENDING
        }

        return ServiceRequest(
            id = json.optString("_id", json.optString("id")),
            customerId = json.optString("customerId", ""),
            customerName = json.optString("customerName", "Customer"),
            workerId = if (json.isNull("workerId")) null else json.optString("workerId"),
            workerName = if (json.isNull("workerName")) null else json.optString("workerName"),
            subService = subService,
            status = status,
            address = json.getString("address"),
            description = json.optString("description", "")
        )
    }

    // ── HTTP Networking Primitives ────────────────────────────────────

    private sealed class AuthHttpResult {
        data class Success(val data: JSONObject) : AuthHttpResult()
        data class AuthError(val message: String) : AuthHttpResult()
        data class ServerUnreachable(val detail: String = "", val cause: Throwable? = null) : AuthHttpResult()
    }

    private fun buildJsonString(vararg pairs: Pair<String, String>): String {
        return pairs.joinToString(separator = ",", prefix = "{", postfix = "}") { (key, value) ->
            val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
            "\"$key\":\"$escaped\""
        }
    }

    private fun executeAuthRequest(urlString: String, payloadJson: String): AuthHttpResult {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payloadJson)
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val body = reader.readText()
                reader.close()
                val json = JSONObject(body)
                if (json.optBoolean("success", false)) {
                    AuthHttpResult.Success(json)
                } else {
                    val msg = json.optString("message", "Authentication failed.")
                    AuthHttpResult.AuthError(msg)
                }
            } else if (responseCode in 400..499) {
                val stream = conn.errorStream ?: conn.inputStream
                val body = stream?.let { BufferedReader(InputStreamReader(it)).readText() } ?: ""
                val msg = try {
                    val json = JSONObject(body)
                    json.optString("message", "Authentication failed. Please check your credentials.")
                } catch (_: Exception) {
                    "Authentication failed. Please check your credentials."
                }
                AuthHttpResult.AuthError(msg)
            } else {
                Log.w(tag, "Auth request returned HTTP $responseCode")
                AuthHttpResult.ServerUnreachable("Server returned HTTP $responseCode")
            }
        } catch (e: Exception) {
            Log.w(tag, "Auth request connection failed: ${e.message}", e)
            AuthHttpResult.ServerUnreachable(e.message ?: "Connection failed", e)
        } finally {
            conn?.disconnect()
        }
    }

    private fun getJson(urlString: String): JSONObject? = runBlocking(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val body = reader.readText()
                reader.close()
                JSONObject(body)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun postJson(urlString: String, payload: JSONObject): JSONObject? = runBlocking(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val body = reader.readText()
                reader.close()
                JSONObject(body)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    private fun putJson(urlString: String, payload: JSONObject): JSONObject? = runBlocking(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 5000
                readTimeout = 5000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payload.toString())
            writer.flush()
            writer.close()

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val body = reader.readText()
                reader.close()
                JSONObject(body)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
