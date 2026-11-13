package fefu.prime.srastem.feed.domain

/** Выпуск ссылки-приглашения (`POST /rooms/{id}/invites`), только владелец в LOBBY. */
class CreateInviteUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke(roomId: String): Result<RoomInvite> =
        runCatching { repository.createInvite(roomId) }
}
