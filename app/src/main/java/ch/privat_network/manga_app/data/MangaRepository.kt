package ch.privat_network.manga_app.data

import ch.privat_network.manga_app.domain.Manga
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MangaRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mangaapi.aby-host-network.duckdns.org/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(MangaService::class.java)

    suspend fun getMangaList(): List<Manga> {
        return try {
            val rawList = service.getMangaList()
            rawList.mapNotNull { array ->
                try {
                    // Position-based mapping based on user-provided structure
                    Manga(
                        id = (array[0] as? Double)?.toInt() ?: (array[0] as? Int) ?: 0,
                        url = array[1] as? String ?: "",
                        title = array[2] as? String ?: "",
                        imageUrl = array[3] as? String ?: "",
                        author = array[4] as? String ?: "",
                        status = array[5] as? String ?: "",
                        genres = array[6] as? String ?: "",
                        rating = array[7]?.toString() ?: "0.0",
                        description = array[9] as? String ?: "",
                        latestChapter = array[10] as? String ?: "",
                        chapterUrl = array[11] as? String ?: "",
                        date = array[12] as? String ?: ""
                    )
                } catch (e: Exception) {
                    null // Skip malformed entries
                }
            }.sortedByDescending { it.date } // Optimization: Sort by date so newest items are first
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addManga(name: String): Result<Unit> {
        return try {
            service.addManga(name)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteManga(name: String): Result<Unit> {
        return try {
            service.deleteManga(name)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun fetchAllUpdates(): Result<Unit> {
        return try {
            service.fetchAllUpdates()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}