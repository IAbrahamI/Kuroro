package ch.privat_network.manga_app.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ch.privat_network.manga_app.MainActivity
import ch.privat_network.manga_app.R
import ch.privat_network.manga_app.domain.Manga

/** Posts the "new chapter" notification. */
object UpdateNotifier {
    private const val CHANNEL_ID = "chapter_updates"
    private const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "New chapters",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "When a manga in your list gets a new chapter" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun notifyNewChapters(context: Context, updated: List<Manga>) {
        if (updated.isEmpty() || !canNotify(context)) return

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_menu_book)
            .setContentIntent(openApp)
            .setAutoCancel(true)

        if (updated.size == 1) {
            val manga = updated.first()
            builder.setContentTitle(manga.title).setContentText(manga.latestChapter)
        } else {
            val inbox = NotificationCompat.InboxStyle()
            updated.forEach { inbox.addLine("${it.title}: ${it.latestChapter}") }
            builder.setContentTitle("${updated.size} mangas have new chapters")
                .setContentText(updated.joinToString { it.title })
                .setStyle(inbox)
        }

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the post
        }
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
