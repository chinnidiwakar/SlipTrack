package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

object DataBackupManager {

    fun exportToJson(events: List<SlipEvent>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val payload = JSONArray()
        events.forEach { event ->
            val item = JSONObject()
            item.put("id", event.id)
            item.put("timestamp", event.timestamp)
            item.put("isResist", event.isResist)
            item.put("intensity", event.intensity)
            item.put("note", event.note)
            item.put("trigger", event.trigger)
            payload.put(item)
        }

        root.put("events", payload)
        return root.toString(2)
    }

    fun parseJson(json: String): List<SlipEvent> {
        val trimmed = json.trim()
        val events = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> JSONObject(trimmed).optJSONArray("events") ?: JSONArray()
        }

        return buildList {
            for (i in 0 until events.length()) {
                val obj = events.optJSONObject(i) ?: continue

                val rawTimestamp = obj.optLong("timestamp", 0L)
                if (rawTimestamp <= 0L) continue

                add(
                    SlipEvent(
                        id = obj.optInt("id", 0),
                        timestamp = normalizeTimestamp(rawTimestamp),
                        isResist = obj.optBoolean("isResist", false),
                        intensity = obj.optInt("intensity", 0).coerceIn(0, 3),
                        note = obj.optNullableString("note")?.trim(),
                        trigger = obj.optNullableString("trigger")?.trim()
                    )
                )
            }
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(content)
        } ?: error("Unable to open destination file")
    }

    fun readFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            reader.readText()
        } ?: error("Unable to open source file")
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() && it != "null" }
}
