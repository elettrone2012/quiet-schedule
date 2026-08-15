package io.github.elettrone2012.quietschedule.data.datastore

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistedSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun profileListCanBeSerializedAndDeserialized() {
        val original = listOf(
            PersistedProfile(
                id = "profile-1",
                name = "Work",
                enabled = true,
                dndPolicy = PersistedDndPolicy(),
                schedules = listOf(
                    PersistedSchedule(
                        daysOfWeek = listOf("MONDAY", "TUESDAY"),
                        startTime = "09:00",
                        endTime = "17:00"
                    )
                )
            )
        )

        val serialized = json.encodeToString(original)

        val restored =
            json.decodeFromString<List<PersistedProfile>>(serialized)

        assertEquals(original, restored)
    }

    @Test
    fun unknownJsonFieldsAreIgnored() {
        val serialized = """
            [
              {
                "id": "profile-1",
                "name": "Work",
                "enabled": false,
                "unknownField": "ignored",
                "dndPolicy": {},
                "schedules": []
              }
            ]
        """.trimIndent()

        val restored =
            json.decodeFromString<List<PersistedProfile>>(serialized)

        assertEquals(1, restored.size)
        assertEquals("Work", restored.first().name)
    }

    @Test
    fun malformedJsonIsRejected() {
        val result = runCatching {
            json.decodeFromString<List<PersistedProfile>>(
                """[{ this is not valid json }]"""
            )
        }

        assertTrue(result.isFailure)
    }
}