package fefu.prime.srastem


data class BottomNavItem(
    val route: Destination,
    val title: String,
    val icon: BottomNavIcon,
)

enum class BottomNavIcon { Home, Map, Feed, Events, Profile }

val bottomNavItems = listOf(
    BottomNavItem(Destination.MainScreen, "Открытия", BottomNavIcon.Home),
    BottomNavItem(Destination.MapScreen, "Карта", BottomNavIcon.Map),
    BottomNavItem(Destination.FeedScreen, "Лента", BottomNavIcon.Feed),
    BottomNavItem(Destination.RoomScreen, "События", BottomNavIcon.Events),
    BottomNavItem(Destination.Profile, "Профиль", BottomNavIcon.Profile),
)
