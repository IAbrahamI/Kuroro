package ch.privat_network.manga_app.data

import android.content.Context
import android.util.AtomicFile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** The last list the server returned, as raw rows, plus when it was fetched. */
class CachedRows(val rows: List<List<Any?>>, val savedAt: Long)

/**
 * Keeps the last successful GET /mangas response in one small JSON file,
 * so the list can be shown instantly on start and while the server is unreachable.
 */
class MangaCache(context: Context) {
    private val file = AtomicFile(File(context.filesDir, "manga_cache.json"))

    private val adapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    suspend fun read(): CachedRows? = withContext(Dispatchers.IO) {
        try {
            val json = adapter.fromJson(file.readFully().decodeToString()) ?: return@withContext null
            @Suppress("UNCHECKED_CAST")
            val rows = json["rows"] as? List<List<Any?>> ?: return@withContext null
            val savedAt = (json["savedAt"] as? Number)?.toLong() ?: 0L
            CachedRows(rows, savedAt)
        } catch (e: Exception) {
            null // No cache yet, or unreadable: behave as if there is none
        }
    }

    suspend fun write(rows: List<List<Any?>>, savedAt: Long) = withContext(Dispatchers.IO) {
        val out = file.startWrite()
        try {
            out.write(adapter.toJson(mapOf("savedAt" to savedAt, "rows" to rows)).encodeToByteArray())
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            e.printStackTrace()
        }
    }
}
