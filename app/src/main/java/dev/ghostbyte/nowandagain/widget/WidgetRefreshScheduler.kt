package dev.ghostbyte.nowandagain.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object WidgetRefreshScheduler {
    private const val PERIODIC = "widget_refresh"
    private const val MIDNIGHT = "widget_refresh_midnight"
    const val KEY_MIDNIGHT = "midnight"

    fun schedule(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES).build(),
        )
        scheduleMidnight(context, ExistingWorkPolicy.KEEP)
    }

    fun scheduleMidnight(context: Context, policy: ExistingWorkPolicy) {
        val now = ZonedDateTime.now()
        val next = now.toLocalDate().plusDays(1).atTime(0, 1).atZone(now.zone)
        val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
            .setInitialDelay(Duration.between(now, next))
            .setInputData(Data.Builder().putBoolean(KEY_MIDNIGHT, true).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(MIDNIGHT, policy, request)
    }
}

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            if (WidgetPinner.hasWidget(applicationContext)) {
                NextTasksWidget.refresh(applicationContext)
            }
        } finally {
            if (inputData.getBoolean(WidgetRefreshScheduler.KEY_MIDNIGHT, false)) {
                WidgetRefreshScheduler.scheduleMidnight(
                    applicationContext,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                )
            }
        }
        return Result.success()
    }
}