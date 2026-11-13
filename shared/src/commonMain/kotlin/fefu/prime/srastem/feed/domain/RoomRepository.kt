package fefu.prime.srastem.feed.domain

import kotlinx.coroutines.flow.Flow

/** Комната и long polling: контракт Discovery v1, BE-04. */
interface RoomRepository {
    suspend fun createRoom(name: String?): RoomSnapshot
    suspend fun createInvite(roomId: String): RoomInvite
    suspend fun joinRoom(token: String): RoomSnapshot
    suspend fun startRoom(roomId: String, filters: FeedFilters): RoomSnapshot
    suspend fun submitVote(roomId: String, roundId: String, candidate: FeedRef, decision: SwipeDecision): RoomSnapshot
    suspend fun completeRound(roomId: String, roundId: String): RoomSnapshot
    suspend fun getSnapshot(roomId: String): RoomSnapshot
    fun observeRoom(roomId: String): Flow<RoomSnapshot>
}

/** Ссылка-приглашение: многоразовая, живёт 30 минут; по истечении перевыпускается новым POST. */
data class RoomInvite(val url: String, val expiresAt: String?)
