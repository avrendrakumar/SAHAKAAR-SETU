package com.example.engine

data class AiClassificationResult(
    val serviceName: String,
    val requiredSkill: String,
    val urgency: String, // "Normal", "High", "Emergency 🚨"
    val isEmergency: Boolean,
    val estimatedBaseCost: Double,
    val explanation: String
)

object AiServiceClassifier {

    fun classifyService(inputText: String): AiClassificationResult {
        val text = inputText.lowercase().trim()

        val isEmergency = text.contains("emergency") || text.contains("urgent") ||
                text.contains("turant") || text.contains("jaldi") || text.contains("leak") ||
                text.contains("burst") || text.contains("spark") || text.contains("flooding") ||
                text.contains("current lag") || text.contains("chulha")

        val urgency = when {
            text.contains("burst") || text.contains("flooding") || text.contains("spark") || text.contains("emergency") -> "Emergency 🚨"
            isEmergency -> "High"
            else -> "Normal"
        }

        return when {
            // Plumbing
            containsAny(text, "pipe", "leak", "nal", "tap", "bathroom", "water", "tank", "sewage", "basin", "tonti", "paani", "drain", "plumb") -> {
                AiClassificationResult(
                    serviceName = "Plumbing",
                    requiredSkill = "Plumbing",
                    urgency = urgency,
                    isEmergency = isEmergency,
                    estimatedBaseCost = 350.0,
                    explanation = "Identified plumbing & sanitation issue. Cooperative verified plumbers matched."
                )
            }
            // Electrical
            containsAny(text, "spark", "current", "switch", "mcb", "light", "wire", "bijli", "fan", "inverter", "short circuit", "board", "bulb", "electric") -> {
                AiClassificationResult(
                    serviceName = "Electrical",
                    requiredSkill = "Electrical",
                    urgency = if (isEmergency) "Emergency 🚨" else "High",
                    isEmergency = isEmergency || text.contains("spark"),
                    estimatedBaseCost = 300.0,
                    explanation = "Identified electrical wiring or appliance circuit issue. Safety certified technician required."
                )
            }
            // AC Repair
            containsAny(text, "ac", "cooling", "air conditioner", "gas refill", "compressor", "aircon", "thanda nahi") -> {
                AiClassificationResult(
                    serviceName = "AC Repair",
                    requiredSkill = "AC Repair",
                    urgency = urgency,
                    isEmergency = false,
                    estimatedBaseCost = 550.0,
                    explanation = "Identified HVAC/AC cooling and refrigerant issue. HVAC specialist required."
                )
            }
            // Carpentry
            containsAny(text, "door", "window", "furniture", "wood", "lock", "table", "chair", "cabinet", "almari", "darwaza", "hinge", "badhai") -> {
                AiClassificationResult(
                    serviceName = "Carpentry",
                    requiredSkill = "Carpentry",
                    urgency = "Normal",
                    isEmergency = false,
                    estimatedBaseCost = 400.0,
                    explanation = "Identified woodwork or fixture repair requirement."
                )
            }
            // Appliance Repair
            containsAny(text, "washing machine", "fridge", "refrigerator", "microwave", "purifier", "ro filter", "geyser", "appliance") -> {
                AiClassificationResult(
                    serviceName = "Appliance Repair",
                    requiredSkill = "Appliance Repair",
                    urgency = urgency,
                    isEmergency = false,
                    estimatedBaseCost = 350.0,
                    explanation = "Identified domestic electronic appliance fault."
                )
            }
            // Painting
            containsAny(text, "paint", "putty", "wall", "color", "primer", "moisture", "peeling", "diwal", "rangai", "distemper") -> {
                AiClassificationResult(
                    serviceName = "Painting",
                    requiredSkill = "Painting",
                    urgency = "Normal",
                    isEmergency = false,
                    estimatedBaseCost = 450.0,
                    explanation = "Identified wall treatment, texture or painting requirement."
                )
            }
            // Masonry
            containsAny(text, "brick", "cement", "tile", "floor", "plaster", "concrete", "rajmistri", "deewar", "seepage", "crack") -> {
                AiClassificationResult(
                    serviceName = "Masonry",
                    requiredSkill = "Masonry",
                    urgency = "Normal",
                    isEmergency = false,
                    estimatedBaseCost = 500.0,
                    explanation = "Identified structural masonry or tile laying work."
                )
            }
            // Welding
            containsAny(text, "weld", "iron", "gate", "grill", "railing", "metal", "loha") -> {
                AiClassificationResult(
                    serviceName = "Welding",
                    requiredSkill = "Welding",
                    urgency = urgency,
                    isEmergency = false,
                    estimatedBaseCost = 450.0,
                    explanation = "Identified metal fabrication or welding repair."
                )
            }
            // Cleaning
            containsAny(text, "clean", "safai", "dust", "deep clean", "tank clean", "swachh") -> {
                AiClassificationResult(
                    serviceName = "Cleaning",
                    requiredSkill = "Cleaning",
                    urgency = "Normal",
                    isEmergency = false,
                    estimatedBaseCost = 400.0,
                    explanation = "Identified residential or commercial deep cleaning."
                )
            }
            // Solar
            containsAny(text, "solar", "panel", "rooftop", "inverter battery") -> {
                AiClassificationResult(
                    serviceName = "Solar Installation",
                    requiredSkill = "Solar Installation",
                    urgency = "Normal",
                    isEmergency = false,
                    estimatedBaseCost = 650.0,
                    explanation = "Identified solar PV installation or repair requirement."
                )
            }
            else -> {
                AiClassificationResult(
                    serviceName = "General Labour",
                    requiredSkill = "General Labour",
                    urgency = if (isEmergency) "High" else "Normal",
                    isEmergency = isEmergency,
                    estimatedBaseCost = 300.0,
                    explanation = "General labour or multi-trade assistance."
                )
            }
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean {
        return keywords.any { text.contains(it) }
    }
}
