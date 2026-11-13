package fefu.prime.srastem.feed.domain

/** Голос по карточке раунда (`PUT .../votes/{kind}/{id}`); до завершения прохода голос заменяем. */
class SubmitVoteUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(
        roomId: String,
        roundId: String,
        candidate: FeedRef,
        decision: SwipeDecision,
    ): Result<RoomSnapshot> = runCatching { repository.submitVote(roomId, roundId, candidate, decision) }
}
