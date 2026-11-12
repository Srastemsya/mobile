package fefu.prime.srastem.home.data

import fefu.prime.srastem.home.domain.HomeCollection
import fefu.prime.srastem.home.domain.HomeContent
import fefu.prime.srastem.home.domain.HomeRepository

/** Local implementation of GET /home. Replace this binding with the API repository when the BE is available. */
class MockHomeRepository : HomeRepository {
    override suspend fun getHome() = HomeContent(
        city = "Владивосток",
        collections = listOf(
            HomeCollection("evening-bars", "Вечерние бары\nСветланской", "5 мест · от 800 ₽", "Подборка"),
            HomeCollection("russky-weekend", "Выходные на\nРусском", "3 события · от 1 500 ₽", "Подборка"),
        ),
    )
}
