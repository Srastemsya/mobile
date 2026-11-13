package fefu.prime.srastem.feed.presentation

/** События экрана «Выбираем вместе» (FE-F00). */
sealed interface FeedModeEvent {
    /** Экран выбора открыт без предвыбора типа. */
    data object OpenedFromHome : FeedModeEvent

    /** Экран открыт входом «Свайпать события» с Главной: предвыбран `kind=EVENT`. */
    data object OpenedWithEventsKind : FeedModeEvent

    /** Выбрана карточка «На одном устройстве» → сценарий FE-F01 в одиночном режиме. */
    data object SingleDeviceSelected : FeedModeEvent

    /** Выбрана карточка «На нескольких устройствах» → сценарий FE-F04. */
    data object MultiDeviceSelected : FeedModeEvent

    /** Пользователь закрыл заглушку или сообщение об ошибке. */
    data object DismissNotice : FeedModeEvent

    /** `403 PROFILE_INCOMPLETE`: пользователь ушёл завершать регистрацию. */
    data object RetryProfileCheck : FeedModeEvent

    data object CompleteProfileSelected : FeedModeEvent
    data class KindChanged(val kind: fefu.prime.srastem.feed.domain.FeedKind) : FeedModeEvent
    data class CategoryChanged(val categoryId: String?) : FeedModeEvent
    data class DateChanged(val value: String) : FeedModeEvent
    data class BudgetChanged(val value: String) : FeedModeEvent
    data class PartySizeChanged(val value: String) : FeedModeEvent
    data object SubmitFilters : FeedModeEvent
    data object ChangeFilters : FeedModeEvent
    data class SwipeDecided(val decision: fefu.prime.srastem.feed.domain.SwipeDecision) :
        FeedModeEvent

    data object UndoSwipe : FeedModeEvent
    data object SwipeFinished : FeedModeEvent
    data object SwipePoolReady : FeedModeEvent
    data object OpenCatalogDetails : FeedModeEvent
    data object CloseCatalogDetails : FeedModeEvent
    data object RetryCatalogDetails : FeedModeEvent
    data object RetryNextPage : FeedModeEvent
}
