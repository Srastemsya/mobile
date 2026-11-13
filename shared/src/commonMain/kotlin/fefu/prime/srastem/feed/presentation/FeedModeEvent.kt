package fefu.prime.srastem.feed.presentation

/** События экрана «Выбираем вместе» (FE-F00). */
sealed interface FeedModeEvent {
    /** Экран открыт с вкладки «Лента»: предвыбранный тип сбрасывается. */
    data object OpenedFromTab : FeedModeEvent

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
}
