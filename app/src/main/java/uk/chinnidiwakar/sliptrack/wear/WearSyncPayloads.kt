package uk.chinnidiwakar.sliptrack.wear

import org.json.JSONObject
import uk.chinnidiwakar.sliptrack.SlipEvent

data class WearLogEventPayload(
    val timestamp: Long,
    val isResist: Boolean,
    val intensity: Int,
    val trigger: String?,
    val note: String?
) {
    fun toSlipEvent(): SlipEvent = SlipEvent(
        timestamp = timestamp,
        isResist = isResist,
        intensity = intensity,
        trigger = trigger,
        note = note
    )

    companion object {
        fun fromBytes(data: ByteArray): WearLogEventPayload {
            val json = JSONObject(String(data, Charsets.UTF_8))
            return WearLogEventPayload(
                timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                isResist = json.optBoolean("isResist", false),
                intensity = json.optInt("intensity", 0),
                trigger = json.optString("trigger").takeIf { it.isNotBlank() },
                note = json.optString("note").takeIf { it.isNotBlank() }
            )
        }
    }
}

data class WearStatePayload(
    val currentStreak: Int,
    val longestStreak: Int,
    val shieldCharges: Int,
    val updatedAt: Long
) {
    fun toBytes(): ByteArray {
        val json = JSONObject()
            .put("currentStreak", currentStreak)
            .put("longestStreak", longestStreak)
            .put("shieldCharges", shieldCharges)
            .put("updatedAt", updatedAt)
        return json.toString().toByteArray(Charsets.UTF_8)
    }
}
