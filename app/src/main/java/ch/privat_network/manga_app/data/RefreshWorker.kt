package ch.privat_network.manga_app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Periodic background check. The server does the scraping on its own schedule,
 * so this only reads the list (GET), refreshes the offline cache and notifies about new chapters.
 */
class RefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = MangaRepository(applicationContext)
        val previous = repository.getCachedMangaList()

        val current = repository.getMangaList().getOrElse {
            // e.g. no VPN: try a few more times, otherwise wait for the next period
            return if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
        }

        if (previous != null) {
            val oldChapters = previous.mangas.associate { it.id to it.latestChapter }
            val updated = current.mangas.filter { manga ->
                val old = oldChapters[manga.id]
                old != null && old != manga.latestChapter
            }
            UpdateNotifier.notifyNewChapters(applicationContext, updated)
        }
        return Result.success()
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
