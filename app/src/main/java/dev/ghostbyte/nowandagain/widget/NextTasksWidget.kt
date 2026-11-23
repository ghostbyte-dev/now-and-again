package dev.ghostbyte.nowandagain.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dev.ghostbyte.nowandagain.MainActivity
import dev.ghostbyte.nowandagain.R
import dev.ghostbyte.nowandagain.data.AppDatabase
import dev.ghostbyte.nowandagain.data.Task
import dev.ghostbyte.nowandagain.domain.TaskStatus
import dev.ghostbyte.nowandagain.domain.daysLeft
import dev.ghostbyte.nowandagain.domain.dueLabel
import dev.ghostbyte.nowandagain.domain.markDone
import dev.ghostbyte.nowandagain.domain.progress
import dev.ghostbyte.nowandagain.domain.status
import dev.ghostbyte.nowandagain.ui.theme.DarkColorScheme
import dev.ghostbyte.nowandagain.ui.theme.Green40
import dev.ghostbyte.nowandagain.ui.theme.Green80
import dev.ghostbyte.nowandagain.ui.theme.Grey200
import dev.ghostbyte.nowandagain.ui.theme.Grey300
import dev.ghostbyte.nowandagain.ui.theme.Grey750
import dev.ghostbyte.nowandagain.ui.theme.LightColorScheme
import dev.ghostbyte.nowandagain.ui.theme.Orange40
import dev.ghostbyte.nowandagain.ui.theme.Orange80
import dev.ghostbyte.nowandagain.ui.theme.Red40
import dev.ghostbyte.nowandagain.ui.theme.Red80
import java.time.LocalDateTime
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.glance.layout.ContentScale
import dev.ghostbyte.nowandagain.ui.theme.Black
import dev.ghostbyte.nowandagain.ui.theme.White
import androidx.core.graphics.createBitmap

private val WidgetColors = ColorProviders(light = LightColorScheme, dark = DarkColorScheme)

private val WidgetGreen = ColorProvider(day = Green40, night = Green80)
private val WidgetOrange = ColorProvider(day = Orange40, night = Orange80)
private val WidgetRed = ColorProvider(day = Red40, night = Red80)

// Same values as surfaceContainerHighest and outlineVariant in the app theme
private val WidgetTrack = ColorProvider(day = Grey200, night = Grey750)
private val WidgetDivider = ColorProvider(
    day = Grey300.copy(alpha = 0.5f),
    night = Grey750.copy(alpha = 0.5f),
)

private val BarHeight = 10.dp
private val SegmentGap = 2.dp

class NextTasksWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.get(context).taskDao()
        val initial = dao.getActive()
        provideContent {
            val tasks by dao.observeActive().collectAsState(initial = initial)
            val tick = currentState<Preferences>()[TickKey] ?: 0L
            GlanceTheme(colors = WidgetColors) { WidgetContent(tasks, tick) }
        }
    }

    companion object {
        val TickKey = longPreferencesKey("tick")

        /** Redraw all widgets and make sure the current time is read again. */
        suspend fun refresh(context: Context) {
            val ids = GlanceAppWidgetManager(context).getGlanceIds(NextTasksWidget::class.java)
            ids.forEach { id ->
                updateAppWidgetState(context, id) { it[TickKey] = System.currentTimeMillis() }
            }
            NextTasksWidget().updateAll(context)
        }
    }
}

val TaskIdKey = ActionParameters.Key<Long>("taskId")

@Composable
private fun WidgetContent(tasks: List<Task>, tick: Long) {
    val context = LocalContext.current
    val now = remember(tick, tasks) { LocalDateTime.now() }
    val today = now.toLocalDate()

    // widget width minus outer padding (8 + 8) and row padding (12 + 12)
    val barWidth = LocalSize.current.width - 40.dp

    val items = tasks.sortedWith(
        compareBy({ it.daysLeft(today) }, { -it.progress(now) })
    )

    Box(
        modifier = GlanceModifier.fillMaxSize().appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground).cornerRadius(16.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize()
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    context.getString(R.string.widget_empty),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            LazyColumn(GlanceModifier.fillMaxSize()) {
                itemsIndexed(items, itemId = { _, task -> task.id }) { index, task ->
                    Column(GlanceModifier.fillMaxWidth()) {
                        if (index > 0) {
                            Box(GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                                Box(
                                    GlanceModifier.fillMaxWidth().height(1.dp)
                                        .background(WidgetDivider)
                                ) {}
                            }
                        }
                        TaskRow(task, now, barWidth)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: Task, now: LocalDateTime, barWidth: Dp) {
    val context = LocalContext.current
    val today = now.toLocalDate()
    val status = task.status(today)

    val progress = if (status == TaskStatus.DoneToday) 1f else task.progress(now).coerceIn(0f, 1f)

    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    task.name,
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface,
                    ),
                )
                Text(
                    task.dueLabel(context.resources, today),
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = GlanceTheme.colors.onSurfaceVariant,
                    ),
                )
            }
            Spacer(GlanceModifier.width(12.dp))
            if (status == TaskStatus.DoneToday) {
                Box(GlanceModifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_check),
                        contentDescription = context.getString(R.string.cd_done_today),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                        modifier = GlanceModifier.size(24.dp),
                    )
                }
            } else {
                DoneButton(task)
            }
        }

        Spacer(GlanceModifier.height(10.dp))
        SegmentedBar(
            progress = progress,
            intervalDays = task.intervalDays,
            status = status,
        )
    }
}

@Composable
private fun DoneButton(task: Task) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier.size(44.dp).background(GlanceTheme.colors.primary)
            .cornerRadius(10.dp).clickable(
                actionRunCallback<MarkDoneAction>(actionParametersOf(TaskIdKey to task.id))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_check),
            contentDescription = context.getString(R.string.cd_mark_done, task.name),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimary),
            modifier = GlanceModifier.size(24.dp),
        )
    }
}

@Composable
private fun SegmentedBar(progress: Float, intervalDays: Int, status: TaskStatus) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val dark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    val fill = when (status) {
        TaskStatus.DoneToday -> if (dark) Green80 else Green40
        TaskStatus.DueToday -> if (dark) Orange80 else Orange40
        TaskStatus.Overdue -> if (dark) Red80 else Red40
        TaskStatus.Fine -> if (dark) White else Black
    }.toArgb()
    val track = (if (dark) Grey750 else Grey200).toArgb()

    // The image is scaled to the real width, so this only needs to be close.
    val widthPx = (((LocalSize.current.width - 40.dp).value) * density).toInt().coerceIn(200, 720)
    val heightPx = (BarHeight.value * density).toInt().coerceAtLeast(8)

    val bitmap = remember(widthPx, heightPx, intervalDays, progress, fill, track) {
        drawBar(
            widthPx = widthPx,
            heightPx = heightPx,
            days = intervalDays,
            progress = progress,
            fill = fill,
            track = track,
            gapPx = SegmentGap.value * density,
            radiusPx = 3f * density,
        )
    }

    Image(
        provider = ImageProvider(bitmap),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = GlanceModifier.fillMaxWidth().height(BarHeight),
    )
}

private fun drawBar(
    widthPx: Int,
    heightPx: Int,
    days: Int,
    progress: Float,
    fill: Int,
    track: Int,
    gapPx: Float,
    radiusPx: Float,
): Bitmap {
    val bitmap = createBitmap(widthPx, heightPx)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val w = widthPx.toFloat()
    val h = heightPx.toFloat()

    // One segment per day, unless a day would be thinner than two gaps (same rule as the app)
    val fits = (w - gapPx * (days - 1)) / days >= gapPx * 2
    val segments = if (days > 1 && fits) days else 1
    val segmentWidth = (w - gapPx * (segments - 1)) / segments

    canvas.clipPath(Path().apply {
        addRoundRect(RectF(0f, 0f, w, h), radiusPx, radiusPx, Path.Direction.CW)
    })

    for (i in 0 until segments) {
        val left = i * (segmentWidth + gapPx)
        val part = (progress * segments - i).coerceIn(0f, 1f)

        paint.color = track
        canvas.drawRect(left, 0f, left + segmentWidth, h, paint)
        if (part > 0f) {
            paint.color = fill
            canvas.drawRect(left, 0f, left + segmentWidth * part, h, paint)
        }
    }
    return bitmap
}

class MarkDoneAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val id = parameters[TaskIdKey] ?: return
        val dao = AppDatabase.get(context).taskDao()
        dao.getById(id)?.let { dao.update(it.markDone()) }
        NextTasksWidget.refresh(context)
    }
}