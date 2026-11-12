package fefu.prime.srastem.home.domain

data class HomeCollection(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
)

data class HomeContent(
    val city: String,
    val collections: List<HomeCollection>,
)

interface HomeRepository {
    suspend fun getHome(): HomeContent
}

class GetHome(private val repository: HomeRepository) {
    suspend operator fun invoke(): HomeContent = repository.getHome()
}
