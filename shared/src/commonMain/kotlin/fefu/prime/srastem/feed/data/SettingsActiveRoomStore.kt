package fefu.prime.srastem.feed.data

import com.russhwolf.settings.Settings
import fefu.prime.srastem.feed.domain.ActiveRoomStore

class SettingsActiveRoomStore(private val settings: Settings) : ActiveRoomStore {
    override fun roomId(userId: String): String? =
        settings.getStringOrNull(ROOM_ID)?.takeIf { settings.getStringOrNull(USER_ID) == userId }

    override fun save(userId: String, roomId: String) {
        settings.putString(USER_ID, userId)
        settings.putString(ROOM_ID, roomId)
    }

    override fun clear() {
        settings.remove(ROOM_ID)
        settings.remove(USER_ID)
    }

    private companion object {
        const val ROOM_ID = "feed.activeRoom.id"
        const val USER_ID = "feed.activeRoom.userId"
    }
}
