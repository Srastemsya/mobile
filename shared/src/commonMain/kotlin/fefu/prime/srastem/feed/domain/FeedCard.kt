package fefu.prime.srastem.feed.domain

/**
 * Проекция `CatalogCard` для карточек ленты: обложка, заголовок, метки и подпись
 * места или даты. Полные детали объекта читаются отдельным запросом деталей каталога.
 */
data class FeedCard(
    val ref: FeedRef,
    val title: String,
    /** Идентификатор категории каталога (`categoryId` контракта); подписи выводятся на экране. */
    val category: String,
    val priceLabel: String,
    /** Цена карточки на человека в копейках; null — цена неизвестна. */
    val priceMinor: Long?,
    val addressOrDate: String,
    val description: String,
    val coverUrl: String?,
)
