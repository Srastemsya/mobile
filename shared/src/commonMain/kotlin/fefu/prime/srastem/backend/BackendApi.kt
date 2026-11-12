package fefu.prime.srastem.backend

import com.russhwolf.settings.Settings
import fefu.prime.srastem.backendBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.plugins.timeout
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random
import kotlin.time.TimeSource

data class ApiResult(val status: Int, val value: JsonElement?)

class BackendException(
    val status: Int,
    val code: String,
    override val message: String,
) : Exception(message)

/** Shared HTTP boundary for the dev and production API. Tokens stay in platform-backed settings. */
class BackendApi(
    private val settings: Settings,
    private val baseUrl: String = backendBaseUrl(),
    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 35_000
            requestTimeoutMillis = 40_000
        }
    },
) {
    suspend fun get(path: String): JsonElement = send("GET", path).value ?: JsonObject(emptyMap())

    suspend fun getResult(path: String): ApiResult = send("GET", path)

    suspend fun post(path: String, body: JsonObject, idempotencyKey: String = newIdempotencyKey()): ApiResult =
        send("POST", path, body, idempotencyKey)

    suspend fun put(path: String, body: JsonObject, idempotencyKey: String = newIdempotencyKey()): ApiResult =
        send("PUT", path, body, idempotencyKey)

    suspend fun putEmpty(path: String): ApiResult = send("PUT", path)

    suspend fun postEmpty(path: String, idempotencyKey: String = newIdempotencyKey()): ApiResult =
        send("POST", path, null, idempotencyKey)

    suspend fun delete(path: String): ApiResult = send("DELETE", path)

    fun accessToken(): String? = settings.getStringOrNull(KEY_ACCESS_TOKEN)
    fun refreshToken(): String? = settings.getStringOrNull(KEY_REFRESH_TOKEN)
    fun isProfileComplete(): Boolean = settings.getBoolean(KEY_PROFILE_COMPLETE, false)

    fun saveSession(session: JsonObject) {
        session.string("accessToken")?.let { settings.putString(KEY_ACCESS_TOKEN, it) }
        session.string("refreshToken")?.let { settings.putString(KEY_REFRESH_TOKEN, it) }
        session.string("userId")?.let { settings.putString(KEY_USER_ID, it) }
        settings.putBoolean(KEY_PROFILE_COMPLETE, session.boolean("profileComplete") ?: false)
    }

    fun setProfileComplete(value: Boolean) = settings.putBoolean(KEY_PROFILE_COMPLETE, value)

    fun clearSession() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.putBoolean(KEY_PROFILE_COMPLETE, false)
    }

    fun userId(): String = settings.getString(KEY_USER_ID, "")

    fun idempotencyKey(scope: String): String {
        idempotencyStorageKey(scope).let { settings.getStringOrNull(it)?.let { key -> return key } }
        val key = newIdempotencyKey()
        settings.putString(idempotencyStorageKey(scope), key)
        return key
    }

    fun clearIdempotencyKey(scope: String) {
        settings.remove(idempotencyStorageKey(scope))
    }

    private fun idempotencyStorageKey(scope: String): String =
        KEY_IDEMPOTENCY_PREFIX + scope.hashCode() + "_" + scope.reversed().hashCode()

    fun resolveMediaUrl(pathOrUrl: String?): String? = pathOrUrl?.let {
        if (it.startsWith("http://") || it.startsWith("https://")) it else baseUrl.trimEnd('/') + "/" + it.trimStart('/')
    }

    private suspend fun send(
        method: String,
        path: String,
        body: JsonObject? = null,
        idempotencyKey: String? = null,
        allowRefresh: Boolean = true,
    ): ApiResult {
        val requestId = newIdempotencyKey().take(8)
        val url = baseUrl.trimEnd('/') + path
        val started = TimeSource.Monotonic.markNow()
        backendLog("[$requestId] --> $method ${sanitizeUrl(path)} body=${sanitizeJson(body)}")
        val response = try {
            client.request(url) {
                this.method = HttpMethod.parse(method)
                accept(ContentType.Application.Json)
                if (body != null) {
                    contentType(ContentType.Application.Json)
                    setBody(body.toString())
                }
                accessToken()?.let { header("Authorization", "Bearer $it") }
                idempotencyKey?.let { header("Idempotency-Key", it) }
                timeout { requestTimeoutMillis = if (path.contains("afterVersion=")) 40_000 else 15_000 }
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            backendLog("[$requestId] xx $method ${sanitizeUrl(path)} cancelled after ${started.elapsedNow().inWholeMilliseconds}ms")
            throw cancelled
        } catch (error: Exception) {
            backendLog("[$requestId] xx $method ${sanitizeUrl(path)} failed after ${started.elapsedNow().inWholeMilliseconds}ms: ${error::class.simpleName}")
            throw error
        }
        val raw = response.bodyAsText()
        val payload = raw.takeIf(String::isNotBlank)?.let {
            runCatching { Json.parseToJsonElement(it) }.getOrNull()
        }
        backendLog(
            "[$requestId] <-- ${response.status.value} $method ${sanitizeUrl(path)} " +
                "${started.elapsedNow().inWholeMilliseconds}ms body=${sanitizeJson(payload)}",
        )
        if (response.status.value == 401 && allowRefresh && path != "/v1/auth/refresh") {
            val token = refreshToken()
            if (token != null) {
                try {
                    val refreshed = send(
                        "POST", "/v1/auth/refresh",
                        jsonObjectOf("refreshToken" to token), idempotencyKey("refresh"), allowRefresh = false,
                    ).value?.jsonObject ?: error("Сервер не вернул сессию")
                    saveSession(refreshed)
                    clearIdempotencyKey("refresh")
                    return send(method, path, body, idempotencyKey, allowRefresh = false)
                } catch (error: BackendException) {
                    if (error.status == 401) clearSession()
                    throw error
                }
            } else {
                clearSession()
            }
        }
        if (!response.status.isSuccess()) {
            val error = payload?.jsonObject?.get("error")?.jsonObject
            val code = error?.get("code")?.jsonPrimitive?.contentOrNull ?: "SERVER_ERROR"
            val message = error?.get("message")?.jsonPrimitive?.contentOrNull ?: "Ошибка соединения с сервером ($code)"
            throw BackendException(response.status.value, code, message)
        }
        return ApiResult(response.status.value, payload)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "backend_access_token"
        private const val KEY_REFRESH_TOKEN = "backend_refresh_token"
        private const val KEY_USER_ID = "backend_user_id"
        private const val KEY_PROFILE_COMPLETE = "backend_profile_complete"
        private const val KEY_IDEMPOTENCY_PREFIX = "backend_pending_idempotency_"

        fun newIdempotencyKey(): String = buildString(32) {
            val bytes = Random.nextBytes(16)
            bytes.forEach { byte ->
                val value = byte.toInt() and 0xff
                append(HEX[value ushr 4])
                append(HEX[value and 0x0f])
            }
        }

        private const val HEX = "0123456789abcdef"
    }
}

private fun backendLog(message: String) {
    println("SrastemNetwork $message")
}

private fun sanitizeJson(value: JsonElement?): String = when (value) {
    null -> "<empty>"
    JsonNull -> "null"
    is JsonObject -> sanitizeObject(value).toString()
    is JsonArray -> value.joinToString(prefix = "[", postfix = "]") { sanitizeJson(it) }
    is JsonPrimitive -> value.toString()
}

private fun sanitizeObject(value: JsonObject, parentKey: String = ""): JsonObject = buildJsonObject {
    value.forEach { (key, child) ->
        put(
            key,
            if (isSensitiveLogField(key, parentKey)) JsonPrimitive("[REDACTED]")
            else when (child) {
                is JsonObject -> sanitizeObject(child, key)
                is JsonArray -> JsonArray(child.map { sanitizeElement(it, key) })
                else -> child
            },
        )
    }
}

private fun sanitizeElement(value: JsonElement, parentKey: String): JsonElement = when (value) {
    JsonNull -> JsonNull
    is JsonObject -> sanitizeObject(value, parentKey)
    is JsonArray -> JsonArray(value.map { sanitizeElement(it, parentKey) })
    else -> value
}

private fun isSensitiveLogField(key: String, parentKey: String): Boolean {
    val normalized = key.lowercase().filter(Char::isLetterOrDigit)
    if (normalized in setOf("accesstoken", "refreshtoken", "token", "authorization", "idempotencykey", "password", "secret", "phone", "email", "destination", "birthdate", "displayname", "organizername", "participantname", "inviteurl", "invitationurl", "query", "q")) return true
    if (normalized.contains("token") || normalized.contains("password") || normalized.contains("secret")) return true
    return normalized in setOf("code", "otp") && parentKey.lowercase() != "error"
}

private fun sanitizeUrl(path: String): String = path.split("?", limit = 2).let { parts ->
    if (parts.size == 1) path else {
        val query = parts[1].split('&').joinToString("&") { parameter ->
            val pair = parameter.split("=", limit = 2)
            val key = pair[0]
            if (isSensitiveLogField(key, "query")) "$key=[REDACTED]" else parameter
        }
        "${parts[0]}?$query"
    }
}

internal fun jsonObjectOf(vararg values: Pair<String, String?>): JsonObject = JsonObject(
    values.mapNotNull { (key, value) -> value?.let { key to JsonPrimitive(it) } }.toMap()
)

internal fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
internal fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
internal fun JsonObject.boolean(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
