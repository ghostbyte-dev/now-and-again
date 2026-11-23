package dev.ghostbyte.nowandagain.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val intervalDays: Int,
    val createdDate: LocalDate = LocalDate.now(),
    val lastDoneDate: LocalDate? = null,
    val archived: Boolean = false,
)

/** The day the clock started: last completion, or creation if never done. */
val Task.startDate: LocalDate get() = lastDoneDate ?: createdDate.minusDays(1)