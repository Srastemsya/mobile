package fefu.prime.srastem.feed.domain

import kotlinx.coroutines.flow.Flow

/** Long polling комнаты: один открытый запрос, наружу — `Flow<RoomSnapshot>`. */
class ObserveRoomUseCase(private val repository: RoomRepository) {
    operator fun invoke(roomId: String): Flow<RoomSnapshot> = repository.observeRoom(roomId)
}
