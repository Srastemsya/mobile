package fefu.prime.srastem.home.domain

data class HomeCollection(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val categoryId: String? = null,
    val filters: Map<String, String> = emptyMap(),
)

data class HomeQuickCategory(val id: String, val label: String, val kinds: List<String>)

data class HomeUpcoming(
    val id: String,
    val title: String,
    val categoryId: String,
    val startsAt: String,
    val address: String
)

data class HomeContent(
    val city: String,
    val collections: List<HomeCollection>,
    val quickCategories: List<HomeQuickCategory> = emptyList(),
    val upcoming: List<HomeUpcoming> = emptyList(),
)

interface HomeRepository {
    suspend fun getHome(): HomeContent
}

class GetHome(private val repository: HomeRepository) {
    suspend operator fun invoke(): HomeContent = repository.getHome()
}
