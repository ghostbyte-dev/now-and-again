package dev.ghostbyte.nowandagain

import android.app.Application
import dev.ghostbyte.nowandagain.notifications.Notifications
import dev.ghostbyte.nowandagain.notifications.ReminderScheduler
import dev.ghostbyte.nowandagain.widget.WidgetRefreshScheduler

class NowAndAgainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
        ReminderScheduler.schedule(this)
        WidgetRefreshScheduler.schedule(this)

        //WorkManager.getInstance(this).enqueue(OneTimeWorkRequestBuilder<DailyReminderWorker>().build())
    }
}