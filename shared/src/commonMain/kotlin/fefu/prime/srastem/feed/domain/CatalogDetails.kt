package fefu.prime.srastem.feed.domain

/** Full catalog fields needed by the chosen-result and detail screens. */
data class CatalogDetails(
    val ref: FeedRef,
    val title: String,
    val description: String,
    val categoryId: String,
    val address: String,
    val startsAt: String?,
    val endsAt: String?,
    val priceLabel: String,
    val admissionLabel: String?,
    val coverUrl: String?,
)
