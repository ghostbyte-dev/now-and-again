package dev.ghostbyte.nowandagain.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import dev.ghostbyte.nowandagain.widget.NextTasksWidget
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao,
    private val appContext: Context,
) {
    val tasks: Flow<List<Task>> = dao.observeActive()

    suspend fun add(name: String, intervalDays: Int) {
        dao.insert(Task(name = name.trim(), intervalDays = intervalDays))
        refreshWidget()
    }

    suspend fun update(task: Task) {
        dao.update(task)
        refreshWidget()
    }

    suspend fun delete(task: Task) {
        dao.delete(task)
        refreshWidget()
    }

    suspend fun restore(task: Task) {
        dao.insert(task)
        refreshWidget()
    }

    private suspend fun refreshWidget() {
        runCatching { NextTasksWidget.refresh(appContext) }
    }
}