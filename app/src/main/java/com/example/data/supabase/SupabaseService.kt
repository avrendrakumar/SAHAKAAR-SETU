package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class SupabaseAuthUser(
    val id: String,
    val email: String?,
    val phone: String?,
    val accessToken: String?,
    val isConfirmed: Boolean
)

data class SupabaseProfile(
    val id: String,
    val email: String,
    val name: String,
    val role: String,
    val phone: String,
    val city: String,
    val state: String = "Bihar",
    val district: String = "Patna",
    val village: String = "",
    val cooperativeId: String? = null,
    val customIdTag: String = "",
    val isMobileVerified: Boolean = true,
    val isActive: Boolean = true,
    val avatarUrl: String? = null,
    val createdAt: String = ""
)

data class SupabaseWorker(
    val id: String,
    val userId: String?,
    val workerIdTag: String,
    val name: String,
    val phone: String,
    val cooperativeId: String?,
    val cooperativeName: String?,
    val primarySkill: String,
    val secondarySkills: String?,
    val experienceYears: Int,
    val verificationStatus: String,
    val certifications: String?,
    val customerRating: Double,
    val ratingCount: Int,
    val completedJobs: Int,
    val reliabilityScore: Int,
    val dailyWageRate: Double,
    val isAvailable: Boolean,
    val isBusy: Boolean,
    val isEmergencyReady: Boolean,
    val lat: Double,
    val lng: Double,
    val city: String,
    val zone: String
)

data class SupabaseBooking(
    val id: String,
    val bookingNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val workerId: String?,
    val workerName: String,
    val cooperativeId: String?,
    val serviceCategory: String,
    val problemDescription: String,
    val urgency: String,
    val isEmergency: Boolean,
    val locationAddress: String,
    val city: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val status: String,
    val laborCost: Double,
    val materialCost: Double,
    val platformFee: Double,
    val totalAmount: Double,
    val paymentStatus: String,
    val paymentMethod: String?
)

class SupabaseException(val statusCode: Int, val errorCode: String?, message: String) : Exception(message)

object SupabaseService {
    private const val TAG = "SupabaseService"

    // Reliable fallback to verified Supabase project credentials
    val supabaseUrl: String
        get() = try {
            val url = BuildConfig.SUPABASE_URL
            if (url.isNullOrBlank() || url.contains("placeholder")) "https://mtfrypjkbmjdbkmnnali.supabase.co" else url.trimEnd('/')
        } catch (_: Exception) {
            "https://mtfrypjkbmjdbkmnnali.supabase.co"
        }

    val supabaseKey: String
        get() = try {
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (key.isNullOrBlank() || key.contains("placeholder")) "sb_publishable_B11k3pPxCu1sMsSwNqCqkg_JNqRbzrU" else key
        } catch (_: Exception) {
            "sb_publishable_B11k3pPxCu1sMsSwNqCqkg_JNqRbzrU"
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    var activeAccessToken: String? = null
    var activeUserId: String? = null

    // ---------------------------------------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------------------------------------

    suspend fun signUp(email: String, password: String, metadata: Map<String, Any> = emptyMap()): SupabaseAuthUser = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/auth/v1/signup"
        val payload = JSONObject().apply {
            put("email", email.trim())
            put("password", password)
            if (metadata.isNotEmpty()) {
                val dataObj = JSONObject()
                metadata.forEach { (k, v) -> dataObj.put(k, v) }
                put("data", dataObj)
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                val errorCode = parseErrorCode(body)
                if (response.code == 429) {
                    Log.w(TAG, "Supabase signUp rate limit reached [429]: $errorMsg")
                } else {
                    Log.e(TAG, "Supabase signUp failed [${response.code}]: $errorMsg")
                }
                throw SupabaseException(response.code, errorCode, errorMsg)
            }

            val json = JSONObject(body)
            val id = json.optString("id")
            val userEmail = json.optString("email", email)
            val phone = json.optString("phone", null)
            val session = json.optJSONObject("session")
            val token = session?.optString("access_token") ?: json.optString("access_token", null)

            activeAccessToken = token
            activeUserId = id

            SupabaseAuthUser(
                id = id,
                email = userEmail,
                phone = phone,
                accessToken = token,
                isConfirmed = json.optString("confirmed_at").isNotBlank()
            )
        }
    }

    suspend fun signInWithPassword(email: String, password: String): SupabaseAuthUser = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/auth/v1/token?grant_type=password"
        val payload = JSONObject().apply {
            put("email", email.trim())
            put("password", password)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                val code = parseErrorCode(body)
                if (response.code == 429 || response.code == 400) {
                    Log.w(TAG, "Supabase signIn notice [${response.code}]: $errorMsg ($code)")
                } else {
                    Log.e(TAG, "Supabase signIn failed [${response.code}]: $errorMsg ($code)")
                }
                throw SupabaseException(response.code, code, errorMsg)
            }

            val json = JSONObject(body)
            val token = json.getString("access_token")
            val userObj = json.getJSONObject("user")
            val id = userObj.getString("id")
            val userEmail = userObj.optString("email", email)
            val phone = userObj.optString("phone", null)

            activeAccessToken = token
            activeUserId = id

            SupabaseAuthUser(
                id = id,
                email = userEmail,
                phone = phone,
                accessToken = token,
                isConfirmed = true
            )
        }
    }

    // ---------------------------------------------------------------------------------------------
    // PROFILES
    // ---------------------------------------------------------------------------------------------

    suspend fun upsertProfile(profile: SupabaseProfile): SupabaseProfile = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/profiles?on_conflict=id"
        val payload = JSONObject().apply {
            put("id", profile.id)
            put("email", profile.email)
            put("name", profile.name)
            put("role", profile.role)
            put("phone", profile.phone)
            put("city", profile.city)
            put("state", profile.state)
            put("district", profile.district)
            put("village", profile.village)
            if (profile.cooperativeId != null) put("cooperative_id", profile.cooperativeId)
            if (profile.customIdTag.isNotBlank()) put("custom_id_tag", profile.customIdTag)
            put("is_mobile_verified", profile.isMobileVerified)
            put("is_active", profile.isActive)
            if (profile.avatarUrl != null) put("avatar_url", profile.avatarUrl)
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
            .post(payload.toString().toRequestBody(JSON))

        client.newCall(reqBuilder.build()).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                Log.e(TAG, "Supabase upsertProfile failed [${response.code}]: $errorMsg")
                throw SupabaseException(response.code, parseErrorCode(body), errorMsg)
            }

            val array = JSONArray(body)
            if (array.length() == 0) {
                return@withContext profile
            }
            parseProfileJson(array.getJSONObject(0))
        }
    }

    suspend fun getProfile(userId: String): SupabaseProfile? = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/profiles?id=eq.$userId&limit=1"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "Supabase getProfile failed [${response.code}]: $body")
                return@withContext null
            }
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext null
            parseProfileJson(array.getJSONObject(0))
        }
    }

    suspend fun getProfileByEmail(email: String): SupabaseProfile? = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/profiles?email=eq.$email&limit=1"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext null
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext null
            parseProfileJson(array.getJSONObject(0))
        }
    }

    suspend fun getProfileByPhone(phone: String): SupabaseProfile? = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/profiles?phone=eq.$phone&limit=1"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext null
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext null
            parseProfileJson(array.getJSONObject(0))
        }
    }

    suspend fun updateProfile(userId: String, updates: Map<String, Any?>): SupabaseProfile = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/profiles?id=eq.$userId"
        val payload = JSONObject()
        updates.forEach { (k, v) -> payload.put(k, v) }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .patch(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                Log.e(TAG, "Supabase updateProfile failed [${response.code}]: $errorMsg")
                throw SupabaseException(response.code, parseErrorCode(body), errorMsg)
            }
            val array = JSONArray(body)
            if (array.length() == 0) throw SupabaseException(404, "NOT_FOUND", "Profile $userId not found in database")
            parseProfileJson(array.getJSONObject(0))
        }
    }

    // ---------------------------------------------------------------------------------------------
    // WORKERS
    // ---------------------------------------------------------------------------------------------

    suspend fun getWorkers(category: String? = null): List<SupabaseWorker> = withContext(Dispatchers.IO) {
        var url = "$supabaseUrl/rest/v1/workers?select=*"
        if (!category.isNullOrBlank() && category != "All" && category != "सभी") {
            url += "&primary_skill=ilike.*$category*"
        }
        url += "&order=customer_rating.desc"

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "Supabase getWorkers failed: $body")
                return@withContext emptyList()
            }
            val array = JSONArray(body)
            val list = mutableListOf<SupabaseWorker>()
            for (i in 0 until array.length()) {
                list.add(parseWorkerJson(array.getJSONObject(i)))
            }
            list
        }
    }

    suspend fun insertWorker(worker: SupabaseWorker): SupabaseWorker = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/workers?on_conflict=id"
        val payload = JSONObject().apply {
            put("id", worker.id)
            if (worker.userId != null) put("user_id", worker.userId)
            put("worker_id_tag", worker.workerIdTag)
            put("name", worker.name)
            put("phone", worker.phone)
            if (worker.cooperativeId != null) put("cooperative_id", worker.cooperativeId)
            if (worker.cooperativeName != null) put("cooperative_name", worker.cooperativeName)
            put("primary_skill", worker.primarySkill)
            if (worker.secondarySkills != null) put("secondary_skills", worker.secondarySkills)
            put("experience_years", worker.experienceYears)
            put("verification_status", worker.verificationStatus)
            if (worker.certifications != null) put("certifications", worker.certifications)
            put("customer_rating", worker.customerRating)
            put("rating_count", worker.ratingCount)
            put("completed_jobs", worker.completedJobs)
            put("reliability_score", worker.reliabilityScore)
            put("daily_wage_rate", worker.dailyWageRate)
            put("is_available", worker.isAvailable)
            put("is_busy", worker.isBusy)
            put("is_emergency_ready", worker.isEmergencyReady)
            put("lat", worker.lat)
            put("lng", worker.lng)
            put("city", worker.city)
            put("zone", worker.zone)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                Log.e(TAG, "Supabase insertWorker failed [${response.code}]: $errorMsg")
                throw SupabaseException(response.code, parseErrorCode(body), errorMsg)
            }
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext worker
            parseWorkerJson(array.getJSONObject(0))
        }
    }

    // ---------------------------------------------------------------------------------------------
    // BOOKINGS
    // ---------------------------------------------------------------------------------------------

    suspend fun createBooking(booking: SupabaseBooking): SupabaseBooking = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings"
        val payload = JSONObject().apply {
            put("id", booking.id)
            put("booking_number", booking.bookingNumber)
            put("customer_id", booking.customerId)
            put("customer_name", booking.customerName)
            put("customer_phone", booking.customerPhone)
            if (booking.workerId != null) put("worker_id", booking.workerId)
            put("worker_name", booking.workerName)
            if (booking.cooperativeId != null) put("cooperative_id", booking.cooperativeId)
            put("service_category", booking.serviceCategory)
            put("problem_description", booking.problemDescription)
            put("urgency", booking.urgency)
            put("is_emergency", booking.isEmergency)
            put("location_address", booking.locationAddress)
            put("city", booking.city)
            put("scheduled_date", booking.scheduledDate)
            put("scheduled_time", booking.scheduledTime)
            put("status", booking.status)
            put("labor_cost", booking.laborCost)
            put("material_cost", booking.materialCost)
            put("platform_fee", booking.platformFee)
            put("total_amount", booking.totalAmount)
            put("payment_status", booking.paymentStatus)
            if (booking.paymentMethod != null) put("payment_method", booking.paymentMethod)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .post(payload.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(body, response.code)
                Log.e(TAG, "Supabase createBooking failed [${response.code}]: $errorMsg")
                throw SupabaseException(response.code, parseErrorCode(body), errorMsg)
            }
            val array = JSONArray(body)
            if (array.length() == 0) return@withContext booking
            parseBookingJson(array.getJSONObject(0))
        }
    }

    suspend fun getBookingsForCustomer(customerId: String): List<SupabaseBooking> = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings?customer_id=eq.$customerId&order=created_at.desc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<SupabaseBooking>()
            for (i in 0 until array.length()) {
                list.add(parseBookingJson(array.getJSONObject(i)))
            }
            list
        }
    }

    suspend fun getBookingsForWorker(workerId: String): List<SupabaseBooking> = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings?worker_id=eq.$workerId&order=created_at.desc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<SupabaseBooking>()
            for (i in 0 until array.length()) {
                list.add(parseBookingJson(array.getJSONObject(i)))
            }
            list
        }
    }

    suspend fun getAllBookings(): List<SupabaseBooking> = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings?order=created_at.desc"
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) return@withContext emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<SupabaseBooking>()
            for (i in 0 until array.length()) {
                list.add(parseBookingJson(array.getJSONObject(i)))
            }
            list
        }
    }

    suspend fun updateBookingStatus(
        bookingId: String,
        status: String,
        workerId: String? = null,
        workerName: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings?id=eq.$bookingId"
        val payload = JSONObject().apply {
            put("status", status)
            if (workerId != null) put("worker_id", workerId)
            if (workerName != null) put("worker_name", workerName)
            if (status == "COMPLETED") {
                put("completed_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date()))
            }
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .patch(payload.toString().toRequestBody(JSON))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update booking status in Supabase: ${e.message}")
            false
        }
    }

    suspend fun updateBookingPayment(bookingId: String, paymentStatus: String, method: String): Boolean = withContext(Dispatchers.IO) {
        val url = "$supabaseUrl/rest/v1/bookings?id=eq.$bookingId"
        val payload = JSONObject().apply {
            put("payment_status", paymentStatus)
            put("payment_method", method)
        }
        val request = Request.Builder()
            .url(url)
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer ${activeAccessToken ?: supabaseKey}")
            .addHeader("Content-Type", "application/json")
            .patch(payload.toString().toRequestBody(JSON))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update booking payment in Supabase: ${e.message}")
            false
        }
    }

    // ---------------------------------------------------------------------------------------------
    // HELPERS & JSON PARSERS
    // ---------------------------------------------------------------------------------------------

    private fun parseProfileJson(obj: JSONObject): SupabaseProfile {
        return SupabaseProfile(
            id = obj.getString("id"),
            email = obj.optString("email", ""),
            name = obj.optString("name", "User"),
            role = obj.optString("role", "CUSTOMER"),
            phone = obj.optString("phone", ""),
            city = obj.optString("city", "Patna"),
            state = obj.optString("state", "Bihar"),
            district = obj.optString("district", "Patna"),
            village = obj.optString("village", ""),
            cooperativeId = obj.optString("cooperative_id").takeIf { it.isNotBlank() },
            customIdTag = obj.optString("custom_id_tag", ""),
            isMobileVerified = obj.optBoolean("is_mobile_verified", true),
            isActive = obj.optBoolean("is_active", true),
            avatarUrl = obj.optString("avatar_url").takeIf { it.isNotBlank() },
            createdAt = obj.optString("created_at", "")
        )
    }

    private fun parseWorkerJson(obj: JSONObject): SupabaseWorker {
        return SupabaseWorker(
            id = obj.getString("id"),
            userId = obj.optString("user_id").takeIf { it.isNotBlank() },
            workerIdTag = obj.optString("worker_id_tag", "SS-WRK-001"),
            name = obj.getString("name"),
            phone = obj.getString("phone"),
            cooperativeId = obj.optString("cooperative_id").takeIf { it.isNotBlank() },
            cooperativeName = obj.optString("cooperative_name", "Patna Shramik Vikas Sahakari Samiti"),
            primarySkill = obj.getString("primary_skill"),
            secondarySkills = obj.optString("secondary_skills").takeIf { it.isNotBlank() },
            experienceYears = obj.optInt("experience_years", 3),
            verificationStatus = obj.optString("verification_status", "VERIFIED"),
            certifications = obj.optString("certifications").takeIf { it.isNotBlank() },
            customerRating = obj.optDouble("customer_rating", 4.8),
            ratingCount = obj.optInt("rating_count", 25),
            completedJobs = obj.optInt("completed_jobs", 25),
            reliabilityScore = obj.optInt("reliability_score", 95),
            dailyWageRate = obj.optDouble("daily_wage_rate", 450.0),
            isAvailable = obj.optBoolean("is_available", true),
            isBusy = obj.optBoolean("is_busy", false),
            isEmergencyReady = obj.optBoolean("is_emergency_ready", true),
            lat = obj.optDouble("lat", 25.5941),
            lng = obj.optDouble("lng", 85.1376),
            city = obj.optString("city", "Patna"),
            zone = obj.optString("zone", "Zone A")
        )
    }

    private fun parseBookingJson(obj: JSONObject): SupabaseBooking {
        return SupabaseBooking(
            id = obj.getString("id"),
            bookingNumber = obj.getString("booking_number"),
            customerId = obj.getString("customer_id"),
            customerName = obj.getString("customer_name"),
            customerPhone = obj.getString("customer_phone"),
            workerId = obj.optString("worker_id").takeIf { it.isNotBlank() },
            workerName = obj.getString("worker_name"),
            cooperativeId = obj.optString("cooperative_id").takeIf { it.isNotBlank() },
            serviceCategory = obj.getString("service_category"),
            problemDescription = obj.getString("problem_description"),
            urgency = obj.optString("urgency", "NORMAL"),
            isEmergency = obj.optBoolean("is_emergency", false),
            locationAddress = obj.getString("location_address"),
            city = obj.optString("city", "Patna"),
            scheduledDate = obj.getString("scheduled_date"),
            scheduledTime = obj.getString("scheduled_time"),
            status = obj.optString("status", "CREATED"),
            laborCost = obj.optDouble("labor_cost", 450.0),
            materialCost = obj.optDouble("material_cost", 0.0),
            platformFee = obj.optDouble("platform_fee", 0.0),
            totalAmount = obj.optDouble("total_amount", 450.0),
            paymentStatus = obj.optString("payment_status", "PENDING"),
            paymentMethod = obj.optString("payment_method").takeIf { it.isNotBlank() }
        )
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        return try {
            val json = JSONObject(body)
            val msg = json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("error_description") }
                .ifBlank { json.optString("hint") }
            if (msg.isNotBlank()) msg else "HTTP $code: $body"
        } catch (_: Exception) {
            "HTTP $code: $body"
        }
    }

    private fun parseErrorCode(body: String): String? {
        return try {
            val json = JSONObject(body)
            json.optString("code").ifBlank { json.optString("error_code") }.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
