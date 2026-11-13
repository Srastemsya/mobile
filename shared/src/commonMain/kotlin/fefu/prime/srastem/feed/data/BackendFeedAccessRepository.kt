package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.feed.domain.FeedAccessRepository
import kotlinx.serialization.json.jsonObject

/** Проверяет актуальное состояние профиля через мобильный API. */
class BackendFeedAccessRepository internal constructor(
    private val getProfile: suspend () -> kotlinx.serialization.json.JsonElement,
) : FeedAccessRepository {
    constructor(api: BackendApi) : this({ api.get("/v1/me") })
    override suspend fun isProfileComplete(): Boolean = try {
        getProfile().jsonObject["profileComplete"]?.toString() == "true"
    } catch (error: fefu.prime.srastem.backend.BackendException) {
        if (error.status == 403 && error.code == "PROFILE_INCOMPLETE") false else throw error
    }
}
