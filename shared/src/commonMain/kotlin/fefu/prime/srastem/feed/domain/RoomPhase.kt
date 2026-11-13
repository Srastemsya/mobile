package fefu.prime.srastem.feed.domain

/** Фазы комнаты: зеркало `Room.state` контракта Discovery v1. */
enum class RoomPhase { LOBBY, VOTING, REFRESHING, MATCHED, EXHAUSTED, CLOSED }
