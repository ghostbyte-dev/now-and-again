package dev.ghostbyte.nowandagain.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ghostbyte.nowandagain.R
import dev.ghostbyte.nowandagain.data.HintPreference
import dev.ghostbyte.nowandagain.data.Task
import dev.ghostbyte.nowandagain.domain.TaskStatus
import dev.ghostbyte.nowandagain.domain.dueLabel
import dev.ghostbyte.nowandagain.domain.intervalLabel
import dev.ghostbyte.nowandagain.domain.lastDoneLabel
import dev.ghostbyte.nowandagain.ui.components.AppFooter
import dev.ghostbyte.nowandagain.ui.components.ListDivider
import dev.ghostbyte.nowandagain.ui.theme.AddTaskButton
import dev.ghostbyte.nowandagain.ui.theme.SettingsButton
import dev.ghostbyte.nowandagain.ui.theme.barColor
import dev.ghostbyte.nowandagain.widget.WidgetPinner
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private sealed interface SheetMode {
    data object Add : SheetMode
    data class Edit(val task: Task) : SheetMode
}

private const val FOOTER_KEY = "footer"
private const val HINT_KEY = "widget_hint"
private val FOOTER_MIN_HEIGHT = 200.dp

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit, vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var sheetMode by remember { mutableStateOf<SheetMode?>(null) }

    val topBarHeight = 120.dp
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val background = MaterialTheme.colorScheme.background

    val context = LocalContext.current
    val resources = LocalResources.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* nothing to do: if denied, no reminders */ }

    fun askForNotifications() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val hintPrefs = remember { HintPreference(context.applicationContext) }
    var hintDismissed by remember { mutableStateOf(hintPrefs.widgetHintDismissed) }
    var hasWidget by remember { mutableStateOf(WidgetPinner.hasWidget(context)) }

// Re-check whenever the user comes back, for example from the launcher.
    LifecycleResumeEffect(Unit) {
        hasWidget = WidgetPinner.hasWidget(context)
        onPauseOrDispose {}
    }

// Once a widget exists, the hint never returns, even if it's removed later.
    LaunchedEffect(hasWidget) {
        if (hasWidget && !hintDismissed) {
            hintPrefs.dismissWidgetHint()
            hintDismissed = true
        }
    }

    val showWidgetHint = !hintDismissed && !hasWidget && state.items.isNotEmpty()

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Box(Modifier.fillMaxSize()) {

                // 1. Content, drawn behind the bar
                when {
                    state.loading -> Unit
                    state.items.isEmpty() -> EmptyState(
                        modifier = Modifier.padding(padding),
                        onAdd = { sheetMode = SheetMode.Add },
                    )

                    else -> {
                        val listState = rememberLazyListState()
                        val density = LocalDensity.current
                        val footerMinPx = with(density) { FOOTER_MIN_HEIGHT.roundToPx() }
                        val taskCount = state.items.size

                        // Height of the footer item. With few tasks it grows, so that the
                        // whole list is exactly one footer taller than the screen.
                        val footerHeightPx by remember(taskCount, footerMinPx) {
                            derivedStateOf {
                                val info = listState.layoutInfo
                                val visible = info.visibleItemsInfo.filter { it.key != FOOTER_KEY }
                                val visibleTasks = visible.count { it.key != HINT_KEY }
                                if (visibleTasks < taskCount) {
                                    footerMinPx
                                } else {
                                    val used =
                                        info.beforeContentPadding + visible.sumOf { it.size } + info.afterContentPadding
                                    maxOf(
                                        footerMinPx, info.viewportSize.height + footerMinPx - used
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = statusBarTop + topBarHeight),
                        ) {
                            itemsIndexed(
                                state.items, key = { _, item -> item.task.id }) { index, item ->
                                Column(Modifier.animateItem()) {
                                    if (index > 0) {
                                        ListDivider()
                                    }
                                    TaskListItem(
                                        item = item,
                                        onClick = { sheetMode = SheetMode.Edit(item.task) },
                                        onDone = {
                                            vm.markDone(item.task)
                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                val result = snackbarHostState.showSnackbar(
                                                    message = resources.getString(
                                                        R.string.snackbar_task_done, item.task.name
                                                    ),
                                                    actionLabel = resources.getString(R.string.action_undo),
                                                    duration = SnackbarDuration.Short,
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    vm.restore(item.task)
                                                }
                                            }
                                        },
                                    )
                                }
                            }

                            if (showWidgetHint) {
                                item(key = HINT_KEY) {
                                    WidgetHint(
                                        canPin = WidgetPinner.canPin(context),
                                        onAdd = { WidgetPinner.requestPin(context) },
                                        onClose = {
                                            hintPrefs.dismissWidgetHint()
                                            hintDismissed = true
                                        },
                                        modifier = Modifier.animateItem(),
                                    )
                                }
                            }

                            item(key = FOOTER_KEY) {
                                AppFooter(Modifier.height(with(density) { footerHeightPx.toDp() }))
                            }
                        }
                    }
                }

                // 2. Fade: cards dissolve into the background toward the top
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(statusBarTop + topBarHeight + 24.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to background,
                                0.55f to background.copy(alpha = 0.9f),
                                1f to background.copy(alpha = 0f),
                            )
                        )
                )

                // 3. Bar: logo and theme toggle, transparent
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(topBarHeight)
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    //AppLogo(size = 67.dp)

                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AddTaskButton(onClick = { sheetMode = SheetMode.Add })
                        SettingsButton(onClick = onOpenSettings)
                    }
                }
            }
        }

        sheetMode?.let { mode ->
            val editing = (mode as? SheetMode.Edit)?.task
            TaskSheet(
                initial = editing,
                onDismiss = { sheetMode = null },
                onSave = { name, days, lastDone ->
                    if (editing == null) {
                        vm.addTask(name, days)
                        askForNotifications()
                    } else {
                        vm.editTask(editing, name, days, lastDone)
                    }
                    sheetMode = null
                },
                onDelete = editing?.let { task ->
                    {
                        vm.delete(task)
                        sheetMode = null
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = resources.getString(
                                    R.string.snackbar_task_deleted, task.name
                                ),
                                actionLabel = resources.getString(R.string.action_undo),
                                duration = SnackbarDuration.Short,
                            )
                            if (result == SnackbarResult.ActionPerformed) vm.undoDelete(task)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun TaskListItem(
    item: TaskItem,
    onClick: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current

    val barColor = item.status.barColor(neutral = MaterialTheme.colorScheme.primary)
    val target = if (item.status == TaskStatus.DoneToday) 1f else item.progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, label = "progress")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.task.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    item.task.dueLabel(resources),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(16.dp))

            val doneToday = item.status == TaskStatus.DoneToday

            if (doneToday) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.cd_done_today),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            } else {
                FilledIconButton(
                    onClick = onDone,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = "Mark ${item.task.name} as done",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }


        }
        Spacer(Modifier.height(12.dp))

        SegmentedProgressBar(
            progress = animated,
            days = item.task.intervalDays,
            color = barColor,
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                item.task.lastDoneLabel(resources),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                item.task.intervalLabel(resources),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            SampleTask()
            Spacer(Modifier.height(40.dp))
            Text(
                stringResource(R.string.empty_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onAdd,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
            ) { Text(stringResource(R.string.empty_action)) }
        }
    }
}

@Composable
private fun SampleTask() {
    val resources = LocalResources.current
    val tile = MaterialTheme.colorScheme.surfaceContainerLow
    // Done yesterday, 3 day interval: the label shows a weekday
    val sample = remember {
        val yesterday = LocalDate.now().minusDays(1)
        Task(name = "", intervalDays = 3, createdDate = yesterday, lastDoneDate = yesterday)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        shape = MaterialTheme.shapes.medium,
        color = tile,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.empty_sample_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        sample.dueLabel(resources),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SegmentedProgressBar(
                progress = 1f / 3f,
                days = 3,
                color = MaterialTheme.colorScheme.primary,
                gapColor = tile,
            )
        }
    }
}

@Composable
private fun TaskSheet(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (String, Int, LocalDate?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    BackHandler(onBack = onDismiss)

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            enter = slideInVertically(animationSpec = tween(300)) { it },
            exit = slideOutVertically(animationSpec = tween(200)) { it },
        ) {
            SheetContent(initial, onSave, onDelete)
        }
    }
}

@Composable
private fun SegmentedProgressBar(
    progress: Float,
    days: Int,
    color: Color,
    modifier: Modifier = Modifier,
    gapColor: Color = MaterialTheme.colorScheme.background,
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .drawWithContent {
                drawContent()
                val gap = 2.dp.toPx()
                if (days > 1 && size.width / days >= gap * 2) {
                    for (d in 1 until days) {
                        val x = size.width * d / days
                        drawRect(gapColor, Offset(x - gap / 2, 0f), Size(gap, size.height))
                    }
                }
            },
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        strokeCap = StrokeCap.Butt,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}

@Composable
private fun SheetContent(
    initial: Task?,
    onSave: (String, Int, LocalDate?) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var days by remember { mutableIntStateOf(initial?.intervalDays ?: 3) }

    var lastDone by remember { mutableStateOf(initial?.lastDoneDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (initial == null) focusRequester.requestFocus()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_task)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
            Spacer(Modifier.height(24.dp))
            IntervalPicker(days = days, onChange = { days = it.coerceIn(1, MAX_INTERVAL) })

            if (initial != null) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(R.string.field_last_done),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        lastDone?.format(dateFormat) ?: stringResource(R.string.last_done_never),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) { Text(stringResource(R.string.action_delete)) }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Button(
                    onClick = { onSave(name, days, lastDone) },
                    enabled = name.isNotBlank(),
                    shape = MaterialTheme.shapes.small,
                ) { Text(stringResource(R.string.action_save)) }
            }
        }
    }

    if (showDatePicker) {
        LastDoneDialog(
            initial = lastDone,
            onConfirm = { lastDone = it; showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun LastDoneDialog(
    initial: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial ?: today).toUtcMillis(),
        selectableDates = object : SelectableDates {
            // Only today or earlier: a task can't have been done in the future
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= today.toUtcMillis()

            override fun isSelectableYear(year: Int) = year <= today.year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onConfirm(it.toLocalDateUtc()) } },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    ) {
        DatePicker(state = state)
    }
}

// The picker works with UTC midnight timestamps, so convert in UTC.
// Using the local zone would give a date that's off by one in some time zones.
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private const val MAX_INTERVAL = 365

private val presets = listOf(1, 2, 3, 7, 14, 30)

@Composable
private fun presetLabel(days: Int): String = when (days) {
    1 -> stringResource(R.string.preset_daily)
    7 -> stringResource(R.string.preset_weekly)
    14 -> stringResource(R.string.preset_weeks, 2)
    else -> stringResource(R.string.preset_days, days)
}

@Composable
private fun IntervalPicker(days: Int, onChange: (Int) -> Unit) {
    Column {
        Text(
            stringResource(R.string.picker_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledTonalIconButton(
                onClick = { onChange(days - 1) },
                enabled = days > 1,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(56.dp),
            ) { Text("−", style = MaterialTheme.typography.headlineMedium) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$days", style = MaterialTheme.typography.displayMedium)
                Text(
                    pluralStringResource(R.plurals.unit_days, days),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilledTonalIconButton(
                onClick = { onChange(days + 1) },
                enabled = days < MAX_INTERVAL,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(56.dp),
            ) { Text("+", style = MaterialTheme.typography.headlineMedium) }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { value ->
                FilterChip(
                    selected = days == value,
                    onClick = { onChange(value) },
                    label = { Text(presetLabel(value)) },
                    shape = MaterialTheme.shapes.small,
                )
            }
        }
    }
}


@Composable
private fun WidgetHint(
    canPin: Boolean,
    onAdd: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Box {
            Column(Modifier.padding(20.dp)) {
                Text(
                    stringResource(R.string.widget_hint_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 32.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (canPin) stringResource(R.string.widget_hint_body)
                    else stringResource(
                        R.string.widget_hint_manual,
                        stringResource(R.string.app_name),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (canPin) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onAdd,
                        shape = MaterialTheme.shapes.small,
                    ) { Text(stringResource(R.string.widget_hint_action)) }
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.widget_hint_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}