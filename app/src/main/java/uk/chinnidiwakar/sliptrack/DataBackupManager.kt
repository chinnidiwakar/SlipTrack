package uk.chinnidiwakar.sliptrack

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

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
        val root = JSONObject(json)
        val events = root.optJSONArray("events") ?: JSONArray()

        return buildList {
            for (i in 0 until events.length()) {
                val obj = events.optJSONObject(i) ?: continue
                add(
                    SlipEvent(
                        id = obj.optInt("id", 0),
                        timestamp = obj.optLong("timestamp"),
                        isResist = obj.optBoolean("isResist", false),
                        intensity = obj.optInt("intensity", 0),
                        note = obj.optString("note").takeIf { it.isNotBlank() },
                        trigger = obj.optString("trigger").takeIf { it.isNotBlank() }
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
