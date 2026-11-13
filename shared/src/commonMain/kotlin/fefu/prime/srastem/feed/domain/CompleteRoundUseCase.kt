package fefu.prime.srastem.feed.domain

/** Завершение своего прохода раунда (`PUT .../completion`); требуется голос по каждой карточке. */
class CompleteRoundUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(roomId: String, roundId: String): Result<RoomSnapshot> =
        runCatching { repository.completeRound(roomId, roundId) }
}
