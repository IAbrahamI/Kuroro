package ch.privat_network.manga_app.data

import android.content.Context
import ch.privat_network.manga_app.domain.Manga
import kotlinx.coroutines.CancellationException

/** A manga list together with the time it was fetched from the server. */
class MangaList(val mangas: List<Manga>, val fetchedAt: Long)

class MangaRepository(
    context: Context,
    private val service: MangaService = ApiClient.service
) {
    private val cache = MangaCache(context.applicationContext)

    /** Fetches the list from the server and stores it as the new offline cache. */
    suspend fun getMangaList(): Result<MangaList> = runCatchingApi {
        val rows = service.getMangaList()
        val now = System.currentTimeMillis()
        cache.write(rows, now)
        MangaList(rows.toMangas(), now)
    }

    /** The last list fetched successfully, or null if there is none yet. */
    suspend fun getCachedMangaList(): MangaList? =
        cache.read()?.let { MangaList(it.rows.toMangas(), it.savedAt) }

    suspend fun addManga(name: String): Result<Unit> = runCatchingApi { service.addManga(name) }

    suspend fun deleteManga(name: String): Result<Unit> = runCatchingApi { service.deleteManga(name) }

    /** Asks the server to re-scrape all mangas. Blocks until the server is done. */
    suspend fun fetchAllUpdates(): Result<Unit> = runCatchingApi { service.fetchAllUpdates() }

    private inline fun <T> runCatchingApi(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

private fun List<List<Any?>>.toMangas(): List<Manga> =
    mapNotNull { it.toManga() }
        .distinctBy { it.id } // LazyColumn keys must be unique
        .sortedByDescending { it.date } // ISO dates sort correctly as strings

/**
 * Position-based mapping of the backend's row arrays.
 * Rows without a numeric id are skipped instead of falling back to a shared id.
 */
private fun List<Any?>.toManga(): Manga? {
    val id = (getOrNull(0) as? Number)?.toInt() ?: return null
    fun text(index: Int) = getOrNull(index) as? String ?: ""
    return Manga(
        id = id,
        url = text(1),
        title = text(2),
        imageUrl = text(3),
        author = text(4),
        status = text(5),
        genres = text(6),
        rating = getOrNull(7)?.toString() ?: "0.0",
        description = text(9),
        latestChapter = text(10),
        chapterUrl = text(11),
        date = text(12)
    )
}
