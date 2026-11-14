package fefu.prime.srastem.feed.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.backend.BackendApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/** A failed POST retried with the same logical action must carry its original key. */
class RoomIdempotencyTest {
    @Test fun createAndInviteRetriesReuseTheirKeys() = runTest {
        val keys = mutableMapOf<String, MutableList<String>>()
        val client = HttpClient(MockEngine { request ->
            val path = request.url.encodedPath
            val used = keys.getOrPut(path) { mutableListOf() }
            used += request.headers["Idempotency-Key"].orEmpty()
            if (used.size == 1) respond("""{"error":{"code":"SERVER_ERROR","message":"retry"}}""", HttpStatusCode.InternalServerError, headersOf(HttpHeaders.ContentType, "application/json"))
            else if (path.endsWith("/invites")) respond("""{"url":"srastemsya://room-invites/join?token=abc","expiresAt":null}""", HttpStatusCode.Created, headersOf(HttpHeaders.ContentType, "application/json"))
            else respond("""{"id":"room-1","name":"Friday","ownerId":"owner","state":"LOBBY","version":1,"members":[{"userId":"owner","displayName":"Owner"}],"filters":null,"round":null,"result":null}""", HttpStatusCode.Created, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        try {
            val api = BackendApi(MemorySettings(), "https://example.test", client)
            api.saveSession(kotlinx.serialization.json.buildJsonObject { put("userId", kotlinx.serialization.json.JsonPrimitive("owner")) })
            val rooms = BackendRoomRepository(api)
            runCatching { rooms.createRoom("Friday") }
            rooms.createRoom("Friday")
            runCatching { rooms.createInvite("room-1") }
            rooms.createInvite("room-1")
            assertEquals(2, keys["/v1/rooms"]?.size)
            assertEquals(keys["/v1/rooms"]?.first(), keys["/v1/rooms"]?.last())
            assertEquals(2, keys["/v1/rooms/room-1/invites"]?.size)
            assertEquals(keys["/v1/rooms/room-1/invites"]?.first(), keys["/v1/rooms/room-1/invites"]?.last())
            assertNotEquals(keys["/v1/rooms"]?.first(), keys["/v1/rooms/room-1/invites"]?.first())
        } finally { client.close() }
    }
}

private class MemorySettings : Settings {
    private val values = mutableMapOf<String, Any>()
    override val keys: Set<String> get() = values.keys
    override val size: Int get() = values.size
    override fun clear() = values.clear()
    override fun remove(key: String) { values.remove(key) }
    override fun hasKey(key: String) = key in values
    override fun putInt(key: String, value: Int) { values[key] = value }
    override fun getInt(key: String, defaultValue: Int) = values[key] as? Int ?: defaultValue
    override fun getIntOrNull(key: String) = values[key] as? Int
    override fun putLong(key: String, value: Long) { values[key] = value }
    override fun getLong(key: String, defaultValue: Long) = values[key] as? Long ?: defaultValue
    override fun getLongOrNull(key: String) = values[key] as? Long
    override fun putString(key: String, value: String) { values[key] = value }
    override fun getString(key: String, defaultValue: String) = values[key] as? String ?: defaultValue
    override fun getStringOrNull(key: String) = values[key] as? String
    override fun putFloat(key: String, value: Float) { values[key] = value }
    override fun getFloat(key: String, defaultValue: Float) = values[key] as? Float ?: defaultValue
    override fun getFloatOrNull(key: String) = values[key] as? Float
    override fun putDouble(key: String, value: Double) { values[key] = value }
    override fun getDouble(key: String, defaultValue: Double) = values[key] as? Double ?: defaultValue
    override fun getDoubleOrNull(key: String) = values[key] as? Double
    override fun putBoolean(key: String, value: Boolean) { values[key] = value }
    override fun getBoolean(key: String, defaultValue: Boolean) = values[key] as? Boolean ?: defaultValue
    override fun getBooleanOrNull(key: String) = values[key] as? Boolean
}
