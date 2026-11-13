package fefu.prime.srastem.feed.domain

/** Вступление по токену приглашения (`POST /room-invites/join`); участник получает snapshot. */
class JoinRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(token: String): Result<RoomSnapshot> =
        runCatching { repository.joinRoom(token) }
}
