package fefu.prime.srastem.feed.domain

/**
 * Фильтры подборки: зеркало query `GET /feed/candidates` и тела старта комнаты.
 * Бюджет указывается в копейках и трактуется как общий; цена карточки — на человека,
 * проверку `price * partySize <= budgetTotal` выполняет сервер, клиент не дублирует.
 */
data class FeedFilters(
    val kind: FeedKind = FeedKind.PLACE,
    val categoryId: String? = null,
    val localDate: String? = null,
    val budgetTotalMinor: Long? = null,
    val partySize: Int? = null,
)
