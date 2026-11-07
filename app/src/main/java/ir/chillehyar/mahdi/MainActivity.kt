package ir.chillehyar.mahdi

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.media.RingtoneManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private val Forest = Color(0xFF173A31)
private val Forest2 = Color(0xFF285647)
private val Gold = Color(0xFFC39A52)
private val Cream = Color(0xFFF7F4EC)
private val CardCream = Color(0xFFFFFDF8)
private val Ink = Color(0xFF25362F)
private val Muted = Color(0xFF7A827B)
private val PaleGold = Color(0xFFF0E5CF)
private val Rose = Color(0xFFB46B63)
private const val APP_NAME = "ساختن یار"
private const val APP_TAGLINE = "راهی بی‌پایان برای امام زمان (عج) · قدم‌به‌قدم تا ظهور"
private val habitIconOptions = listOf(
    "📖", "🕌", "🤲", "📿", "🕋", "🌙", "⭐", "🧎", "💚", "🌱",
    "💧", "🏃", "📚", "🧠", "⏰", "🧹", "🚭", "📵", "🍬", "🎮",
    "😤", "🗣️", "👀", "🍃", "🪥", "💤", "🍎", "💪", "🧘", "🕯️"
)
private val habitColorOptions = listOf(
    "#176B55", "#245B83", "#80613B", "#8C5A72", "#68804A", "#A65E45",
    "#586A9C", "#83704C", "#477D7A", "#6C5A8D", "#9B7650", "#64736D"
)
private data class DailyChecklistItem(val id: Int, val title: String)
private val LocalPageScrollState = compositionLocalOf<ScrollState?> { null }

private val defaultHabits = listOf("قرائت قرآن", "خوش‌اخلاقی", "دعا برای فرج")
private val dailyTasks = listOf("نمازهایم را با توجه خواندم", "قرآن و دعا خواندم", "برای امام زمان(عج) دعا کردم", "از گناه و عادت نامطلوب دوری کردم", "به دیگران نیکی کردم", "محاسبهٔ نفس انجام دادم")
private val habitTasks = listOf(
    listOf("نماز صبح", "نماز ظهر", "نماز عصر", "نماز مغرب", "نماز عشا", "نماز اول وقت", "حضور قلب", "تعقیبات", "نماز مستحبی", "محاسبهٔ نماز"),
    listOf("قرآن صبحگاهی", "ترجمهٔ آیات", "تدبر در آیات", "حفظ یک آیه", "تلاوت روزانه", "سورهٔ عهد", "آیت‌الکرسی", "هدیهٔ ثواب", "عمل به یک آیه", "مرور آموخته‌ها"),
    listOf("صلوات روزانه", "دعای فرج", "دعای عهد", "ذکر استغفار", "ذکر یا مهدی", "صدقه برای سلامتی", "یاد امام زمان", "زیارت آل‌یاسین", "دعا برای مؤمنان", "تجدید عهد"),
    listOf("کنترل زبان", "کنترل نگاه", "ترک غیبت", "ترک دروغ", "کنترل خشم", "ترک اسراف", "کنترل فضای مجازی", "دوری از بدگمانی", "حفظ حق‌الناس", "توبه و جبران"),
    listOf("کمک به خانواده", "احوال‌پرسی", "دستگیری نیازمند", "لبخند و خوش‌رفتاری", "انتقال خیر", "رسیدگی به والدین", "رفع کدورت", "کار خیر پنهانی", "رعایت حق دیگران", "نیت خدمت")
)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("chillehyar_native_v30", MODE_PRIVATE) }
    private var revision by mutableIntStateOf(0)
    private var page by mutableStateOf("خانه")
    private val pageBackStack = mutableListOf<String>()
    private var customDateDraft by mutableStateOf("")
    private var selectedDay by mutableIntStateOf(1)
    private var selectedHabit by mutableIntStateOf(0)
    private var noteDraft by mutableStateOf("")
    private var slipDraft by mutableStateOf("")
    private var customHabitDraft by mutableStateOf("")
    private var customTargetDraft by mutableStateOf("")
    private var customSpecialDraft by mutableStateOf("")
    private var customEmergencyDraft by mutableStateOf("")
    private var reminderHour by mutableIntStateOf(8)
    private var reminderMinute by mutableIntStateOf(0)
    private var reminderEnabled by mutableStateOf(false)
    private var selectedVirtueDate by mutableStateOf("۱۴۰۵/۰۸/۱۰")
    private var motivationTitleDraft by mutableStateOf("کارهای انگیزشی امروز")
    private var motivationDateDraft by mutableStateOf("۱۴۰۵/۰۸/۱۰")
    private var tacticNameDraft by mutableStateOf("")
    private var tacticSolutionDraft by mutableStateOf("")
    private var themeChoice by mutableIntStateOf(0)
    private var uiScale by mutableFloatStateOf(1f)
    private val virtueHeaders = listOf("واجب", "حرام", "مستحب", "مکروه", "اخلاق", "سلامتی", "شغلی", "تفریحی", "خانواده", "علمی", "متفرقه")
    private val motivationItems = listOf("یادآوری اهداف", "تفکر", "کتاب", "کلاس", "کلاس اخلاق و موعظه", "سخنرانی", "آیات قرآن", "احادیث", "دعا", "توسل", "ارتباط با امام زمان (عج)", "اشک بر امام حسین (ع)", "تلقین مثبت", "ارتباط با افراد موفق", "همیار", "کمک به دیگران")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderEnabled = prefs.getBoolean("reminder_enabled", false)
        themeChoice = prefs.getInt("theme_choice", 0)
        uiScale = prefs.getFloat("ui_scale", 1f)
        reminderHour = prefs.getInt("reminder_hour", 8)
        reminderMinute = prefs.getInt("reminder_minute", 0)
        createNotificationChannel()
        // Rebuild alarms when the app is opened as a safety net after updates or device-specific alarm cleanup.
        if (reminderEnabled) ReminderReceiver.schedule(this, prefs)
        habitNames().forEachIndexed { index, name ->
            if (prefs.getBoolean("habit_reminder_${index}_on", false)) {
                ReminderReceiver.scheduleHabit(
                    this, index, name,
                    prefs.getString("habit_reminder_${index}_time", "20:00") ?: "20:00",
                    prefs.getString("habit_reminder_${index}_text", "") ?: ""
                )
            }
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 29)
        }
        setContent { ChillehyarApp() }
    }

    // Re-arm reminders after returning from system special-access settings.
    override fun onResume() {
        super.onResume()
        if (prefs.getBoolean("reminder_enabled", false)) ReminderReceiver.schedule(this, prefs)
        habitNames().forEachIndexed { index, name ->
            if (prefs.getBoolean("habit_reminder_${index}_on", false)) {
                ReminderReceiver.scheduleHabit(
                    this, index, name,
                    prefs.getString("habit_reminder_${index}_time", "20:00") ?: "20:00",
                    prefs.getString("habit_reminder_${index}_text", "") ?: ""
                )
            }
        }
    }

    private fun navigateTo(target: String) {
        if (target == page) return
        pageBackStack.add(page)
        page = target
    }
    private fun navigateBack() {
        if (pageBackStack.isNotEmpty()) page = pageBackStack.removeAt(pageBackStack.lastIndex)
        else page = "خانه"
    }
    private fun keyDay(day: Int, task: Int) = "day_${day}_task_$task"
    private fun dailyTaskItems(): List<DailyChecklistItem> {
        val raw = prefs.getString("daily_task_items_json", null) ?: return dailyTasks.mapIndexed { index, title -> DailyChecklistItem(index, title) }
        return try {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val title = item.optString("title").trim()
                if (title.isBlank()) null else DailyChecklistItem(item.optInt("id", index), title)
            }
        } catch (_: Exception) {
            dailyTasks.mapIndexed { index, title -> DailyChecklistItem(index, title) }
        }
    }
    private fun saveDailyTaskItems(items: List<DailyChecklistItem>) {
        val json = JSONArray()
        items.forEach { item -> json.put(JSONObject().put("id", item.id).put("title", item.title)) }
        prefs.edit().putString("daily_task_items_json", json.toString()).apply()
        revision++
    }
    private fun isTaskDone(day: Int, task: Int) = prefs.getBoolean(keyDay(day, task), false)
    private fun setTask(day: Int, task: Int, value: Boolean) { prefs.edit().putBoolean(keyDay(day, task), value).apply(); revision++ }
    private fun dayDoneCount(day: Int) = dailyTaskItems().count { isTaskDone(day, it.id) }
    private fun completedDays(): Int { val total = dailyTaskItems().size; if (total == 0) return 0; return (1..40).count { dayDoneCount(it) == total } }
    private fun currentDay(): Int {
        val start = prefs.getLong("start_date", 0L)
        if (start == 0L) return 1
        return (((System.currentTimeMillis() - start) / 86_400_000L).toInt() + 1).coerceIn(1, 40)
    }
    private fun habitDaysConsidered(index: Int): Int {
        val savedStart = prefs.getLong("hmeta_${index}_start_millis", 0L)
        val parsedStart = prefs.getString("hmeta_${index}_date", null)?.let { raw -> parseHabitDate(raw) } ?: 0L
        val start = when {
            savedStart > 0L -> savedStart
            parsedStart > 0L -> parsedStart
            else -> prefs.getLong("start_date", 0L)
        }
        if (start <= 0L) return 1
        return (((System.currentTimeMillis() - start) / 86_400_000L).toInt() + 1).coerceIn(1, 40)
    }
    private fun startChallenge() { if (prefs.getLong("start_date", 0L) == 0L) prefs.edit().putLong("start_date", System.currentTimeMillis()).apply(); revision++ }
    private fun habitNames(): List<String> {
        val extra = prefs.getString("custom_habits", "")!!.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        return (defaultHabits + extra).mapIndexed { index, name -> prefs.getString("habit_name_override_$index", name) ?: name }
    }
    private fun normalizedDateDigits(value: String): String = value.map { ch ->
        when (ch) {
            in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
            in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
            else -> ch
        }
    }.joinToString("")
    private fun parseHabitDate(value: String): Long {
        val normalized = normalizedDateDigits(value.trim())
        return try { SimpleDateFormat("yyyy/MM/dd", Locale.US).apply { isLenient = false }.parse(normalized)?.time ?: System.currentTimeMillis() }
        catch (_: Exception) { System.currentTimeMillis() }
    }
    private fun todayDateText(): String = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
    private fun defaultHabitEmoji(index: Int): String = when (index) {
        0 -> "📖"
        1 -> "💚"
        2 -> "🤲"
        else -> "🌱"
    }
    private fun habitColor(index: Int): Color {
        val raw = prefs.getString("hmeta_${index}_color", habitColorOptions[index % habitColorOptions.size]) ?: habitColorOptions[index % habitColorOptions.size]
        return try { Color(android.graphics.Color.parseColor(raw)) } catch (_: Exception) { Forest2 }
    }
    private fun taskKey(habit: Int, task: Int, day: Int) = "habit_${habit}_task_${task}_day_${day}"
    private fun taskDone(habit: Int, task: Int, day: Int) = prefs.getBoolean(taskKey(habit, task, day), false)
    private fun setHabitTask(habit: Int, task: Int, day: Int, value: Boolean) { prefs.edit().putBoolean(taskKey(habit, task, day), value).apply(); revision++ }
    private fun noteKey(day: Int) = "note_day_$day"
    private fun notes(): List<Pair<Int, String>> = (1..40).mapNotNull { d -> prefs.getString(noteKey(d), null)?.takeIf { it.isNotBlank() }?.let { d to it } }.reversed()
    private fun saveNote(day: Int, text: String) { prefs.edit().putString(noteKey(day), text).apply(); revision++ }
    private fun saveSlip(day: Int, text: String) { prefs.edit().putString("slip_day_$day", text).apply(); revision++ }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(ReminderReceiver.CHANNEL, "یادآورهای ساختن یار", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "یادآور روزانه و مسیر ساختن یار"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    private fun requestExactAlarmAccessIfNeeded() {
        if (Build.VERSION.SDK_INT >= 31) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "برای اجرای دقیق سرِ ساعت، دسترسی «هشدارها و یادآورهای دقیق» را هم فعال کن؛ در غیر این صورت اعلان ممکن است با تأخیر برسد.", Toast.LENGTH_LONG).show()
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
                } catch (_: Exception) {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                }
            }
        }
    }

    private fun configureReminder(enabled: Boolean, hour: Int, minute: Int) {
        reminderEnabled = enabled; reminderHour = hour; reminderMinute = minute
        prefs.edit().putBoolean("reminder_enabled", enabled).putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
        val alarm = getSystemService(AlarmManager::class.java)
        val intent = Intent(this, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this, ReminderReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (enabled) {
            val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
            ReminderReceiver.scheduleAlarm(this, c.timeInMillis, pending)
            requestExactAlarmAccessIfNeeded()
        } else alarm.cancel(pending)
        revision++
    }
    private fun deleteCustomHabit(index: Int) {
        if (index < defaultHabits.size) return
        val extras = (prefs.getString("custom_habits", "") ?: "").split("\n").filter { it.isNotBlank() }.toMutableList()
        val extraIndex = index - defaultHabits.size
        if (extraIndex !in extras.indices) return
        val oldCount = defaultHabits.size + extras.size
        extras.removeAt(extraIndex)
        val editor = prefs.edit()
        val prefixes = listOf("h400_", "h400_row_", "hmeta_", "habit_note_", "habit_reminder_", "habit_name_override_", "habit_" )
        val all = prefs.all
        // Remove the selected habit's index-based keys, then shift later habit data down one slot.
        all.keys.forEach { key ->
            prefixes.forEach { prefix ->
                if (key.startsWith(prefix)) {
                    val tail = key.removePrefix(prefix)
                    val digits = tail.takeWhile { it.isDigit() }
                    if (digits.isNotEmpty()) {
                        val parsed = digits.toIntOrNull()
                        if (parsed == index) editor.remove(key)
                    }
                }
            }
        }
        for (oldIndex in index + 1 until oldCount) {
            all.forEach { (key, value) ->
                prefixes.forEach { prefix ->
                    if (key.startsWith(prefix)) {
                        val tail = key.removePrefix(prefix)
                        val digits = tail.takeWhile { it.isDigit() }
                        if (digits.toIntOrNull() == oldIndex) {
                            val newKey = prefix + (oldIndex - 1) + tail.drop(digits.length)
                            when (value) { is Boolean -> editor.putBoolean(newKey,value); is Int -> editor.putInt(newKey,value); is Long -> editor.putLong(newKey,value); is Float -> editor.putFloat(newKey,value); is String -> editor.putString(newKey,value) }
                            editor.remove(key)
                        }
                    }
                }
            }
        }
        editor.putString("custom_habits", extras.joinToString("\n")).apply()
        selectedHabit = (defaultHabits.size).coerceAtMost((defaultHabits.size + extras.size - 1).coerceAtLeast(0))
        if (pageBackStack.isNotEmpty()) pageBackStack.removeAt(pageBackStack.lastIndex)
        page = "عادت‌ها"; revision++
    }

    private fun configureHabitReminder(index: Int, habitName: String, enabled: Boolean, time: String, message: String) {
        val parts = time.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0,23) ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0,59) ?: 0
        val alarm = getSystemService(AlarmManager::class.java)
        val requestCode = 10000 + index
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            action = "ir.chillehyar.mahdi.HABIT_REMINDER"
            putExtra("habit_index", index); putExtra("habit_name", habitName); putExtra("habit_time", "%02d:%02d".format(hour,minute)); putExtra("habit_message", message)
        }
        val pending = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (!enabled) { alarm.cancel(pending); pending.cancel(); return }
        val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour); set(Calendar.MINUTE,minute); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0); if(timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
        ReminderReceiver.scheduleAlarm(this, c.timeInMillis, pending)
        requestExactAlarmAccessIfNeeded()
    }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) exportBackup(uri) }
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) importBackup(uri) }
    private fun exportBackup(uri: Uri) {
        try {
            val all = JSONObject()
            val data = JSONObject()
            prefs.all.forEach { (k, v) -> when (v) { is Boolean -> data.put(k, v); is Int -> data.put(k, v); is Long -> data.put(k, v); is Float -> data.put(k, v); is String -> data.put(k, v) } }
            all.put("app", "ChillehyarMahdavi").put("schema", 1).put("exportedAt", System.currentTimeMillis()).put("preferences", data)
            contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(all.toString(2)) }
            Toast.makeText(this, "پشتیبان با موفقیت ذخیره شد", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { Toast.makeText(this, "خطا در پشتیبان‌گیری: ${e.message}", Toast.LENGTH_LONG).show() }
    }
    private fun importBackup(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() } ?: error("فایل خوانده نشد")
            val root = JSONObject(text); val data = root.getJSONObject("preferences"); val editor = prefs.edit()
            data.keys().forEach { key -> val value = data.get(key); when (value) { is Boolean -> editor.putBoolean(key, value); is Int -> editor.putInt(key, value); is Long -> editor.putLong(key, value); is String -> editor.putString(key, value); is Double -> editor.putLong(key, value.toLong()) } }
            editor.apply(); reminderEnabled = prefs.getBoolean("reminder_enabled", false); reminderHour = prefs.getInt("reminder_hour", 8); reminderMinute = prefs.getInt("reminder_minute", 0)
            if (reminderEnabled) configureReminder(true, reminderHour, reminderMinute)
            revision++; Toast.makeText(this, "بازیابی اطلاعات انجام شد", Toast.LENGTH_LONG).show()
        } catch (e: Exception) { Toast.makeText(this, "فایل پشتیبان معتبر نیست: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    @Composable private fun ChillehyarApp() {
        // Read revision to trigger redraws without recreating the whole page/scroll state.
        val observedRevision = revision
        val palette = listOf(Color(0xFF173A31), Color(0xFF176B55), Color(0xFF2F6FBA), Color(0xFF7B61A8), Color(0xFFB96865), Color(0xFF946C2E))
        val chosenPrimary = palette[themeChoice.coerceIn(0, palette.lastIndex)]
        val density = LocalDensity.current
        val pageScrollStates = remember { mutableMapOf<String, ScrollState>() }
        val scrollKey = if (page == "جزئیات عادت") "$page-$selectedHabit" else page
        val activeScrollState = pageScrollStates.getOrPut(scrollKey) { ScrollState(prefs.getInt("page_scroll_$scrollKey", 0)) }
        LaunchedEffect(scrollKey, activeScrollState) {
            snapshotFlow { activeScrollState.value }.distinctUntilChanged().collectLatest { value ->
                delay(450)
                prefs.edit().putInt("page_scroll_$scrollKey", value).apply()
            }
        }
        DisposableEffect(scrollKey, activeScrollState) {
            onDispose { prefs.edit().putInt("page_scroll_$scrollKey", activeScrollState.value).apply() }
        }
        BackHandler(enabled = page != "خانه") { navigateBack() }
        MaterialTheme(colorScheme = lightColorScheme(primary = chosenPrimary, secondary = Gold, background = Cream, surface = CardCream, onPrimary = Color.White, onBackground = Ink, onSurface = Ink)) {
            CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl, LocalDensity provides androidx.compose.ui.unit.Density(density.density, uiScale.coerceIn(.9f,1.2f)), LocalPageScrollState provides activeScrollState) {
                Scaffold(containerColor = Cream, bottomBar = { BottomNavigationBar(page) { navigateTo(it) } }) { padding ->
                    if (observedRevision < 0) Spacer(Modifier.height(0.dp))
                    key(page) {
                        Column(Modifier.fillMaxSize().padding(padding)) {
                            if (page != "خانه") HeaderBar(page)
                            when (page) {
                                "خانه" -> HomePage()
                                "چله من" -> ChillaPage()
                                "عادت‌ها" -> HabitsPage()
                                "ثبت روزانه" -> DailyPage()
                                "جزئیات عادت" -> HabitDetailPage()
                                "یادداشت‌ها" -> NotesPage()
                                "گزارش" -> ReportsPage()
                                "یادآورها" -> RemindersPage()
                                "ابزارهای پیشرفته" -> ToolsPage()
                                "جدول فضیلت‌ها" -> VirtuePage()
                                "افزایش انگیزه" -> MotivationPage()
                                "تحلیل لغزش" -> TacticsPage()
                                "جدول تحلیل‌ها" -> HabitAnalysisTablePage()
                                else -> SettingsPage()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable private fun HeaderBar(title: String) {
        Column(Modifier.fillMaxWidth().background(Cream)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(APP_NAME, color = Forest, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(title, color = Muted, fontSize = 11.sp)
                }
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(PaleGold).border(1.dp, Color(0xFFE0C995), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("س", color = Forest, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(Color(0xFFE7DCC7)))
        }
    }

    @Composable private fun BottomNavigationBar(selected: String, onSelect: (String) -> Unit) {
        val items = listOf("خانه" to Icons.Filled.Home, "عادت‌ها" to Icons.Filled.CheckCircle, "تحلیل" to Icons.Filled.Insights, "تنظیمات" to Icons.Filled.Settings)
        NavigationBar(containerColor = CardCream, contentColor = Forest, tonalElevation = 8.dp) {
            items.forEach { (label, icon) ->
                val target = when(label) { "تحلیل" -> "جدول تحلیل‌ها"; else -> label }
                NavigationBarItem(selected = if(label == "تحلیل") selected == "جدول تحلیل‌ها" else selected == target,
                    onClick = { onSelect(target) }, icon = { Icon(icon, null) },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Forest, selectedTextColor = Forest, indicatorColor = PaleGold, unselectedIconColor = Muted))
            }
        }
    }

    @Composable private fun ScrollPage(content: @Composable ColumnScope.() -> Unit) {
        val rememberedPageScroll = LocalPageScrollState.current ?: rememberScrollState()
        Column(Modifier.fillMaxSize().verticalScroll(rememberedPageScroll).padding(16.dp), content = content)
    }
    @Composable private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) { Card(Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) { Column(Modifier.padding(16.dp), content = content) } }
    @Composable private fun SectionTitle(title: String, subtitle: String? = null) { Column(Modifier.padding(bottom = 10.dp)) { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Forest); if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Muted) } }
    @Composable private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PaleGold)) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Forest, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center) } } }
    @Composable private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) { Button(onClick, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest)) { Text(text, fontWeight = FontWeight.SemiBold) } }

    @Composable private fun HomePage() {
        val day = currentDay()
        val done = dayDoneCount(day)
        val taskCount = dailyTaskItems().size.coerceAtLeast(1)
        val percent = (completedDays() / 40f).coerceIn(0f, 1f)
        ScrollPage {
            Card(
                Modifier.fillMaxWidth().padding(bottom = 13.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = CardCream),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column {
                    Box(Modifier.fillMaxWidth().height(265.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))) {
                        Image(
                            painter = painterResource(id = R.drawable.home_hero),
                            contentDescription = "مسیر روشن و قدم‌های پیوسته برای یاری امام زمان",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x22102A22)))))
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = Forest2, modifier = Modifier.size(25.dp))
                            Text("روز جاری", color = Muted, fontSize = 11.sp)
                            Text(day.toString(), color = Forest, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            Modifier.size(94.dp).clip(CircleShape).background(Color(0xFFFFF8E9)).border(2.dp, Color(0xFFE5D2AC), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(progress = { percent }, modifier = Modifier.fillMaxSize().padding(5.dp), strokeWidth = 7.dp, color = Forest2, trackColor = PaleGold)
                            Text("${(percent * 100).toInt()}٪", color = Forest, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("پیشرفت کلی مسیر", color = Muted, fontSize = 11.sp)
                            Text("${completedDays()} / ۴۰", color = Forest, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                            Text("${done} از ${taskCount} کار امروز", color = Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("عادت‌های من", color = Forest, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(7.dp)); Text("🌿", fontSize = 23.sp)
            }
            habitNames().forEachIndexed { index, name ->
                val count = (0 until 400).count { prefs.getString("h400_${index}_$it", "") != "" }
                val ratio = count / 400f
                val accent = habitColor(index)
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 9.dp).clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFFFFAF0)).border(1.dp, Color(0xFFE9DCC4), RoundedCornerShape(22.dp))
                        .clickable { selectedHabit = index; navigateTo("جزئیات عادت") }.padding(horizontal = 12.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                        Text(prefs.getString("hmeta_${index}_emoji", defaultHabitEmoji(index)) ?: defaultHabitEmoji(index), fontSize = 32.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = Forest, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("$count از ۴۰۰ قدم", color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = accent, trackColor = PaleGold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFFFF7E9)).border(2.dp, Color(0xFFE9D8B8), CircleShape), contentAlignment = Alignment.Center) {
                            Text("${(ratio * 100).toInt()}٪", fontSize = 13.sp, color = Forest, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.ChevronLeft, null, tint = Forest2)
                    }
                }
            }
            OutlinedButton(onClick = { navigateTo("عادت‌ها") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("مدیریت و افزودن عادت")
            }
            PremiumCard {
                SectionTitle("چک‌لیست امروز", "با ثبت هر مورد، پیشرفت روزانه‌ات را ببین")
                dailyTaskItems().forEach { item ->
                    Row(Modifier.fillMaxWidth().clickable { setTask(day,item.id,!isTaskDone(day,item.id)) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTaskDone(day,item.id), onCheckedChange = { setTask(day,item.id,it) }, colors = CheckboxDefaults.colors(checkedColor = Forest))
                        Text(item.title, Modifier.weight(1f), fontSize = 14.sp, color = if(isTaskDone(day,item.id)) Muted else Ink)
                    }
                }
                TextButton(onClick = { navigateTo("تنظیمات") }, modifier = Modifier.align(Alignment.End)) { Text("ویرایش چک‌لیست") }
            }
            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0D6))) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("پیام امروز", color = Forest, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text("با هر قدم کوچک، به امام زمان (عج) نزدیک‌تر می‌شوی…", color = Forest, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 25.sp)
                }
            }
        }
    }

    @Composable private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit, modifier: Modifier = Modifier) { OutlinedButton(onClick = action, modifier = modifier, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, PaleGold), contentPadding = PaddingValues(6.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Gold); Text(label, fontSize = 11.sp, color = Forest) } } }

    @Composable private fun ChillaPage() {
        ScrollPage {
            PremiumCard { SectionTitle("چلهٔ چهل‌روزهٔ من", "تاریخ شروع و پیشرفت شخصی")
                val start = prefs.getLong("start_date", 0L)
                Text(if (start == 0L) "هنوز چله را شروع نکرده‌ای." else "شروع: ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(start))}", color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp)); PrimaryButton(if (start == 0L) "شروع چلهٔ من" else "چله آغاز شده است", { startChallenge() }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatTile("${currentDay()}", "روز جاری", Modifier.weight(1f)); StatTile("${completedDays()}", "روز تکمیل‌شده", Modifier.weight(1f)); StatTile("${40-completedDays()}", "روز باقی‌ماندهٔ کامل", Modifier.weight(1f)) }
            }
            PremiumCard { SectionTitle("تقویم پیشرفت", "روی هر روز بزن تا ثبت روزانهٔ همان روز باز شود")
                (1..40).chunked(5).forEach { rowDays -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { rowDays.forEach { day -> val complete = dailyTaskItems().isNotEmpty() && dayDoneCount(day) == dailyTaskItems().size; val today = day == currentDay(); Box(Modifier.weight(1f).padding(vertical = 4.dp).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (complete) Forest else if (today) PaleGold else Cream).border(if (today) 1.dp else 0.dp, Gold, RoundedCornerShape(12.dp)).clickable { selectedDay = day; navigateTo("ثبت روزانه") }, contentAlignment = Alignment.Center) { Text(if (complete) "✓" else day.toString(), color = if (complete) Color.White else Forest, fontWeight = if (today || complete) FontWeight.Bold else FontWeight.Normal) } } } }
            }
        }
    }

    @Composable private fun HabitsPage() {
        val names = habitNames()
        var createEmoji by remember { mutableStateOf("📖") }
        var createColor by remember { mutableStateOf(habitColorOptions[0]) }
        ScrollPage {
            PremiumCard {
                SectionTitle("عادت‌های من", "هر عادت مسیر ۴۰۰ قدمی مستقل خودش را دارد")
                names.forEachIndexed { index, name ->
                    val total = (0 until 400).count { prefs.getString("h400_${index}_$it", "") != "" }
                    val accent = habitColor(index)
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Cream).clickable { selectedHabit = index; navigateTo("جزئیات عادت") }.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha=.13f)), contentAlignment = Alignment.Center) {
                                Text(prefs.getString("hmeta_${index}_emoji", defaultHabitEmoji(index)) ?: defaultHabitEmoji(index), fontSize = 26.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.Bold, color = Forest); Text("$total از ۴۰۰ قدم ثبت‌شده", fontSize = 11.sp, color = Muted) }
                            Icon(Icons.Filled.ChevronLeft, null, tint = accent)
                        }
                        Spacer(Modifier.height(9.dp)); LinearProgressIndicator(progress = { total / 400f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = accent, trackColor = PaleGold)
                    }
                    Spacer(Modifier.height(9.dp))
                }
            }
            PremiumCard {
                SectionTitle("ساخت عادت تازه", "نماد و رنگ را انتخاب کن؛ بعداً همه مشخصات قابل ویرایش‌اند")
                OutlinedTextField(customHabitDraft, { customHabitDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام عادت") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(customDateDraft.ifBlank { todayDateText() }, { customDateDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تاریخ شروع") }, singleLine = true, shape = RoundedCornerShape(14.dp), readOnly = true)
                TextButton(onClick = {
                    val initial = Calendar.getInstance()
                    DatePickerDialog(this@MainActivity, { _, year, month, dayOfMonth ->
                        val selected = Calendar.getInstance().apply { set(year, month, dayOfMonth, 12, 0, 0) }
                        customDateDraft = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(selected.time)
                    }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show()
                }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(5.dp)); Text("انتخاب تاریخ") }
                Spacer(Modifier.height(10.dp)); HabitStylePicker(createEmoji, { createEmoji = it }, createColor, { createColor = it })
                Spacer(Modifier.height(8.dp)); OutlinedTextField(customTargetDraft, { customTargetDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه کوچک؛ مقدار معمول") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(7.dp)); OutlinedTextField(customSpecialDraft, { customSpecialDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه ویژه؛ مقدار کمتر") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(7.dp)); OutlinedTextField(customEmergencyDraft, { customEmergencyDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه اضطراری؛ حداقل قدم") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(10.dp))
                PrimaryButton("ایجاد عادت", {
                    val name = customHabitDraft.trim()
                    if (name.isNotEmpty() && name !in names) {
                        val newIndex = names.size
                        val date = customDateDraft.ifBlank { todayDateText() }
                        val editor = prefs.edit()
                            .putString("custom_habits", (prefs.getString("custom_habits", "")!! + "\n" + name).trim())
                            .putString("hmeta_${newIndex}_target",customTargetDraft)
                            .putString("hmeta_${newIndex}_special",customSpecialDraft)
                            .putString("hmeta_${newIndex}_emergency",customEmergencyDraft)
                            .putString("hmeta_${newIndex}_date",date)
                            .putString("hmeta_${newIndex}_emoji",createEmoji)
                            .putString("hmeta_${newIndex}_color",createColor)
                            .putLong("hmeta_${newIndex}_start_millis",parseHabitDate(date))
                        editor.apply()
                        customHabitDraft = ""; customDateDraft = ""; customTargetDraft=""; customSpecialDraft=""; customEmergencyDraft=""
                        createEmoji = "📖"; createColor = habitColorOptions[0]
                        selectedHabit = newIndex; revision++; navigateTo("جزئیات عادت")
                    } else Toast.makeText(this@MainActivity,"نام عادت را وارد کن یا نام تکراری را تغییر بده.",Toast.LENGTH_SHORT).show()
                }, Modifier.fillMaxWidth())
            }
        }
    }

    @Composable private fun HabitStylePicker(emoji: String, onEmoji: (String) -> Unit, colorHex: String, onColor: (String) -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("نماد عادت", color = Forest, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("نمادهای مذهبی، رشد فردی و ترک عادت‌های نامطلوب", color = Muted, fontSize = 11.sp)
            habitIconOptions.chunked(6).forEach { group ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    group.forEach { option ->
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (emoji == option) PaleGold else Cream).border(if (emoji == option) 2.dp else 1.dp, if (emoji == option) Gold else Color(0xFFE4DED2), RoundedCornerShape(12.dp)).clickable { onEmoji(option) }, contentAlignment = Alignment.Center) {
                            Text(option, fontSize = 23.sp)
                        }
                    }
                    repeat(6-group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            OutlinedTextField(emoji, { onEmoji(it) }, modifier = Modifier.fillMaxWidth(), label = { Text("نماد دلخواه؛ می‌توانی ایموجی خودت را وارد کنی") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            Text("رنگ عادت", color = Forest, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            habitColorOptions.chunked(6).forEach { rowColors ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowColors.forEach { option ->
                        val selected = option.equals(colorHex, ignoreCase = true)
                        val swatch = try { Color(android.graphics.Color.parseColor(option)) } catch (_: Exception) { Forest2 }
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(swatch).border(if(selected) 3.dp else 1.dp, if(selected) Gold else Color.White, CircleShape).clickable { onColor(option) }, contentAlignment = Alignment.Center) {
                            if (selected) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    repeat(6-rowColors.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }

    @Composable private fun HabitDetailPage() {
        val names = habitNames(); if (names.isEmpty()) return
        val habit = selectedHabit.coerceIn(0, names.lastIndex)
        val completed = (0 until 400).count { prefs.getString("h400_${habit}_$it", "") != "" }
        val small = (0 until 400).count { prefs.getString("h400_${habit}_$it", "") == "s" }
        val special = (0 until 400).count { prefs.getString("h400_${habit}_$it", "") == "v" }
        val emergency = (0 until 400).count { prefs.getString("h400_${habit}_$it", "") == "e" }
        var target by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_target", "") ?: "") }
        var specialTarget by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_special", "") ?: "") }
        var emergencyTarget by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_emergency", "") ?: "") }
        var note by remember(habit) { mutableStateOf(prefs.getString("habit_note_$habit", "") ?: "") }
        var editableName by remember(habit) { mutableStateOf(names[habit]) }
        var editableStartDate by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_date", "") ?: "") }
        var editableEmoji by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_emoji", defaultHabitEmoji(habit)) ?: defaultHabitEmoji(habit)) }
        var editableColor by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_color", habitColorOptions[habit % habitColorOptions.size]) ?: habitColorOptions[habit % habitColorOptions.size]) }
        var showDeleteConfirm by remember(habit) { mutableStateOf(false) }
        var showHabitEditor by remember(habit) { mutableStateOf(false) }
        var showTargetEditor by remember(habit) { mutableStateOf(false) }
        var reminderOn by remember(habit) { mutableStateOf(prefs.getBoolean("habit_reminder_${habit}_on", false)) }
        var reminderTime by remember(habit) { mutableStateOf(prefs.getString("habit_reminder_${habit}_time", "20:00") ?: "20:00") }
        var reminderText by remember(habit) { mutableStateOf(prefs.getString("habit_reminder_${habit}_text", ReminderReceiver.MESSAGES[habit % ReminderReceiver.MESSAGES.size]) ?: ReminderReceiver.MESSAGES[habit % ReminderReceiver.MESSAGES.size]) }
        ScrollPage {
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(habitColor(habit).copy(alpha=.14f)), contentAlignment = Alignment.Center) { Text(prefs.getString("hmeta_${habit}_emoji", defaultHabitEmoji(habit)) ?: defaultHabitEmoji(habit), fontSize = 30.sp) }
                    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { SectionTitle(names[habit], "جدول ۴۰۰ قدم · ۱۰ ردیف × ۴۰ خانه"); Text("تغییر وضعیت هر خانه با لمس آن", fontSize = 11.sp, color = Muted) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { StatTile("${completed}", "ثبت‌شده", Modifier.weight(1f)); StatTile("${small}", "لقمه کوچک", Modifier.weight(1f)); StatTile("${special}", "ویژه", Modifier.weight(1f)); StatTile("${emergency}", "اضطراری", Modifier.weight(1f)) }
                Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { completed / 400f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Gold, trackColor = PaleGold)
                Text("${completed * 100 / 400}% تکمیل · ${400-completed} خانه باقی‌مانده", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
            }
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionTitle("مشخصات عادت")
                        Text("${names[habit]} · شروع ${editableStartDate.ifBlank { "ثبت‌نشده" }}", color = Muted, fontSize = 12.sp)
                    }
                    Text(prefs.getString("hmeta_${habit}_emoji", defaultHabitEmoji(habit)) ?: defaultHabitEmoji(habit), fontSize = 30.sp)
                }
                OutlinedButton(onClick = { showHabitEditor = !showHabitEditor }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(if(showHabitEditor) Icons.Filled.ExpandLess else Icons.Filled.Edit, null); Spacer(Modifier.width(6.dp)); Text(if(showHabitEditor) "بستن ویرایش مشخصات" else "ویرایش نام، تاریخ، نماد و رنگ")
                }
                if (showHabitEditor) {
                    Spacer(Modifier.height(9.dp))
                    OutlinedTextField(editableName, { editableName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام عادت") }, singleLine = true)
                    Spacer(Modifier.height(7.dp)); PrimaryButton("ذخیرهٔ نام عادت", { if(editableName.isNotBlank()) { prefs.edit().putString("habit_name_override_$habit",editableName.trim()).apply(); val extras = prefs.getString("custom_habits", "")!!.split("\n").filter { it.isNotBlank() }.toMutableList(); if(habit >= defaultHabits.size && habit-defaultHabits.size < extras.size) { extras[habit-defaultHabits.size] = editableName.trim(); prefs.edit().putString("custom_habits",extras.joinToString("\n")).apply() }; revision++; Toast.makeText(this@MainActivity,"نام عادت ذخیره شد",Toast.LENGTH_SHORT).show() } }, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(editableStartDate.ifBlank { todayDateText() }, { editableStartDate = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تاریخ شروع") }, singleLine = true, readOnly = true)
                    TextButton(onClick = {
                        val initial = Calendar.getInstance().apply { timeInMillis = parseHabitDate(editableStartDate.ifBlank { todayDateText() }) }
                        DatePickerDialog(this@MainActivity, { _, year, month, dayOfMonth ->
                            val selected = Calendar.getInstance().apply { set(year, month, dayOfMonth, 12, 0, 0) }
                            editableStartDate = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(selected.time)
                        }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.align(Alignment.End)) { Icon(Icons.Filled.CalendarMonth, null); Spacer(Modifier.width(5.dp)); Text("تغییر تاریخ") }
                    Spacer(Modifier.height(7.dp)); HabitStylePicker(editableEmoji, { editableEmoji = it }, editableColor, { editableColor = it })
                    Spacer(Modifier.height(7.dp)); PrimaryButton("ذخیرهٔ تاریخ، نماد و رنگ", { prefs.edit().putString("hmeta_${habit}_date",editableStartDate.ifBlank { todayDateText() }).putLong("hmeta_${habit}_start_millis",parseHabitDate(editableStartDate.ifBlank { todayDateText() })).putString("hmeta_${habit}_emoji",editableEmoji).putString("hmeta_${habit}_color",editableColor).apply(); revision++; Toast.makeText(this@MainActivity,"مشخصات عادت ذخیره شد",Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
                }
                if (habit >= defaultHabits.size) { Spacer(Modifier.height(7.dp)); OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)) { Icon(Icons.Filled.Delete,null); Spacer(Modifier.width(6.dp)); Text("حذف این عادت شخصی") } }
                if (showDeleteConfirm) AlertDialog(onDismissRequest = { showDeleteConfirm = false }, title = { Text("حذف عادت؟") }, text = { Text("عادت و داده‌های وابسته به آن از این دستگاه حذف می‌شود. اگر لازم است، ابتدا نسخهٔ پشتیبان بگیر.") }, confirmButton = { TextButton(onClick = { showDeleteConfirm = false; deleteCustomHabit(habit) }) { Text("حذف", color = Rose) } }, dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") } })
            }
            PremiumCard {
                SectionTitle("سه سطح انجام کار", "برای روزهای عادی، سخت و خیلی سخت")
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("لقمه کوچک: ${target.ifBlank { "تعریف نشده" }}", color = Forest, fontSize = 13.sp)
                    Text("لقمه ویژه: ${specialTarget.ifBlank { "تعریف نشده" }}", color = Forest, fontSize = 13.sp)
                    Text("لقمه اضطراری: ${emergencyTarget.ifBlank { "تعریف نشده" }}", color = Forest, fontSize = 13.sp)
                }
                TextButton(onClick = { showTargetEditor = !showTargetEditor }, modifier = Modifier.align(Alignment.End)) { Icon(if(showTargetEditor) Icons.Filled.ExpandLess else Icons.Filled.Edit, null); Spacer(Modifier.width(5.dp)); Text(if(showTargetEditor) "بستن ویرایش هدف‌ها" else "ویرایش هدف‌ها") }
                if (showTargetEditor) {
                    OutlinedTextField(target, { target = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه کوچک؛ مقدار معمول") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(specialTarget, { specialTarget = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه ویژه؛ مقدار کمتر") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(emergencyTarget, { emergencyTarget = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه اضطراری؛ کوچک‌ترین قدم") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(8.dp)); PrimaryButton("ذخیرهٔ هدف‌ها", { prefs.edit().putString("hmeta_${habit}_target",target).putString("hmeta_${habit}_special",specialTarget).putString("hmeta_${habit}_emergency",emergencyTarget).apply(); revision++; Toast.makeText(this@MainActivity,"هدف‌های عادت ذخیره شد",Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
                }
            }
            PremiumCard {
                SectionTitle("مسیر ۴۰۰ قدمی", "۱۰ لقمهٔ مستقل؛ هر لقمه ۴۰ خانهٔ شماره‌دار دارد")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    LegendPill("خالی", Color(0xFFE8ECE8)); LegendPill("✓ کوچک", Color(0xFF3D8B6E)); LegendPill("★ ویژه", Color(0xFFD6A64D)); LegendPill("! اضطراری", Color(0xFFB96865))
                }
                Spacer(Modifier.height(10.dp))
                (0 until 10).forEach { row ->
                    var rowTarget by remember(habit,row) { mutableStateOf(prefs.getString("h400_row_${habit}_$row", "") ?: "") }
                    var expanded by remember(habit,row) { mutableStateOf(prefs.getBoolean("h400_expand_${habit}_$row", false)) }
                    val filledInRow = (0 until 40).count { col -> (prefs.getString("h400_${habit}_${row*40+col}", "") ?: "").isNotEmpty() }
                    val firstNumber = row * 40 + 1
                    val lastNumber = (row + 1) * 40
                    Column(Modifier.fillMaxWidth().padding(bottom = 11.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFFBF2)).border(1.dp, Color(0xFFE7D8BB), RoundedCornerShape(20.dp)).padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(38.dp).clip(CircleShape).background(Forest), contentAlignment = Alignment.Center) { Text("${row+1}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text("لقمهٔ کوچک ${row+1}", fontSize = 15.sp, color = Forest, fontWeight = FontWeight.Bold)
                                Text("خانه‌های $firstNumber تا $lastNumber · $filledInRow از ۴۰", fontSize = 10.sp, color = Muted)
                            }
                            IconButton(onClick = { expanded = !expanded; prefs.edit().putBoolean("h400_expand_${habit}_$row", expanded).apply() }) {
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = if(expanded) "جمع‌کردن" else "نمایش چهل خانه", tint = Forest, modifier = Modifier.size(30.dp))
                            }
                        }
                        if (rowTarget.isNotBlank()) Text(rowTarget, Modifier.fillMaxWidth().padding(top = 6.dp), color = Ink, fontSize = 12.sp)
                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(rowTarget, { rowTarget = it; prefs.edit().putString("h400_row_${habit}_$row",it).apply() }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("شرح این لقمه") }, shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                (0 until 40).forEach { col ->
                                    val idx = row * 40 + col
                                    val globalNumber = idx + 1
                                    val value = prefs.getString("h400_${habit}_$idx", "") ?: ""
                                    val bg = when(value) { "s" -> Color(0xFF3D8B6E); "v" -> Color(0xFFD6A64D); "e" -> Color(0xFFB96865); else -> Color.White }
                                    Box(Modifier.size(width = 48.dp, height = 50.dp).clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, if(value.isEmpty()) Color(0xFFD5D8D1) else bg, RoundedCornerShape(12.dp)).clickable {
                                        val next = when(value) { "" -> "s"; "s" -> "v"; "v" -> "e"; else -> "" }
                                        prefs.edit().putString("h400_${habit}_$idx",next).apply(); revision++
                                    }, contentAlignment = Alignment.Center) {
                                        Text(globalNumber.toString(), color = if(value.isEmpty()) Ink else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.height(7.dp))
                            LinearProgressIndicator(progress = { filledInRow / 40f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Forest2, trackColor = PaleGold)
                        }
                    }
                }
                Text("شمارهٔ خانه‌ها در کل مسیر پیوسته است: ۱ تا ۴۰، سپس ۴۱ تا ۸۰ و به همین ترتیب تا ۴۰۰. با باز و بسته کردن هر لقمه، وضعیت خانه‌ها حفظ می‌شود.", fontSize = 11.sp, color = Muted, lineHeight = 18.sp)
            }

            PremiumCard {
                SectionTitle("یادآور اختصاصی این عادت")
                Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("یادآوری برای ${names[habit]}", fontWeight = FontWeight.Bold); Text("تنظیم مستقل برای این عادت", fontSize = 11.sp, color = Muted) }; Switch(reminderOn, { reminderOn = it }) }
                OutlinedTextField(reminderTime, { reminderTime = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ساعت (مثلاً 20:00)") }, singleLine = true)
                Spacer(Modifier.height(7.dp)); OutlinedTextField(reminderText, { reminderText = it }, modifier = Modifier.fillMaxWidth(), label = { Text("پیام یادآور") }, singleLine = true)
                Spacer(Modifier.height(8.dp)); PrimaryButton("ذخیره تنظیمات یادآور", { prefs.edit().putBoolean("habit_reminder_${habit}_on",reminderOn).putString("habit_reminder_${habit}_time",reminderTime).putString("habit_reminder_${habit}_text",reminderText).apply(); configureHabitReminder(habit,names[habit],reminderOn,reminderTime,reminderText.ifBlank { ReminderReceiver.MESSAGES[habit % ReminderReceiver.MESSAGES.size] }); revision++; Toast.makeText(this@MainActivity,if(reminderOn) "یادآور اختصاصی این عادت زمان‌بندی شد" else "یادآور اختصاصی غیرفعال شد",Toast.LENGTH_LONG).show() }, Modifier.fillMaxWidth())
            }
            PremiumCard {
                SectionTitle("یادداشت و بازتاب مسیر")
                OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("چه چیزی کمک کرد؟ چه چیزی سخت بود؟") }, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(8.dp)); PrimaryButton("ذخیره یادداشت عادت", { prefs.edit().putString("habit_note_$habit",note).apply(); revision++; Toast.makeText(this@MainActivity,"یادداشت ذخیره شد",Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { (0 until 400).forEach { prefs.edit().remove("h400_${habit}_$it").apply() }; revision++ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Filled.Refresh,null); Spacer(Modifier.width(6.dp)); Text("شروع دوبارهٔ جدول این عادت") }
            }
        }
    }

    @Composable private fun LegendPill(label: String, color: Color) {
        Row(Modifier.clip(RoundedCornerShape(9.dp)).background(color.copy(alpha=.22f)).padding(horizontal=9.dp, vertical=6.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(8.dp).clip(CircleShape).background(color)); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 10.sp, color = Forest) }
    }

    @Composable private fun DailyPage() {
        val day = selectedDay.coerceIn(1,40)
        ScrollPage {
            PremiumCard { SectionTitle("ثبت روزانه", "روز موردنظر را انتخاب کن و اعمالت را ثبت کن")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { selectedDay = (selectedDay - 1).coerceAtLeast(1) }) { Icon(Icons.Filled.ChevronRight, null) }; Text("روز $day از ۴۰", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 18.sp); OutlinedButton(onClick = { selectedDay = (selectedDay + 1).coerceAtMost(40) }) { Icon(Icons.Filled.ChevronLeft, null) } }
                Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { dayDoneCount(day) / dailyTaskItems().size.coerceAtLeast(1).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = Gold, trackColor = PaleGold)
                Text("${dayDoneCount(day)} از ${dailyTaskItems().size} مورد انجام شده", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                dailyTaskItems().forEach { item -> Row(Modifier.fillMaxWidth().clickable { setTask(day,item.id,!isTaskDone(day,item.id)) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(isTaskDone(day,item.id), { setTask(day,item.id,it) }, colors = CheckboxDefaults.colors(checkedColor = Forest)); Text(item.title, Modifier.weight(1f), fontSize = 14.sp) } }
            }
            PremiumCard { SectionTitle("محاسبهٔ نفس", "اگر لغزشی رخ داد، با مهربانی و صداقت ثبت کن")
                var slip by remember(day) { mutableStateOf(prefs.getString("slip_day_$day", "") ?: "") }
                OutlinedTextField(slip, { slip = it }, modifier = Modifier.fillMaxWidth(), label = { Text("چه چیزی را می‌خواهم اصلاح کنم؟") }, minLines = 3, shape = RoundedCornerShape(14.dp))
                Spacer(Modifier.height(8.dp)); PrimaryButton("ذخیرهٔ محاسبه", { saveSlip(day, slip); Toast.makeText(this@MainActivity, "محاسبه ذخیره شد", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
            }
            PrimaryButton("یادداشت این روز", { navigateTo("یادداشت‌ها") }, Modifier.fillMaxWidth())
        }
    }

    @Composable private fun NotesPage() {
        val day = selectedDay.coerceIn(1,40)
        var note by remember(day) { mutableStateOf(prefs.getString(noteKey(day), "") ?: "") }
        ScrollPage {
            PremiumCard { SectionTitle("یادداشت‌های روزانه", "فکرها، دعاها، تجربه‌ها و عهدهای شخصی")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { OutlinedButton(onClick = { selectedDay = (selectedDay - 1).coerceAtLeast(1) }) { Text("روز قبل") }; Text("روز $day", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold); OutlinedButton(onClick = { selectedDay = (selectedDay + 1).coerceAtMost(40) }) { Text("روز بعد") } }
                Spacer(Modifier.height(10.dp)); OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp), label = { Text("یادداشت امروز") }, placeholder = { Text("امروز چه آموختم؟ برای فردا چه عهدی دارم؟") }, shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(10.dp)); PrimaryButton("ذخیرهٔ یادداشت", { saveNote(day,note); Toast.makeText(this@MainActivity, "یادداشت ذخیره شد", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
            }
            PremiumCard { SectionTitle("یادداشت‌های ذخیره‌شده")
                val allNotes = notes()
                if (allNotes.isEmpty()) Text("هنوز یادداشتی ثبت نشده است.", color = Muted)
                allNotes.forEach { (d, text) -> Column(Modifier.fillMaxWidth().padding(vertical = 7.dp).clip(RoundedCornerShape(12.dp)).background(Cream).clickable { selectedDay = d; note = text }.padding(12.dp)) { Text("روز $d", color = Gold, fontWeight = FontWeight.Bold); Text(text, maxLines = 4, fontSize = 13.sp) } }
            }
        }
    }

    @Composable private fun HabitAnalysisTablePage() {
        val headers = listOf(
            "نام عادت", "تعداد روزهای محاسبه", "تعداد لقمه کوچک", "تعداد لقمه ویژه",
            "تعداد لقمه اضطراری", "تعداد شکست", "ویژه هنگام شکست", "اضطراری هنگام شکست",
            "ویژه بیش از حد", "اضطراری بیش از حد"
        )
        val headerColors = listOf(
            Color(0xFFF2B5AE), Color(0xFFF6D3B0), Color(0xFFB9D8B2), Color(0xFFF7DFA0),
            Color(0xFFC9CDCA), Color(0xFFBFC9E3), Color(0xFFE5B9D0), Color(0xFFF5CBA9),
            Color(0xFFBDE1DB), Color(0xFFC8C2D7)
        )
        val rowColors = listOf(Color(0xFFF6D8C4), Color(0xFFF7E6B9), Color(0xFFF2C8C7), Color(0xFFD8E9D0), Color(0xFFCDE8E3))
        ScrollPage {
            PremiumCard {
                SectionTitle("جدول تحلیل‌ها", "فرم قابل‌ویرایش مطابق الگوی ارسالی")
                Text("برای نوشتن در هر خانه، آن را لمس کن. اطلاعات هر خانه جداگانه ذخیره می‌شود.", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Spacer(Modifier.width(38.dp).height(68.dp))
                        headers.forEachIndexed { index, title ->
                            Box(Modifier.width(if (index == 0) 118.dp else 104.dp).height(68.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(headerColors[index]).padding(5.dp), contentAlignment = Alignment.Center) {
                                Text(title, color = Ink, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 13.sp)
                            }
                        }
                    }
                    (0..4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(38.dp).sizeIn(minHeight = 48.dp).clip(CircleShape).background(rowColors[row]), contentAlignment = Alignment.Center) {
                                Text(listOf("۱", "۲", "۳", "۴", "۵")[row], color = Forest, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            headers.indices.forEach { col ->
                                val key = "analysis_grid_${row}_${col}"
                                var value by remember(row, col) { mutableStateOf(prefs.getString(key, if (col == 0) habitNames().getOrNull(row) ?: "" else "") ?: "") }
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { value = it; prefs.edit().putString(key, it).apply() },
                                    modifier = Modifier.width(if (col == 0) 118.dp else 104.dp).height(58.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center, color = Ink),
                                    shape = RoundedCornerShape(9.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Forest2,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = rowColors[row].copy(alpha = .42f),
                                        unfocusedContainerColor = rowColors[row].copy(alpha = .32f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            PremiumCard {
                SectionTitle("تحلیل‌ها", "پنج بخش برای نوشتن برداشت‌ها و تصمیم‌های بعدی")
                (1..5).forEach { number ->
                    var analysisText by remember(number) { mutableStateOf(prefs.getString("habit_analysis_note_$number", "") ?: "") }
                    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(14.dp)).background(rowColors[number - 1].copy(alpha = .25f)).padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(32.dp).clip(CircleShape).background(rowColors[number - 1]), contentAlignment = Alignment.Center) { Text(number.toString(), color = Forest, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(8.dp))
                            Text("تحلیل شمارهٔ $number", color = Forest, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(7.dp))
                        OutlinedTextField(
                            value = analysisText,
                            onValueChange = { analysisText = it; prefs.edit().putString("habit_analysis_note_$number", it).apply() },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 105.dp),
                            placeholder = { Text("اینجا بنویسید…") },
                            minLines = 3,
                            shape = RoundedCornerShape(11.dp)
                        )
                    }
                }
                Text("نوشته‌ها به‌صورت خودکار در گوشی ذخیره می‌شوند.", color = Muted, fontSize = 11.sp)
            }
        }
    }

    @Composable private fun ReportsPage() {
        val totalTasks = (1..40).sumOf { dayDoneCount(it) }; val totalPossible = (40 * dailyTaskItems().size).coerceAtLeast(1)
        val habitDone = habitNames().indices.sumOf { h -> (0 until 400).count { prefs.getString("h400_${h}_$it", "") != "" } }
        val smallCount = habitNames().indices.sumOf { h -> (0 until 400).count { prefs.getString("h400_${h}_$it", "") == "s" } }
        val specialCount = habitNames().indices.sumOf { h -> (0 until 400).count { prefs.getString("h400_${h}_$it", "") == "v" } }
        val emergencyCount = habitNames().indices.sumOf { h -> (0 until 400).count { prefs.getString("h400_${h}_$it", "") == "e" } }
        val allCells = habitNames().size * 400
        ScrollPage {
            PremiumCard { SectionTitle("گزارش مسیر", "تصویری از استمرار و قدم‌های برداشته‌شده")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatTile("${if(allCells>0) habitDone * 100 / allCells else 0}%", "پیشرفت عادت‌ها", Modifier.weight(1f)); StatTile("${completedDays()}", "روز کامل چله", Modifier.weight(1f)); StatTile("${habitDone}", "خانه ثبت‌شده", Modifier.weight(1f)) }
                Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatTile("$smallCount", "لقمه کوچک", Modifier.weight(1f)); StatTile("$specialCount", "لقمه ویژه", Modifier.weight(1f)); StatTile("$emergencyCount", "لقمه اضطراری", Modifier.weight(1f)) }
                Spacer(Modifier.height(16.dp)); Text("پیشرفت چله", fontWeight = FontWeight.Bold, color = Forest); LinearProgressIndicator(progress = { completedDays() / 40f }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(CircleShape), color = Gold, trackColor = PaleGold)
                Spacer(Modifier.height(14.dp)); Text("ثبت روزانه", fontWeight = FontWeight.Bold, color = Forest); LinearProgressIndicator(progress = { totalTasks / totalPossible.toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(8.dp).clip(CircleShape), color = Forest2, trackColor = PaleGold)
            }
            PremiumCard { SectionTitle("گزارش روز به روز")
                (1..40).forEach { day -> val count = dayDoneCount(day); Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Text("روز $day", Modifier.width(62.dp), fontSize = 12.sp, color = Muted); LinearProgressIndicator(progress = { count / dailyTaskItems().size.coerceAtLeast(1).toFloat() }, modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape), color = if (dailyTaskItems().isNotEmpty() && count == dailyTaskItems().size) Forest else Gold, trackColor = PaleGold); Text("$count/${dailyTaskItems().size}", Modifier.width(48.dp), textAlign = TextAlign.End, fontSize = 11.sp) } }
            }
        }
    }

    @Composable private fun RemindersPage() {
        var enabled by remember { mutableStateOf(reminderEnabled) }; var hour by remember { mutableIntStateOf(reminderHour) }; var minute by remember { mutableIntStateOf(reminderMinute) }
        ScrollPage {
            PremiumCard { SectionTitle("یادآور بومی اندروید", "اعلان مستقل از باز بودن برنامه زمان‌بندی می‌شود")
                Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("یادآوری روزانهٔ عهد", fontWeight = FontWeight.Bold); Text("یک پیام برای بازگشت به نیت روزانه", color = Muted, fontSize = 12.sp) }; Switch(enabled, { enabled = it }) }
                Spacer(Modifier.height(16.dp)); Text("زمان یادآوری", color = Forest, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { IconButton(onClick = { hour = (hour + 23) % 24 }) { Icon(Icons.Filled.Remove, null) }; Text(String.format(Locale.US, "%02d", hour), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Forest); Text(":", fontSize = 30.sp); Text(String.format(Locale.US, "%02d", minute), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Forest); IconButton(onClick = { hour = (hour + 1) % 24 }) { Icon(Icons.Filled.Add, null) } }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { TextButton(onClick = { minute = (minute + 55) % 60 }) { Text("−۵ دقیقه") }; TextButton(onClick = { minute = (minute + 5) % 60 }) { Text("+۵ دقیقه") } }
                Spacer(Modifier.height(8.dp)); PrimaryButton("ذخیره و فعال‌سازی", { configureReminder(enabled,hour,minute); Toast.makeText(this@MainActivity, if (enabled) "یادآور ذخیره شد" else "یادآور غیرفعال شد", Toast.LENGTH_LONG).show() }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { showTestNotification() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.NotificationsActive, null); Spacer(Modifier.width(8.dp)); Text("آزمایش فوری اعلان") }
                Spacer(Modifier.height(6.dp)); OutlinedButton(onClick = { scheduleTestNotification() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.Alarm, null); Spacer(Modifier.width(8.dp)); Text("آزمایش اعلان زمان‌بندی‌شده (حدود یک دقیقه بعد)") }
                Spacer(Modifier.height(6.dp)); TextButton(onClick = { openNotificationSettings() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Settings, null); Spacer(Modifier.width(6.dp)); Text("بررسی مجوز و تنظیمات اعلان") }
            }
            PremiumCard {
                SectionTitle("عیب‌یابی واقعی اعلان")
                val nm = getSystemService(NotificationManager::class.java)
                val appEnabled = nm.areNotificationsEnabled()
                val channelEnabled = Build.VERSION.SDK_INT < 26 || (nm.getNotificationChannel(ReminderReceiver.CHANNEL)?.importance ?: NotificationManager.IMPORTANCE_NONE) != NotificationManager.IMPORTANCE_NONE
                val power = getSystemService(android.os.PowerManager::class.java)
                val batteryFree = Build.VERSION.SDK_INT < 23 || (try { power.isIgnoringBatteryOptimizations(packageName) } catch (_: SecurityException) { false })
                Text("اعلان‌های برنامه: ${if (appEnabled) "روشن" else "خاموش"}", color = if (appEnabled) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("کانال اعلان: ${if (channelEnabled) "فعال" else "خاموش"}", color = if (channelEnabled) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("محدودیت باتری: ${if (batteryFree) "مستثنا از بهینه‌سازی" else "ممکن است محدود باشد"}", color = if (batteryFree) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) } }, modifier = Modifier.fillMaxWidth()) { Text("بازکردن تنظیمات محدودیت باتری") }
                Text("اول «آزمایش فوری اعلان» را بزن. اگر رسید، بعد «آزمایش زمان‌بندی‌شده» را بزن. فوری‌رسیدن و نرسیدن زمان‌بندی‌شده، مسیر ایراد را مشخص می‌کند.", fontSize = 11.sp, color = Muted, lineHeight = 17.sp)
            }
            PremiumCard { SectionTitle("نکتهٔ مهم")
                Text("در بعضی گوشی‌ها علاوه بر مجوز اعلان، اجرای خودکار و فعالیت پس‌زمینه باید در تنظیمات مدیریت باتری گوشی فعال باشد.", fontSize = 13.sp, color = Muted)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 30) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                showTestNotification()
            } else {
                Toast.makeText(this, "برای نمایش اعلان آزمایشی، مجوز اعلان را در تنظیمات برنامه فعال کن.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun scheduleTestNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 30)
            Toast.makeText(this, "ابتدا مجوز اعلان را فعال کن و دوباره آزمایش را بزن.", Toast.LENGTH_LONG).show()
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled() || (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(ReminderReceiver.CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE)) {
            Toast.makeText(this, "اعلان برنامه یا کانال آن خاموش است؛ تنظیمات باز می‌شود.", Toast.LENGTH_LONG).show()
            openNotificationSettings()
            return
        }
        val intent = Intent(this, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_TEST)
        val pending = PendingIntent.getBroadcast(this, 2030, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ReminderReceiver.scheduleAlarm(this, System.currentTimeMillis() + 60_000L, pending)
        requestExactAlarmAccessIfNeeded()
        Toast.makeText(this, "آزمایش زمان‌بندی ثبت شد؛ حدود یک دقیقه دیگر باید اعلان آزمایشی برسد.", Toast.LENGTH_LONG).show()
    }

    private fun openNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= 26) {
                Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, ReminderReceiver.CHANNEL)
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
            }
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
    }

    private fun showTestNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 30)
            Toast.makeText(this, "برای آزمایش، مجوز اعلان را در پنجرهٔ اندروید فعال کن.", Toast.LENGTH_LONG).show()
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        if (!manager.areNotificationsEnabled() || (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel(ReminderReceiver.CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE)) {
            Toast.makeText(this, "اعلان‌های برنامه یا کانال آن خاموش است؛ تنظیمات اعلان باز می‌شود.", Toast.LENGTH_LONG).show()
            openNotificationSettings()
            return
        }
        val message = ReminderReceiver.MESSAGES[(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 3) % ReminderReceiver.MESSAGES.size]
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, ReminderReceiver.CHANNEL) else Notification.Builder(this).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
        val notification = builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ساختن یار · پیام امروز")
            .setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_REMINDER).setAutoCancel(true).build()
        manager.notify(2029, notification)
        Toast.makeText(this, "اعلان آزمایشی ارسال شد؛ اگر دیده نشد تنظیمات اعلان و مزاحم‌نشدن را بررسی کن.", Toast.LENGTH_LONG).show()
    }

    @Composable private fun SettingsPage() {
        ScrollPage {
            PremiumCard { SectionTitle("تنظیمات و پشتیبان‌گیری", "اطلاعات شخصی روی همین دستگاه نگهداری می‌شود")
                SettingRow(Icons.Filled.FileUpload, "خروجی پشتیبان", "ذخیرهٔ اطلاعات در فایل JSON") { exportLauncher.launch("chillehyar-backup.json") }
                Divider(color = PaleGold)
                SettingRow(Icons.Filled.FileDownload, "بازیابی پشتیبان", "انتخاب فایل JSON قبلی") { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }
                Divider(color = PaleGold)
                SettingRow(Icons.Filled.Notifications, "تنظیم یادآورها", "زمان‌بندی اعلان‌های روزانه") { navigateTo("یادآورها") }
                Divider(color = PaleGold)
                SettingRow(Icons.Filled.Insights, "گزارش پیشرفت", "مشاهدهٔ روند ثبت‌ها") { navigateTo("گزارش") }
            }
            PremiumCard {
                SectionTitle("مدیریت چک‌لیست روزانه", "متن گزینه‌ها را عوض کن، گزینهٔ تازه بساز یا موارد دلخواه را حذف کن")
                var editableTasks by remember { mutableStateOf(dailyTaskItems()) }
                var newTask by remember { mutableStateOf("") }
                editableTasks.forEachIndexed { position, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 7.dp)) {
                        OutlinedTextField(item.title, { updated -> editableTasks = editableTasks.toMutableList().also { it[position] = item.copy(title = updated) } }, modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp), label = { Text("گزینه ${position+1}") })
                        IconButton(onClick = { editableTasks = editableTasks.filterNot { it.id == item.id } }) { Icon(Icons.Filled.DeleteOutline, "حذف", tint = Rose) }
                    }
                }
                OutlinedTextField(newTask, { newTask = it }, modifier = Modifier.fillMaxWidth(), label = { Text("گزینهٔ جدید") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(7.dp))
                OutlinedButton(onClick = {
                    val title = newTask.trim()
                    if (title.isNotEmpty()) {
                        val nextId = maxOf(prefs.getInt("daily_task_next_id", 6), (editableTasks.maxOfOrNull { it.id } ?: 5) + 1)
                        prefs.edit().putInt("daily_task_next_id", nextId + 1).apply()
                        editableTasks = editableTasks + DailyChecklistItem(nextId, title); newTask = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(5.dp)); Text("افزودن گزینه") }
                Spacer(Modifier.height(7.dp))
                PrimaryButton("ذخیره چک‌لیست", { saveDailyTaskItems(editableTasks); Toast.makeText(this@MainActivity,"چک‌لیست ذخیره شد",Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
            }
            PremiumCard { SectionTitle("شخصی‌سازی ظاهر", "رنگ و اندازهٔ نوشته‌ها را متناسب با چشم خود تنظیم کن")
                val colors = listOf(Color(0xFF173A31), Color(0xFF176B55), Color(0xFF2F6FBA), Color(0xFF7B61A8), Color(0xFFB96865), Color(0xFF946C2E))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) { colors.forEachIndexed { i,c -> Box(Modifier.size(38.dp).clip(CircleShape).background(c).border(if(themeChoice==i) 3.dp else 0.dp, Gold, CircleShape).clickable { themeChoice=i; prefs.edit().putInt("theme_choice",i).apply(); revision++ }) } }
                Spacer(Modifier.height(12.dp)); Text("اندازهٔ متن", color = Forest, fontWeight = FontWeight.Bold)
                Slider(value = uiScale, onValueChange = { uiScale=it }, valueRange = .9f..1.2f, steps = 5)
                Text("${(uiScale*100).toInt()}٪ · مقیاس نوشته‌ها در همین برنامه اعمال می‌شود.", color = Muted, fontSize = 11.sp)
                PrimaryButton("ذخیرهٔ ظاهر", { prefs.edit().putInt("theme_choice",themeChoice).putFloat("ui_scale",uiScale).apply(); Toast.makeText(this@MainActivity,"تنظیمات ظاهر ذخیره شد",Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
            }
            PremiumCard { SectionTitle("دربارهٔ ساختن یار")
                Text("ساختن یار · نسخهٔ بومی اندروید ۳۸", color = Forest, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp)); Text(APP_TAGLINE + "\nبرای همراهی در یک مسیر چهل‌روزه از عهد، مراقبت و رشد معنوی.", color = Muted, fontSize = 13.sp, lineHeight = 21.sp)
            }
        }
    }
    @Composable private fun ToolsPage() {
        ScrollPage {
            PremiumCard { SectionTitle("ابزارهای پیشرفتهٔ ساختن یار", "امکانات تکمیلی نسخهٔ ۲۹ در قالب رابط بومی اندروید")
                ToolRow("📋", "جدول الف و فضیلت‌ها", "ثبت روزانه در ۱۱ دسته", { navigateTo("جدول فضیلت‌ها") })
                Divider(color = PaleGold)
                ToolRow("🌱", "چک‌لیست افزایش انگیزه", "۱۶ عامل انگیزشی با سابقهٔ روزانه", { navigateTo("افزایش انگیزه") })
                Divider(color = PaleGold)
                ToolRow("🧭", "راهکارهای بهتر شدن", "ثبت و مرور راهکارهای عملی برای ادامه مسیر", { navigateTo("تحلیل لغزش") })
                Divider(color = PaleGold)
                ToolRow("📊", "جدول تحلیل عادت‌ها", "مقایسهٔ لقمه‌ها، روزهای ثبت‌شده و شاخص‌های هر عادت", { navigateTo("جدول تحلیل‌ها") })
                Divider(color = PaleGold)
                ToolRow("📈", "گزارش و کارنامه", "خانه‌های کوچک، ویژه، اضطراری و پیشرفت هر عادت", { navigateTo("گزارش") })
                Divider(color = PaleGold)
                ToolRow("🔔", "یادآورها", "یادآور روزانه و آزمایش اعلان اندروید", { navigateTo("یادآورها") })
                Divider(color = PaleGold)
                ToolRow("🎨", "ظاهر و پشتیبان", "رنگ‌بندی، خروجی و بازیابی اطلاعات", { navigateTo("تنظیمات") })
            }
            PremiumCard { SectionTitle("یادآوری دربارهٔ اطلاعات")
                Text("اطلاعات این نسخه در حافظهٔ محلی برنامه ذخیره می‌شود. برای انتقال به گوشی دیگر یا پیشگیری از ازدست‌رفتن داده‌ها، از بخش تنظیمات مرتباً نسخهٔ پشتیبان JSON بگیر.", color = Muted, fontSize = 13.sp, lineHeight = 22.sp)
            }
        }
    }
    @Composable private fun ToolRow(emoji: String, title: String, subtitle: String, action: () -> Unit) {
        Row(Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(PaleGold), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 23.sp) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = Forest, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 11.sp, lineHeight = 16.sp) }; Icon(Icons.Filled.ChevronLeft, null, tint = Gold)
        }
    }

    @Composable private fun VirtuePage() {
        val dateKey = selectedVirtueDate.replace("/", "_").replace(" ", "")
        ScrollPage {
            PremiumCard { SectionTitle("جدول الف و فضیلت‌ها", "ثبت موضوعات روزانه در ۱۱ دسته")
                OutlinedTextField(selectedVirtueDate, { selectedVirtueDate = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تاریخ (شمسی)") }, singleLine = true)
                Spacer(Modifier.height(8.dp)); Text("هر کادر را برای همان تاریخ تکمیل کن؛ اطلاعات خودکار ذخیره می‌شود.", fontSize = 12.sp, color = Muted)
            }
            virtueHeaders.forEachIndexed { i, label ->
                var value by remember(dateKey, i) { mutableStateOf(prefs.getString("virtue_${dateKey}_$i", "") ?: "") }
                PremiumCard {
                    Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(PaleGold), contentAlignment = Alignment.Center) { Text("${i+1}", color = Forest, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(9.dp)); Text(label, color = Forest, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(value, { value = it; prefs.edit().putString("virtue_${dateKey}_$i",it).apply() }, modifier = Modifier.fillMaxWidth(), minLines = 2, placeholder = { Text("ثبت روزانه در دستهٔ $label…") }, shape = RoundedCornerShape(12.dp))
                }
            }
            OutlinedButton(onClick = { virtueHeaders.indices.forEach { prefs.edit().remove("virtue_${dateKey}_$it").apply() }; revision++ }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("پاک‌کردن جدول این تاریخ") }
        }
    }

    @Composable private fun MotivationPage() {
        val dateKey = motivationDateDraft.replace("/", "_").replace(" ", "")
        val checked = remember(dateKey, revision) { motivationItems.indices.filter { prefs.getBoolean("mot_${dateKey}_$it", false) }.toSet() }
        var titleDraft by remember(dateKey) { mutableStateOf(prefs.getString("mot_title_$dateKey", motivationTitleDraft) ?: motivationTitleDraft) }
        ScrollPage {
            PremiumCard { SectionTitle("چک‌لیست افزایش انگیزه", "۱۶ عامل برای زنده نگه‌داشتن انگیزه")
                OutlinedTextField(titleDraft, { titleDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("عنوان ثبت") }, singleLine = true)
                Spacer(Modifier.height(8.dp)); OutlinedTextField(motivationDateDraft, { motivationDateDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("تاریخ ثبت (شمسی)") }, singleLine = true)
                Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("پیشرفت امروز", color = Forest, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${checked.size} از ${motivationItems.size}", color = Gold, fontWeight = FontWeight.Bold) }
                LinearProgressIndicator(progress = { checked.size / motivationItems.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 7.dp).height(7.dp).clip(CircleShape), color = Gold, trackColor = PaleGold)
            }
            motivationItems.forEachIndexed { i, item ->
                val isChecked = prefs.getBoolean("mot_${dateKey}_$i", false)
                Row(Modifier.fillMaxWidth().padding(bottom = 7.dp).clip(RoundedCornerShape(14.dp)).background(if(isChecked) Color(0xFFE6F2E9) else CardCream).clickable { prefs.edit().putBoolean("mot_${dateKey}_$i",!isChecked).putString("mot_title_$dateKey",titleDraft).apply(); revision++ }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(isChecked, { prefs.edit().putBoolean("mot_${dateKey}_$i",it).putString("mot_title_$dateKey",titleDraft).apply(); revision++ }, colors = CheckboxDefaults.colors(checkedColor = Forest)); Text(item, Modifier.weight(1f), color = if(isChecked) Forest else Ink, fontSize = 13.sp); if(isChecked) Icon(Icons.Filled.CheckCircle, null, tint = Forest)
                }
            }
            PrimaryButton("ثبت چک‌لیست این روز در سابقه", { prefs.edit().putString("mot_title_$dateKey",titleDraft).putString("mot_saved_${dateKey}",checked.joinToString(",")).apply(); Toast.makeText(this@MainActivity,"چک‌لیست ذخیره شد",Toast.LENGTH_SHORT).show(); revision++ }, Modifier.fillMaxWidth())
            PremiumCard { SectionTitle("سابقهٔ ثبت‌ها")
                val history = prefs.all.keys.filter { it.startsWith("mot_saved_") }.sortedDescending()
                if(history.isEmpty()) Text("هنوز سابقه‌ای ثبت نشده است.", color = Muted) else history.forEach { key ->
                    val k = key.removePrefix("mot_saved_"); val title = prefs.getString("mot_title_$k", "چک‌لیست انگیزشی") ?: "چک‌لیست انگیزشی"; val count = (prefs.getString(key, "") ?: "").split(",").count { it.isNotBlank() }
                    Text("$title · ${k.replace("_", "/")} · $count مورد", modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp), color = Forest, fontSize = 12.sp)
                }
            }
        }
    }

    @Composable private fun TacticsPage() {
        ScrollPage {
            PremiumCard { SectionTitle("راهکارهای بهتر شدن", "به‌جای سرزنش، برای قدم بعدی راهکار بنویس")
                OutlinedTextField(tacticNameDraft, { tacticNameDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام عادت یا موقعیت") }, singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(tacticSolutionDraft, { tacticSolutionDraft = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, label = { Text("راهکار عملی من برای ادامهٔ مسیر") })
                Spacer(Modifier.height(9.dp)); PrimaryButton("ذخیرهٔ راهکار", {
                    if(tacticNameDraft.isBlank() || tacticSolutionDraft.isBlank()) Toast.makeText(this@MainActivity,"نام عادت و راهکار را وارد کن",Toast.LENGTH_SHORT).show()
                    else { val id = System.currentTimeMillis(); prefs.edit().putString("tactic_$id", JSONObject().put("name",tacticNameDraft).put("solution",tacticSolutionDraft).put("date",SimpleDateFormat("yyyy/MM/dd",Locale.getDefault()).format(Date())).toString()).apply(); tacticNameDraft=""; tacticSolutionDraft=""; revision++; Toast.makeText(this@MainActivity,"راهکار ذخیره شد",Toast.LENGTH_SHORT).show() }
                }, Modifier.fillMaxWidth())
            }
            PremiumCard { SectionTitle("راهکارهای ذخیره‌شده")
                val records = prefs.all.filterKeys { it.startsWith("tactic_") }.toList().sortedByDescending { it.first }.mapNotNull { (key, raw) ->
                    val json = raw as? String ?: return@mapNotNull null
                    val parsed = try { JSONObject(json) } catch (_: Exception) { null }; parsed?.let { key to it }
                }
                if(records.isEmpty()) Text("هنوز راهکاری ثبت نشده است.", color = Muted)
                records.forEach { (key,o) ->
                    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(13.dp)).background(Cream).padding(12.dp)) {
                        Text(o.optString("name"), color = Forest, fontWeight = FontWeight.Bold)
                        Text(o.optString("date"), color = Muted, fontSize = 11.sp)
                        Text(o.optString("solution"), fontSize = 13.sp, lineHeight = 20.sp)
                        TextButton(onClick = { prefs.edit().remove(key).apply(); revision++ }) { Text("حذف راهکار", color = Rose) }
                    }
                }
            }
        }
    }

    @Composable private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(PaleGold), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Forest) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted, fontSize = 11.sp) }; Icon(Icons.Filled.ChevronLeft, null, tint = Gold) } }
}

class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("chillehyar_native_v30", Context.MODE_PRIVATE)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "یادآورهای ساختن یار", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            }
            manager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (intent?.action == ACTION_TEST) {
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
            val message = "گیرندهٔ آلارم اجرا شد و اعلان زمان‌بندی‌شده به سیستم تحویل داده شد."
            manager.notify(2030, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ساختن یار · آزمایش زمان‌بندی").setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_REMINDER).setAutoCancel(true).build())
            return
        }
        if (intent?.action == "ir.chillehyar.mahdi.HABIT_REMINDER") {
            val index = intent.getIntExtra("habit_index",0)
            if (!prefs.getBoolean("habit_reminder_${index}_on", false)) return
            val name = intent.getStringExtra("habit_name") ?: "عادت روزانه"
            val message = intent.getStringExtra("habit_message")?.takeIf { it.isNotBlank() } ?: MESSAGES[(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + index) % MESSAGES.size]
            val time = intent.getStringExtra("habit_time") ?: "20:00"
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
            manager.notify(10000 + index, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ساختن یار · $name").setContentText(message).setCategory(Notification.CATEGORY_REMINDER).setAutoCancel(true).build())
            scheduleHabit(context,index,name,time,message)
            return
        }
        if (!prefs.getBoolean("reminder_enabled", false)) return
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
        val message = MESSAGES[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % MESSAGES.size]
        manager.notify(4002, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("ساختن یار · یادآوری مسیر").setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_REMINDER).setAutoCancel(true).build())
        schedule(context, prefs)
    }
    companion object {
        const val CHANNEL = "sanat_yar_reminders_v38"
        const val REQUEST_CODE = 730
        const val ACTION_TEST = "ir.chillehyar.mahdi.TEST_REMINDER"
        fun scheduleAlarm(context: Context, triggerAtMillis: Long, pendingIntent: PendingIntent) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            if (Build.VERSION.SDK_INT >= 31) {
                if (alarm.canScheduleExactAlarms()) {
                    alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    // Keep a best-effort reminder even when Android's special exact-alarm access is denied.
                    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= 23) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= 19) {
                alarm.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
        val MESSAGES = listOf(
            "امروز را با سلامی به امام زمان (عج) آغاز کن؛ یک قدم کوچک کافی است.",
            "اللهم عجل لولیک الفرج؛ امروز یک کار خوب را به نیت رضایت خدا انجام بده.",
            "یار امام زمان بودن از یک انتخاب پاک در همین لحظه آغاز می‌شود.",
            "آیا امروز برای فرج دعا کردی؟ چند لحظه با دل حاضر دعا کن.",
            "امروز دل کسی را شاد کن؛ شاید همین قدم کوچک، عهدی روشن باشد.",
            "برای تعجیل فرج دعا کن و سهم خودت از اصلاح را فراموش نکن.",
            "یک صلوات، یک دعای فرج، یک کار نیک؛ عهد امروزت را زنده نگه دار.",
            "امام زمان (عج) را با عمل صالح به یاد بیاور؛ قدمی هرچند کوچک بردار.",
            "خدایا ما را از یاران راستین و زمینه‌سازان فرج قرار بده.",
            "امروز با مهربانی، امانت‌داری و دوری از گناه به عهدت وفا کن.",
            "دعای عهد را با توجه بخوان؛ امید را در رفتار امروزت جاری کن.",
            "فرج را بخواه و برای بهتر شدن خودت هم یک تصمیم عملی بگیر.",
            "هر قدم صادقانه در مسیر بندگی ارزشمند است؛ ناامید نشو.",
            "به نیت تعجیل فرج، به خانواده‌ات محبت کن و حق کسی را رعایت کن.",
            "سلام بر امام زمان (عج)؛ امروز را با نیت پاک و امید آغاز کن."
        )
        fun schedule(context: Context, prefs: android.content.SharedPreferences) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            val pi = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            if (!prefs.getBoolean("reminder_enabled", false)) { alarm.cancel(pi); return }
            val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, prefs.getInt("reminder_hour", 8)); set(Calendar.MINUTE, prefs.getInt("reminder_minute", 0)); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
            scheduleAlarm(context, c.timeInMillis, pi)
        }
        fun scheduleHabit(context: Context, index: Int, name: String, time: String, message: String) {
            val p = context.getSharedPreferences("chillehyar_native_v30", Context.MODE_PRIVATE)
            if (!p.getBoolean("habit_reminder_${index}_on", false)) return
            val parts = time.split(":"); val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0,23) ?: 20; val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0,59) ?: 0
            val intent = Intent(context, ReminderReceiver::class.java).apply { action = "ir.chillehyar.mahdi.HABIT_REMINDER"; putExtra("habit_index",index); putExtra("habit_name",name); putExtra("habit_time",time); putExtra("habit_message",message) }
            val pi = PendingIntent.getBroadcast(context,10000+index,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY,hour); set(Calendar.MINUTE,minute); set(Calendar.SECOND,0); set(Calendar.MILLISECOND,0); if(timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR,1) }
            val alarm = context.getSystemService(AlarmManager::class.java)
            scheduleAlarm(context, c.timeInMillis, pi)
        }
    }
}

class BootReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED || intent?.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            val prefs = context.getSharedPreferences("chillehyar_native_v30", Context.MODE_PRIVATE)
            ReminderReceiver.schedule(context, prefs)
            val defaults = listOf("قرائت قرآن", "خوش‌اخلاقی", "دعا برای فرج")
            val extras = (prefs.getString("custom_habits", "") ?: "").split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            (defaults + extras).forEachIndexed { index, name ->
                if (prefs.getBoolean("habit_reminder_${index}_on", false)) ReminderReceiver.scheduleHabit(context,index,name,prefs.getString("habit_reminder_${index}_time","20:00") ?: "20:00",prefs.getString("habit_reminder_${index}_text","") ?: "یک قدم کوچک امروز هم ارزشمند است.")
            }
        }
    }
}
