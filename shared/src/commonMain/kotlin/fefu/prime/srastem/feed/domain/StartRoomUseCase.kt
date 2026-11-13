package fefu.prime.srastem.feed.domain

/** Запуск комнаты организатором (`POST /rooms/{id}/start`): пул и состав фиксируются атомарно. */
class StartRoomUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(roomId: String, filters: FeedFilters): Result<RoomSnapshot> =
        runCatching { repository.startRoom(roomId, filters) }
}
