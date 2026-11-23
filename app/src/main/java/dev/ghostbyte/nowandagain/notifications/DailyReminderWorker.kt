package dev.ghostbyte.nowandagain.notifications

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import dev.ghostbyte.nowandagain.data.AppDatabase
import dev.ghostbyte.nowandagain.data.SettingsRepository
import dev.ghostbyte.nowandagain.domain.TaskStatus
import dev.ghostbyte.nowandagain.domain.daysLeft
import dev.ghostbyte.nowandagain.domain.status
import dev.ghostbyte.nowandagain.widget.NextTasksWidget
import java.time.LocalDate

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val today = LocalDate.now()
            val settings = SettingsRepository.get(applicationContext).settings.value
            val due = AppDatabase.get(applicationContext).taskDao().getActive()
                .filter {
                    val s = it.status(today)
                    s == TaskStatus.DueToday || s == TaskStatus.Overdue
                }
                .sortedBy { it.daysLeft(today) }

            if (settings.reminderEnabled && due.isNotEmpty()) {
                Notifications.postDue(applicationContext, due, today)
            }
            NextTasksWidget.refresh(applicationContext)
        } finally {
            ReminderScheduler.schedule(applicationContext, ExistingWorkPolicy.APPEND_OR_REPLACE)
        }
        return Result.success()
    }
}