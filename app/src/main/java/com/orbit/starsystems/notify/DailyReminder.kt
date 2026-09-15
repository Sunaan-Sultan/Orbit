package com.orbit.starsystems.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.orbit.starsystems.MainActivity
import com.orbit.starsystems.R
import com.orbit.starsystems.core.DailyFact
import com.orbit.starsystems.core.DeepLink
import com.orbit.starsystems.core.OrbitData
import com.orbit.starsystems.core.OrbitPrefs
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * The once-a-day nudge back to the app, and the only thing in Space Facts that runs while it
 * is closed. Off until the user turns it on from the You tab.
 *
 * Scheduled as a self-rescheduling one-shot rather than a 24-hour periodic request: periodic
 * work drifts away from the wall clock and cannot follow a time-zone change or a change to the
 * chosen hour, and this needs to land in the evening the user picked.
 */
object DailyReminder {

    private const val CHANNEL_ID = "daily_fact"
    private const val WORK_NAME = "daily_fact_reminder"
    private const val NOTIFICATION_ID = 1

    /** Re-arms or clears the reminder to match [OrbitPrefs]. Safe to call on every launch. */
    fun sync(context: Context) {
        if (OrbitPrefs.notifyEnabled) schedule(context) else cancel(context)
    }

    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<DailyFactWorker>()
            .setInitialDelay(millisUntilNextRun(OrbitPrefs.notifyHour), TimeUnit.MILLISECONDS)
            .build()
        // REPLACE rather than KEEP so changing the hour takes effect on the next run instead
        // of being ignored until the pending one fires.
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    /** Milliseconds from now until the next [hour] o'clock in the device's own time zone. */
    internal fun millisUntilNextRun(hour: Int, now: Long = System.currentTimeMillis()): Long {
        val target = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now
    }

    internal fun notifyToday(context: Context) {
        // The worker runs in its own process state; nothing guarantees the catalog is loaded.
        OrbitData.init(context)
        OrbitPrefs.init(context)
        val fact = DailyFact.factForToday() ?: return

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        createChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = DeepLink.toFact(fact.id).toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(fact.title)
            .setContentText(fact.sub)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fact.blurb))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // POST_NOTIFICATIONS can be revoked between the permission prompt and here.
        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily fact",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "One fact from the cosmos, once a day." }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}

/** Posts today's fact, then books itself in again for tomorrow. */
class DailyFactWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        OrbitPrefs.init(applicationContext)
        // The user may have switched the reminder off after this run was booked.
        if (!OrbitPrefs.notifyEnabled) return Result.success()
        DailyReminder.notifyToday(applicationContext)
        DailyReminder.schedule(applicationContext)
        return Result.success()
    }
}
