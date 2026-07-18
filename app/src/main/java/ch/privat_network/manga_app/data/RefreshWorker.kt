package ch.privat_network.manga_app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = MangaRepository()
        val result = repository.fetchAllUpdates()
        
        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}