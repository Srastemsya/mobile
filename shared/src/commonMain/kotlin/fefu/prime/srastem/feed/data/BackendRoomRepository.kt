package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.backend.jsonObjectOf
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedRef
import fefu.prime.srastem.feed.domain.RoomInvite
import fefu.prime.srastem.feed.domain.RoomRepository
import fefu.prime.srastem.feed.domain.RoomSnapshot
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Комната и long polling через общий `BackendApi` (контракт Discovery v1, BE-04).
 * Один открытый `GET /rooms/{id}?afterVersion=&waitSeconds=25`; `204` — немедленный повтор,
 * сетевая ошибка — повтор с задержкой, snapshot со старой версией игнорируется.
 */
class BackendRoomRepository(private val api: BackendApi) : RoomRepository {

    override suspend fun createRoom(name: String?): RoomSnapshot = guard {
        val scope = "create-room:${name.orEmpty().trim()}"
        val created = api.post(
            "/v1/rooms",
            jsonObjectOf("name" to name?.trim()?.takeIf(String::isNotBlank)),
            api.idempotencyKey(scope),
        ).value?.jsonObject ?: error("Сервер не вернул комнату")
        api.clearIdempotencyKey(scope)
        FeedMappers.room(created, api.userId())
    }

    override suspend fun createInvite(roomId: String): RoomInvite = guard {
        val scope = "invite-room:$roomId"
        val invite =
            api.postEmpty("/v1/rooms/$roomId/invites", api.idempotencyKey(scope)).value?.jsonObject
                ?: error("Сервер не вернул приглашение")
        api.clearIdempotencyKey(scope)
        RoomInvite(
            url = invite["url"]?.jsonPrimitive?.content
                ?: error("Сервер не вернул ссылку приглашения"),
            expiresAt = invite["expiresAt"]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content,
        )
    }

    override suspend fun joinRoom(token: String): RoomSnapshot = guard {
        val scope = "join-room:${token.hashCode()}"
        val joined = api.post(
            "/v1/room-invites/join",
            jsonObjectOf("token" to token),
            api.idempotencyKey(scope),
        ).value?.jsonObject ?: error("Сервер не вернул комнату")
        api.clearIdempotencyKey(scope)
        FeedMappers.room(joined, api.userId())
    }

    override suspend fun startRoom(roomId: String, filters: FeedFilters): RoomSnapshot = guard {
        val scope = "start-room:$roomId"
        val started = api.post(
            "/v1/rooms/$roomId/start",
            FeedContract.startBody(filters),
            api.idempotencyKey(scope)
        ).value?.jsonObject
            ?: error("Сервер не вернул комнату")
        api.clearIdempotencyKey(scope)
        FeedMappers.room(started, api.userId())
    }

    override suspend fun submitVote(
        roomId: String,
        roundId: String,
        candidate: FeedRef,
        decision: SwipeDecision,
    ): RoomSnapshot = guard {
        val kind = candidate.kind.name
        val updated = api.put(
            "/v1/rooms/$roomId/rounds/$roundId/votes/$kind/${candidate.id}",
            jsonObjectOf("choice" to if (decision == SwipeDecision.LIKE) "LIKE" else "PASS"),
        ).value?.jsonObject ?: error("Сервер не вернул комнату")
        FeedMappers.room(updated, api.userId())
    }

    override suspend fun completeRound(roomId: String, roundId: String): RoomSnapshot = guard {
        val updated = api.putEmpty("/v1/rooms/$roomId/rounds/$roundId/completion").value?.jsonObject
            ?: error("Сервер не вернул комнату")
        FeedMappers.room(updated, api.userId())
    }

    override suspend fun getSnapshot(roomId: String): RoomSnapshot = guard {
        FeedMappers.room(api.get("/v1/rooms/$roomId").jsonObject, api.userId())
    }

    override fun observeRoom(roomId: String): Flow<RoomSnapshot> = flow {
        var room = getSnapshot(roomId)
        emit(room)
        while (isPolling(room)) {
            try {
                val result =
                    api.getResult("/v1/rooms/$roomId?afterVersion=${room.version}&waitSeconds=25")
                if (result.status == 204) continue
                val next =
                    result.value?.jsonObject?.let { FeedMappers.room(it, api.userId()) } ?: continue
                if (next.version >= room.version) {
                    room = next
                    emit(room)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                delay(POLL_RETRY_DELAY_MS)
            }
        }
    }

    private fun isPolling(room: RoomSnapshot): Boolean =
        room.phase != fefu.prime.srastem.feed.domain.RoomPhase.MATCHED &&
                room.phase != fefu.prime.srastem.feed.domain.RoomPhase.EXHAUSTED &&
                room.phase != fefu.prime.srastem.feed.domain.RoomPhase.CLOSED

    private inline fun <T> guard(block: () -> T): T = try {
        block()
    } catch (error: BackendException) {
        throw FeedMappers.errorOf(error)
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        throw FeedMappers.errorOf(error)
    }

    private companion object {
        const val POLL_RETRY_DELAY_MS = 1_500L
    }
}
