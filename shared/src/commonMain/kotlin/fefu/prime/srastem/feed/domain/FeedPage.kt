package fefu.prime.srastem.feed.domain

/** Страница выдачи подборки; `nextCursor == null` означает конец выдачи. */
data class FeedPage(val items: List<FeedCard>, val nextCursor: String?)
