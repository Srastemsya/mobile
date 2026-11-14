package fefu.prime.srastem.feed.domain

/**
 * Доменные ошибки сценариев ленты. Транспортные коды мобильного контракта
 * (`docs/contracts/mobile-api.md`) маппятся в data-слое; presentation реагирует
 * только на эти типы и не знает HTTP-статусов.
 */
sealed class FeedError(override val message: String) : Exception(message) {
    /** `403 PROFILE_INCOMPLETE`: сценарии ленты доступны только с завершённым профилем. */
    data class ProfileIncomplete(override val message: String = "Завершите регистрацию, чтобы открыть подборку") : FeedError(message)

    /** `410 INVITE_EXPIRED`: новому участнику нужно запросить свежую ссылку. */
    data class InviteExpired(override val message: String = "Ссылка-приглашение истекла. Попросите организатора прислать новую") : FeedError(message)

    /** `404 NOT_FOUND`: комната удалена или ссылка чужая. */
    data class RoomNotFound(override val message: String = "Комната не найдена") : FeedError(message)

    /** `409 STALE_ROUND` / `INVALID_STATE`: перечитать snapshot перед следующим действием. */
    data class InvalidState(override val message: String = "Состояние комнаты изменилось. Обновляем данные") : FeedError(message)

    /** `409 CURSOR_EXPIRED`: предложить обновить подборку заново без курсора. */
    data class CursorExpired(override val message: String = "Подборка устарела. Обновите её") : FeedError(message)

    /** Прочие сетевые и серверные ошибки. */
    data class Transport(override val message: String) : FeedError(message)
}
