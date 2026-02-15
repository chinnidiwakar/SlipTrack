package uk.chinnidiwakar.sliptrack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.chinnidiwakar.sliptrack.utils.normalizeTimestamp

class DataBackupManagerTest {

    @Test
    fun parseJson_treatsJsonNullStringsAsNull() {
        val json = """
            {
              "events": [
                {
                  "id": 1,
                  "timestamp": 1730000000000,
                  "isResist": false,
                  "intensity": 0,
                  "note": null,
                  "trigger": null
                }
              ]
            }
        """.trimIndent()

        val parsed = DataBackupManager.parseJson(json)

        assertEquals(1, parsed.size)
        assertNull(parsed.first().note)
        assertNull(parsed.first().trigger)
    }

    @Test
    fun parseJson_supportsRawArrayAndSanitizesFields() {
        val rawSeconds = 1_730_000_000L
        val json = """
            [
              {
                "id": 9,
                "timestamp": $rawSeconds,
                "isResist": true,
                "intensity": 7,
                "note": "  Journal  ",
                "trigger": "  Stress "
              },
              {
                "id": 10,
                "timestamp": 0
              }
            ]
        """.trimIndent()

        val parsed = DataBackupManager.parseJson(json)

        assertEquals(1, parsed.size)
        val event = parsed.first()
        assertEquals(9, event.id)
        assertEquals(normalizeTimestamp(rawSeconds), event.timestamp)
        assertEquals(3, event.intensity)
        assertEquals("Journal", event.note)
        assertEquals("Stress", event.trigger)
    }
}
