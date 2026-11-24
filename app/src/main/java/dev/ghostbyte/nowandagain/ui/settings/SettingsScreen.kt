package dev.ghostbyte.nowandagain.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingWorkPolicy
import dev.ghostbyte.nowandagain.R
import dev.ghostbyte.nowandagain.data.AppLanguage
import dev.ghostbyte.nowandagain.data.SettingsRepository
import dev.ghostbyte.nowandagain.data.ThemeMode
import dev.ghostbyte.nowandagain.notifications.ReminderScheduler
import dev.ghostbyte.nowandagain.ui.components.AppFooter
import dev.ghostbyte.nowandagain.ui.components.ListDivider
import dev.ghostbyte.nowandagain.ui.theme.TopBarButton
import dev.ghostbyte.nowandagain.widget.WidgetPinner
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { SettingsRepository.get(context.applicationContext) }
    val settings by repo.settings.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }

    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    var hasWidget by remember { mutableStateOf(WidgetPinner.hasWidget(context)) }
    val canPinWidget = remember { WidgetPinner.canPin(context) }

    LifecycleResumeEffect(Unit) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        hasWidget = WidgetPinner.hasWidget(context)
        onPauseOrDispose {}
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TopBarButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
        }

        // Appearance
        SectionLabel(stringResource(R.string.section_appearance))
        val modes = listOf(
            ThemeMode.System to R.string.theme_system,
            ThemeMode.Light to R.string.theme_light,
            ThemeMode.Dark to R.string.theme_dark,
        )
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            modes.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = settings.themeMode == mode,
                    onClick = { repo.setThemeMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = modes.size,
                        baseShape = MaterialTheme.shapes.small,
                    ),
                    icon = {},
                ) { Text(stringResource(label)) }
            }
        }

        // Language (per-app language needs Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            SectionLabel(stringResource(R.string.section_language))
            LanguagePicker()
        }

        // Reminder
        SectionLabel(stringResource(R.string.section_reminder))
        SettingsRow(
            title = stringResource(R.string.setting_reminder),
            onClick = {
                toggleReminder(context, repo, !settings.reminderEnabled, permissionLauncher::launch)
            },
        ) {
            Switch(checked = settings.reminderEnabled, onCheckedChange = null)
        }

        AnimatedVisibility(visible = settings.reminderEnabled) {
            Column {
                ListDivider()
                SettingsRow(
                    title = stringResource(R.string.setting_reminder_time),
                    onClick = { showTimePicker = true },
                ) {
                    Text(
                        LocalTime.of(settings.reminderHour, settings.reminderMinute)
                            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (!notificationsAllowed) {
                    ListDivider()
                    SettingsRow(
                        title = stringResource(R.string.notifications_blocked),
                        onClick = { openNotificationSettings(context) },
                    ) {
                        Text(
                            stringResource(R.string.notifications_open_settings),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

// Widget
        if (canPinWidget && !hasWidget) {
            SectionLabel(stringResource(R.string.section_widget))
            SettingsRow(
                title = stringResource(R.string.setting_widget),
                onClick = { WidgetPinner.requestPin(context) },
            ) {
                Text(
                    stringResource(R.string.setting_widget_action),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        //AppFooter(Modifier.padding(top = 64.dp))
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initialHour = settings.reminderHour,
            initialMinute = settings.reminderMinute,
            onConfirm = { hour, minute ->
                repo.setReminderTime(hour, minute)
                ReminderScheduler.schedule(context, ExistingWorkPolicy.REPLACE)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@RequiresApi(33)
@Composable
private fun LanguagePicker() {
    val context = LocalContext.current
    var language by remember { mutableStateOf(AppLanguage.current(context)) }
    val options = listOf(
        null to stringResource(R.string.language_system),
        "en" to "English",
        "de" to "Deutsch",
    )

    SingleChoiceSegmentedButtonRow(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        options.forEachIndexed { index, (tag, label) ->
            SegmentedButton(
                selected = language == tag,
                onClick = {
                    language = tag
                    AppLanguage.set(context, tag)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                    baseShape = MaterialTheme.shapes.small,
                ),
                icon = {},
            ) { Text(label) }
        }
    }
}

private fun toggleReminder(
    context: Context,
    repo: SettingsRepository,
    enabled: Boolean,
    requestPermission: (String) -> Unit,
) {
    repo.setReminderEnabled(enabled)
    if (enabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        requestPermission(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun openNotificationSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(
            Settings.EXTRA_APP_PACKAGE, context.packageName
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

@Composable
private fun ReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        text = { TimePicker(state = state) },
    )
}