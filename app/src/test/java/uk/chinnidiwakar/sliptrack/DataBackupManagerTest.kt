package uk.chinnidiwakar.sliptrack

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

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
}
