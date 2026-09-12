package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ClassificationResult(
    val serviceCategory: String,
    val requiredSkill: String,
    val urgency: String, // "NORMAL", "HIGH", "EMERGENCY"
    val confidence: Float,
    val explanation: String
)

object AiServiceClassifier {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun classifyProblem(userInput: String): ClassificationResult = withContext(Dispatchers.IO) {
        val trimmed = userInput.trim()
        if (trimmed.isEmpty()) {
            return@withContext ClassificationResult(
                serviceCategory = "General Labour",
                requiredSkill = "General Labourer",
                urgency = "NORMAL",
                confidence = 0.5f,
                explanation = "Please describe the problem to identify the exact trade."
            )
        }

        // Try Gemini API if key is valid, else fallback seamlessly to NLP rule engine
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiClassification(trimmed, apiKey)
                if (geminiResult != null) return@withContext geminiResult
            } catch (_: Exception) {
                // Fall through to local NLP
            }
        }

        // High precision rule-based NLP Classifier (Hindi, Hinglish, English)
        return@withContext classifyLocally(trimmed)
    }

    private fun callGeminiClassification(prompt: String, apiKey: String): ClassificationResult? {
        val jsonPayload = JSONObject().apply {
            val contentsArr = org.json.JSONArray().apply {
                val partObj = JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", """
                                You are an AI trade classifier for Indian labour cooperatives (Sahakaar Setu).
                                Analyze this customer problem: "$prompt".
                                Respond ONLY in raw JSON with keys:
                                "serviceCategory": one of ["Plumbing", "Electrical", "Carpentry", "Painting", "Masonry", "Welding", "AC Repair", "Appliance Repair", "Cleaning", "Solar Installation", "Gardening", "General Labour"],
                                "requiredSkill": specific trade title,
                                "urgency": one of ["NORMAL", "HIGH", "EMERGENCY"],
                                "explanation": short 1 sentence reason.
                            """.trimIndent())
                        })
                    })
                }
                put(partObj)
            }
            put("contents", contentsArr)
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val bodyString = response.body?.string() ?: return null
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() > 0) {
                val text = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                val parsed = JSONObject("{$cleanJson}")
                return ClassificationResult(
                    serviceCategory = parsed.optString("serviceCategory", "General Labour"),
                    requiredSkill = parsed.optString("requiredSkill", "Artisan"),
                    urgency = parsed.optString("urgency", "HIGH"),
                    confidence = 0.96f,
                    explanation = parsed.optString("explanation", "Recognized via Gemini Intelligence")
                )
            }
        }
        return null
    }

    private fun classifyLocally(text: String): ClassificationResult {
        val lower = text.lowercase()

        // Plumbing
        if (containsAny(lower, "pipe", "leak", "paani", "pani", "nal", "tap", "drain", "sewer", "tank", "motor", "geyser leak", "flush", "plumb", "basin", "siphon", "valve")) {
            val isUrgent = containsAny(lower, "burst", "bahar", "bahaar", "heavy", "flood", "faela", "toot", "urgent", "turant")
            return ClassificationResult(
                serviceCategory = "Plumbing",
                requiredSkill = "Plumber (नलसाज)",
                urgency = if (isUrgent) "EMERGENCY" else "HIGH",
                confidence = 0.95f,
                explanation = "Identified plumbing water supply or drainage breakdown."
            )
        }

        // Electrical
        if (containsAny(lower, "spark", "current", "bijli", "shock", "mcb", "trip", "wire", "wiring", "short circuit", "fan", "switch", "inverter", "light", "board", "fuse", "blinking", "socket")) {
            val isUrgent = containsAny(lower, "spark", "smoke", "shock", "dhuan", "aag", "fire", "danger", "turant")
            return ClassificationResult(
                serviceCategory = "Electrical",
                requiredSkill = "Licensed Electrician (बिजली मिस्त्री)",
                urgency = if (isUrgent) "EMERGENCY" else "HIGH",
                confidence = 0.94f,
                explanation = "Identified circuit interruption or electrical power failure."
            )
        }

        // AC & Appliance
        if (containsAny(lower, "ac", "air conditioner", "cooling", "gas charge", "compressor", "chilling", "coil")) {
            return ClassificationResult(
                serviceCategory = "AC Repair",
                requiredSkill = "HVAC & AC Technician",
                urgency = "HIGH",
                confidence = 0.93f,
                explanation = "Identified refrigeration cooling or compressor defect."
            )
        }
        if (containsAny(lower, "fridge", "refrigerator", "washing machine", "microwave", "mixer", "cooler", "oven", "machine")) {
            return ClassificationResult(
                serviceCategory = "Appliance Repair",
                requiredSkill = "Home Appliance Technician",
                urgency = "NORMAL",
                confidence = 0.92f,
                explanation = "Identified domestic electronic appliance fault."
            )
        }

        // Carpentry
        if (containsAny(lower, "wood", "darwaza", "door", "window", "furniture", "table", "chair", "bed", "lock", "handle", "cabinet", "almari", "plywood", "badhai", "carpenter")) {
            return ClassificationResult(
                serviceCategory = "Carpentry",
                requiredSkill = "Master Carpenter (बढ़ई)",
                urgency = "NORMAL",
                confidence = 0.92f,
                explanation = "Identified woodwork, latch, or furniture crafting requirement."
            )
        }

        // Masonry
        if (containsAny(lower, "cement", "tile", "plaster", "deewar", "wall", "crack", "darar", "chhat", "roof", "marble", "brick", "it", "mitti", "rajmistri", "mason")) {
            return ClassificationResult(
                serviceCategory = "Masonry",
                requiredSkill = "Rajmistri / Civil Mason",
                urgency = "NORMAL",
                confidence = 0.90f,
                explanation = "Identified structural masonry, tiling or civil repair."
            )
        }

        // Painting
        if (containsAny(lower, "paint", "putty", "distemper", "color", "rang", " सफेदी", "primer", "waterproof", "texture", "painting")) {
            return ClassificationResult(
                serviceCategory = "Painting",
                requiredSkill = "Professional Painter",
                urgency = "NORMAL",
                confidence = 0.92f,
                explanation = "Identified surface coating and decorative painting."
            )
        }

        // Default
        return ClassificationResult(
            serviceCategory = "General Labour",
            requiredSkill = "Multi-skilled Worker",
            urgency = "NORMAL",
            confidence = 0.75f,
            explanation = "General trade assignment based on description."
        )
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        for (kw in keywords) {
            if (text.contains(kw)) return true
        }
        return false
    }
}
