package ch.privat_network.manga_app

import android.app.Application
import android.content.Context
import androidx.work.*
import ch.privat_network.manga_app.data.ApiClient
import ch.privat_network.manga_app.data.RefreshWorker
import ch.privat_network.manga_app.data.UpdateNotifier
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import java.util.concurrent.TimeUnit

class MangaApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        UpdateNotifier.createChannel(this)
        setupBackgroundWork()
    }

    // Coil reuses the app's OkHttp client instead of creating its own
    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { ApiClient.okHttp })) }
            .crossfade(true)
            .build()

    private fun setupBackgroundWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MangaRefreshWork",
            // UPDATE replaces the schedule of installs that still have the old (PUT-based) job
            ExistingPeriodicWorkPolicy.UPDATE,
            repeatingRequest
        )
    }
}
