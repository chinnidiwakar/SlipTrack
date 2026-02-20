package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp
import java.security.MessageDigest

object DataBackupManager {

    fun exportToJson(events: List<SlipEvent>): String {
        val payload = JSONArray()
        events.sortedBy { it.timestamp }.forEach { event ->
            val item = JSONObject()
            item.put("id", event.id)
            item.put("timestamp", normalizeTimestamp(event.timestamp))
            item.put("isResist", event.isResist)
            item.put("intensity", event.intensity.coerceIn(0, 3))
            item.put("note", event.note?.take(2_000))
            item.put("trigger", event.trigger?.take(120))
            payload.put(item)
        }

        val root = JSONObject()
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("checksum", sha256(payload.toString()))
        root.put("events", payload)
        return root.toString(2)
    }

    fun parseJson(json: String): List<SlipEvent> {
        val trimmed = json.trim()
        val events = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            else -> {
                val root = JSONObject(trimmed)
                val payload = root.optJSONArray("events") ?: JSONArray()
                val checksum = root.optString("checksum", "")
                if (checksum.isNotBlank()) {
                    val calculated = sha256(payload.toString())
                    require(calculated == checksum) { "Backup integrity check failed. File may be corrupted." }
                }
                payload
            }
        }

        val parsed = buildList {
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
                        note = obj.optNullableString("note")?.take(2_000),
                        trigger = obj.optNullableString("trigger")?.take(120)
                    )
                )
            }
        }

        return parsed
            .distinctBy { "${it.timestamp}|${it.isResist}|${it.intensity}|${it.note}|${it.trigger}" }
            .sortedBy { it.timestamp }
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

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key).takeIf { it.isNotBlank() && it != "null" }
}
