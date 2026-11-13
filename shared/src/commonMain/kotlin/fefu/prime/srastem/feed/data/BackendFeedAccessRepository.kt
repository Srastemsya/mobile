package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.feed.domain.FeedAccessRepository

/**
 * Кэшированный флаг завершённости профиля из сессии (`Session.profileComplete`).
 * Сервер остаётся источником истины: незавершённый профиль получит `403 PROFILE_INCOMPLETE`,
 * который [BackendFeedRepository] и [BackendRoomRepository] маппят в `FeedError.ProfileIncomplete`.
 */
class BackendFeedAccessRepository(private val api: BackendApi) : FeedAccessRepository {
    override suspend fun isProfileComplete(): Boolean = api.isProfileComplete()
}
