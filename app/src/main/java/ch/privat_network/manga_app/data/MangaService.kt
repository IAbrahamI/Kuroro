package ch.privat_network.manga_app.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MangaService {
    @GET("mangas") // Updated to match your FastAPI endpoint
    suspend fun getMangaList(): List<List<Any>>

    @POST("mangas/{manga_name}")
    suspend fun addManga(@Path("manga_name") name: String)

    @DELETE("mangas/{manga_name}")
    suspend fun deleteManga(@Path("manga_name") name: String)

    @PUT("mangas")
    suspend fun fetchAllUpdates()
}