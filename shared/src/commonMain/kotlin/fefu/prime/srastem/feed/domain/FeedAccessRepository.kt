package fefu.prime.srastem.feed.domain

/**
 * Доступность сценариев ленты: контрактом мобильного API они разрешены только
 * с завершённым профилем (`403 PROFILE_INCOMPLETE` иначе).
 */
interface FeedAccessRepository {
    suspend fun isProfileComplete(): Boolean
}
