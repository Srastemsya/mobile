package fefu.prime.srastem.feed.domain

/** Создание комнаты (`POST /rooms`); создатель сразу становится участником. */
class CreateRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(name: String?): Result<RoomSnapshot> =
        runCatching { repository.createRoom(name) }
}
