package ch.privat_network.manga_app.data

import ch.privat_network.manga_app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Single shared HTTP stack for the whole app (API calls and Coil image loading).
 */
object ApiClient {
    private const val BASE_URL = "https://mangaapi.aby-host-network.duckdns.org/"

    // PUT /mangas makes the server re-scrape every manga before it answers (~10-30 s)
    private const val SCRAPE_READ_TIMEOUT_SECONDS = 60

    val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            if (chain.request().method == "PUT") {
                chain.withReadTimeout(SCRAPE_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .proceed(chain.request())
            } else {
                chain.proceed(chain.request())
            }
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    val service: MangaService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(MangaService::class.java)
}
