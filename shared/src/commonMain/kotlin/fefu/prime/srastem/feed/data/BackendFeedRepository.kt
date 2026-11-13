package fefu.prime.srastem.feed.data

import fefu.prime.srastem.backend.BackendApi
import fefu.prime.srastem.backend.BackendException
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedPage
import fefu.prime.srastem.feed.domain.FeedRepository
import kotlinx.serialization.json.jsonObject
/** Одиночная выдача подборки через общий `BackendApi`: `GET /v1/feed/candidates`. */
class BackendFeedRepository(private val api: BackendApi) : FeedRepository {

    override suspend fun getCandidates(filters: FeedFilters, cursor: String?): FeedPage = try {
        FeedMappers.page(api.get(FeedContract.candidatesQuery(filters, cursor)).jsonObject)
    } catch (error: BackendException) {
        throw FeedMappers.errorOf(error)
    } catch (error: Exception) {
        throw FeedMappers.errorOf(error)
    }
}

internal fun String.encodeQueryComponent(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if ((value in 0x41..0x5a) || (value in 0x61..0x7a) || (value in 0x30..0x39) ||
            value == 0x2d || value == 0x2e || value == 0x5f || value == 0x7e
        ) {
            append(value.toChar())
        } else {
            append('%')
            append("0123456789ABCDEF"[value ushr 4])
            append("0123456789ABCDEF"[value and 0x0f])
        }
    }
}
