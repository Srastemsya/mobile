package fefu.prime.srastem

import kotlinx.serialization.Serializable

sealed interface Destination {

    @Serializable
    data object MainScreen: Destination

    @Serializable
    data object MapScreen: Destination

    @Serializable
    data object FeedScreen: Destination

    @Serializable
    data object RoomScreen: Destination

    @Serializable
    data object EventsScreen: Destination

    @Serializable
    data object AiScreen: Destination

    @Serializable
    data object Profile: Destination
}

