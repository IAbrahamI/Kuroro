package ch.privat_network.manga_app

import android.app.Application
import androidx.work.*
import ch.privat_network.manga_app.data.RefreshWorker
import java.util.concurrent.TimeUnit

class MangaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupBackgroundWork()
    }

    private fun setupBackgroundWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MangaRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}