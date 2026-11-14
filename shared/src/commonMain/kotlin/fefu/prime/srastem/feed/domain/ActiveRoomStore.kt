package fefu.prime.srastem.feed.domain

/** Remembers only the current room ID for the signed-in user; votes come from the live room snapshot. */
interface ActiveRoomStore {
    fun roomId(userId: String): String?
    fun save(userId: String, roomId: String)
    fun clear()
}
