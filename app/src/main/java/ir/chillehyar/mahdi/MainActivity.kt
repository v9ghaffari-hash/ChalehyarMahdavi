package ir.chillehyar.mahdi

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private val Forest = Color(0xFF0B3B34)
private val Forest2 = Color(0xFF0E8068)
private val Gold = Color(0xFFD99A27)
private val Cream = Color(0xFFF3F0E6)
private val CardCream = Color(0xFFFFFEF9)
private val Ink = Color(0xFF17332F)
private val Muted = Color(0xFF687A70)
private val PaleGold = Color(0xFFF7E6BC)
private val Rose = Color(0xFFC45A62)
private const val APP_NAME = "قدم یار"
private const val APP_TAGLINE = "در مسیر یار شدن برای امام زمان (عج)"
private val habitIconOptions = listOf(
    "📖", "🕌", "🕋", "📿", "🤲", "🌙", "⭐", "🕊️", "💚", "🌱",
    "🧎", "🧭", "💧", "🏃", "📚", "🧠", "⏰", "🧹", "🚭", "📵",
    "🍬", "🎮", "😤", "🗣️", "👀", "🍃", "🪥", "💤", "🍎", "💪",
    "🧘", "🕯️", "🌿", "🌹", "☀️", "🛡️", "🧡", "🧎‍♂️", "🪴", "🎯"
)
private val habitColorOptions = listOf(
    "#176B55", "#245B83", "#80613B", "#8C5A72", "#68804A", "#A65E45",
    "#586A9C", "#83704C", "#477D7A", "#6C5A8D", "#9B7650", "#64736D"
)
private data class DailyChecklistItem(val id: Int, val title: String)
private data class MotivationItem(val id: Int, val title: String, val icon: String)
private data class ChillaData(
    val id: Int,
    val title: String,
    val goal: String,
    val intention: String,
    val startMillis: Long,
    val icon: String,
    val colorHex: String,
    val archived: Boolean = false,
    val imageFile: String = ""
)
private val qualityLabels = listOf("ضعیف", "متوسط", "خوب", "عالی")
private val qualityKeys = listOf("weak", "medium", "good", "excellent")
private val qualityColors = listOf(Color(0xFFB66E68), Color(0xFFD5A84F), Color(0xFF5D9A7B), Color(0xFF26765D))
private val LocalPageScrollState = compositionLocalOf<ScrollState?> { null }

private val defaultHabits = listOf("قرائت قرآن", "نماز اول وقت", "ذکر و دعا")
private val dailyTasks = listOf("نمازهایم را با توجه خواندم", "قرآن و دعا خواندم", "برای امام زمان(عج) دعا کردم", "از گناه و عادت نامطلوب دوری کردم", "به دیگران نیکی کردم", "محاسبهٔ نفس انجام دادم")
private val habitTasks = listOf(
    listOf("قرآن صبحگاهی", "ترجمهٔ آیات", "تدبر در آیات", "حفظ یک آیه", "تلاوت روزانه", "سورهٔ عهد", "آیت‌الکرسی", "هدیهٔ ثواب", "عمل به یک آیه", "مرور آموخته‌ها"),
    listOf("نماز صبح", "نماز ظهر", "نماز عصر", "نماز مغرب", "نماز عشا", "نماز اول وقت", "حضور قلب", "تعقیبات", "نماز مستحبی", "محاسبهٔ نماز"),
    listOf("صلوات روزانه", "دعای فرج", "دعای عهد", "ذکر استغفار", "ذکر یا مهدی", "صدقه برای سلامتی", "یاد امام زمان", "زیارت آل‌یاسین", "دعا برای مؤمنان", "تجدید عهد"),
    listOf("کنترل زبان", "کنترل نگاه", "ترک غیبت", "ترک دروغ", "کنترل خشم", "ترک اسراف", "کنترل فضای مجازی", "دوری از بدگمانی", "حفظ حق‌الناس", "توبه و جبران"),
    listOf("کمک به خانواده", "احوال‌پرسی", "دستگیری نیازمند", "لبخند و خوش‌رفتاری", "انتقال خیر", "رسیدگی به والدین", "رفع کدورت", "کار خیر پنهانی", "رعایت حق دیگران", "نیت خدمت")
)


private data class JalaliParts(val year: Int, val month: Int, val day: Int)
private val jalaliMonthNames = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
private val persianWeekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
private fun latinDigits(value: String): String = value.map { ch ->
    when (ch) {
        in '۰'..'۹' -> ('0'.code + ch.code - '۰'.code).toChar()
        in '٠'..'٩' -> ('0'.code + ch.code - '٠'.code).toChar()
        else -> ch
    }
}.joinToString("")
private fun persianDigits(value: String): String = value.map { ch -> if (ch in '0'..'9') ('۰'.code + ch.code - '0'.code).toChar() else ch }.joinToString("")
private fun jalaliToGregorianMillis(jy: Int, jm: Int, jd: Int): Long {
    var jdn = 365 * (jy - 979) + ((jy - 979) / 33) * 8 + (((jy - 979) % 33 + 3) / 4)
    jdn += if (jm <= 6) (jm - 1) * 31 else 186 + (jm - 7) * 30
    jdn += jd - 1
    var gdn = jdn + 79
    var gy = 1600 + 400 * (gdn / 146097)
    gdn %= 146097
    var leap = true
    if (gdn >= 36525) {
        gdn--
        gy += 100 * (gdn / 36524)
        gdn %= 36524
        if (gdn >= 365) gdn++ else leap = false
    }
    gy += 4 * (gdn / 1461)
    gdn %= 1461
    if (gdn >= 366) {
        leap = false
        gdn--
        gy += gdn / 365
        gdn %= 365
    }
    val monthLengths = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var gm = 0
    while (gm < 12 && gdn >= monthLengths[gm]) { gdn -= monthLengths[gm]; gm++ }
    val gd = gdn + 1
    return Calendar.getInstance().apply { clear(); set(gy, gm, gd, 12, 0, 0) }.timeInMillis
}
private fun gregorianToJalali(millis: Long): JalaliParts {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    val gy = c.get(Calendar.YEAR); val gm = c.get(Calendar.MONTH) + 1; val gd = c.get(Calendar.DAY_OF_MONTH)
    val monthDays = intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334)
    val gy0 = gy - 1600
    var gdn = 365 * gy0 + (gy0 + 3) / 4 - (gy0 + 99) / 100 + (gy0 + 399) / 400
    gdn += monthDays[gm - 1] + gd - 1
    if (gm > 2 && (gy % 400 == 0 || (gy % 4 == 0 && gy % 100 != 0))) gdn++
    var jdn = gdn - 79
    val cycle = jdn / 12053
    jdn %= 12053
    var jy = 979 + 33 * cycle + 4 * (jdn / 1461)
    jdn %= 1461
    if (jdn >= 366) { jy += (jdn - 1) / 365; jdn = (jdn - 1) % 365 }
    val jm: Int
    val jd: Int
    if (jdn < 186) { jm = 1 + jdn / 31; jd = 1 + jdn % 31 }
    else { jdn -= 186; jm = 7 + jdn / 30; jd = 1 + jdn % 30 }
    return JalaliParts(jy, jm, jd)
}
private fun isJalaliLeap(year: Int): Boolean =
    (jalaliToGregorianMillis(year + 1, 1, 1) - jalaliToGregorianMillis(year, 1, 1)) > 365L * 86_400_000L
private fun jalaliMonthLength(year: Int, month: Int): Int = when {
    month <= 6 -> 31
    month <= 11 -> 30
    isJalaliLeap(year) -> 30
    else -> 29
}
private fun formatJalaliDate(millis: Long): String {
    val p = gregorianToJalali(millis)
    return persianDigits("%04d/%02d/%02d".format(Locale.US, p.year, p.month, p.day))
}
private fun parseJalaliText(value: String): Long? {
    val parts = latinDigits(value).trim().split('/', '-', '.')
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    if (y !in 1200..1600 || m !in 1..12 || d !in 1..jalaliMonthLength(y, m)) return null
    return jalaliToGregorianMillis(y, m, d)
}

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("chillehyar_native_v30", MODE_PRIVATE) }
    private var revision by mutableIntStateOf(0)
    private var page by mutableStateOf("خانه")
    private val pageBackStack = mutableListOf<String>()
    private var customDateDraft by mutableStateOf("")
    private var selectedDay by mutableIntStateOf(1)
    private var selectedChillaId by mutableIntStateOf(-1)
    private var selectedChillaDay by mutableIntStateOf(1)
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
    private var selectedVirtueDate by mutableStateOf("")
    private var motivationTitleDraft by mutableStateOf("کارهای انگیزشی امروز")
    private var motivationDateDraft by mutableStateOf("")
    private var tacticNameDraft by mutableStateOf("")
    private var tacticSolutionDraft by mutableStateOf("")
    private var themeChoice by mutableIntStateOf(0)
    private var openCreateFormOnArrival by mutableStateOf(false)
    private var iconPickerTarget by mutableIntStateOf(-1)
    private var pendingHabitIconFile by mutableStateOf("")
    private var uiScale by mutableFloatStateOf(1f)
    private val virtueHeaders = listOf("واجب", "حرام", "مستحب", "مکروه", "اخلاق", "سلامتی", "شغلی", "تفریحی", "خانواده", "علمی")
    private val defaultMotivationItems = listOf(
        "یادآوری اهداف" to "🎯", "تفکر" to "💭", "کتاب" to "📚", "کلاس" to "🏫",
        "کلاس اخلاق و موعظه" to "🌿", "سخنرانی" to "🎙️", "آیات قرآن" to "📖", "احادیث" to "💬",
        "دعا" to "🤲", "توسل" to "🕋", "ارتباط با امام زمان (عج)" to "💚", "اشک بر امام حسین (ع)" to "🕯️",
        "تلقین مثبت" to "✨", "ارتباط با افراد موفق" to "🤝", "همیار" to "🫂", "کمک به دیگران" to "❤️"
    )

    private fun loadMotivationItems(): List<MotivationItem> {
        val raw = prefs.getString("motivation_items_v40_json", null)
        if (raw.isNullOrBlank()) return defaultMotivationItems.mapIndexed { index, pair -> MotivationItem(index, pair.first, pair.second) }
        return try {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { i ->
                val obj = json.optJSONObject(i) ?: return@mapNotNull null
                val title = obj.optString("title").trim()
                if (title.isBlank()) null else MotivationItem(obj.optInt("id", i), title, obj.optString("icon", "✨").ifBlank { "✨" })
            }
        } catch (_: Exception) {
            defaultMotivationItems.mapIndexed { index, pair -> MotivationItem(index, pair.first, pair.second) }
        }
    }

    private fun saveMotivationItems(items: List<MotivationItem>) {
        val json = JSONArray()
        items.forEach { item -> json.put(JSONObject().put("id", item.id).put("title", item.title).put("icon", item.icon)) }
        prefs.edit().putString("motivation_items_v40_json", json.toString())
            .putInt("motivation_next_id", maxOf(prefs.getInt("motivation_next_id", 16), (items.maxOfOrNull { it.id } ?: -1) + 1)).apply()
        revision++
    }

    private fun loadChillas(): List<ChillaData> {
        val raw = prefs.getString("chilla_records_v44_json", null) ?: return emptyList()
        return try {
            val json = JSONArray(raw)
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optInt("id", -1)
                val title = item.optString("title").trim()
                if (id < 0 || title.isBlank()) null else ChillaData(
                    id = id,
                    title = title,
                    goal = item.optString("goal", ""),
                    intention = item.optString("intention", ""),
                    startMillis = item.optLong("startMillis", System.currentTimeMillis()),
                    icon = item.optString("icon", "🌙").ifBlank { "🌙" },
                    colorHex = item.optString("colorHex", "#176B55").ifBlank { "#176B55" },
                    archived = item.optBoolean("archived", false),
                    imageFile = item.optString("imageFile", "")
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveChillas(items: List<ChillaData>) {
        val json = JSONArray()
        items.forEach { item ->
            json.put(JSONObject()
                .put("id", item.id).put("title", item.title).put("goal", item.goal)
                .put("intention", item.intention).put("startMillis", item.startMillis)
                .put("icon", item.icon).put("colorHex", item.colorHex).put("archived", item.archived)
                .put("imageFile", item.imageFile))
        }
        prefs.edit().putString("chilla_records_v44_json", json.toString()).apply()
        revision++
    }

    private fun chillaElapsedDays(chilla: ChillaData): Int {
        // Normalize local calendar dates to UTC midnights, avoiding daylight-saving boundary errors.
        val dateKey = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val startDay = dateKey.parse(dateKey.format(Date(chilla.startMillis)))?.time ?: chilla.startMillis
        val todayDay = dateKey.parse(dateKey.format(Date(System.currentTimeMillis())))?.time ?: System.currentTimeMillis()
        return ((todayDay - startDay) / 86_400_000L).toInt().plus(1).coerceIn(0, 41)
    }

    private fun activeChilla(): ChillaData? {
        val id = prefs.getInt("chilla_active_id", -1)
        return loadChillas().firstOrNull { it.id == id && !it.archived && chillaElapsedDays(it) <= 40 }
    }

    private fun archiveExpiredChilla() {
        val activeId = prefs.getInt("chilla_active_id", -1)
        val active = loadChillas().firstOrNull { it.id == activeId && !it.archived } ?: return
        if (chillaElapsedDays(active) > 40) {
            saveChillas(loadChillas().map { if (it.id == active.id) it.copy(archived = true) else it })
            prefs.edit().putInt("chilla_active_id", -1).apply()
        }
    }

    private fun chillaQuality(id: Int, day: Int): String = prefs.getString("chilla_${id}_quality_$day", "") ?: ""
    private fun saveChillaQuality(id: Int, day: Int, quality: String) {
        val record = loadChillas().firstOrNull { it.id == id } ?: return
        if (day !in 1..chillaElapsedDays(record).coerceAtMost(40) || quality !in qualityKeys) return
        prefs.edit().putString("chilla_${id}_quality_$day", quality).apply(); revision++
    }
    private fun chillaQualityCounts(id: Int): List<Int> = qualityKeys.map { key -> (1..40).count { chillaQuality(id, it) == key } }
    private fun chillaRecordedCount(id: Int): Int = chillaQualityCounts(id).sum()
    private fun setChillaActive(id: Int) {
        val target = loadChillas().firstOrNull { it.id == id && !it.archived } ?: return
        if (chillaElapsedDays(target) > 40) return
        prefs.edit().putInt("chilla_active_id", id).apply(); revision++
    }
    private fun archiveChilla(id: Int) {
        saveChillas(loadChillas().map { if (it.id == id) it.copy(archived = true) else it })
        if (prefs.getInt("chilla_active_id", -1) == id) prefs.edit().putInt("chilla_active_id", -1).apply()
        revision++
    }

    private fun totalCompletedHabitCells(): Int = habitNames().indices.sumOf { h ->
        (0 until 400).count { prefs.getString("h400_${h}_$it", "")?.isNotEmpty() == true }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        archiveExpiredChilla()
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
        // Keep the Android status bar visible on every page, including the home screen.
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = android.graphics.Color.rgb(243, 240, 230)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.navigationBarColor = android.graphics.Color.rgb(243, 240, 230)
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        setContent { ChillehyarApp() }
    }

    // Re-arm reminders after returning from system special-access settings.
    override fun onResume() {
        super.onResume()
        archiveExpiredChilla()
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
        val normalized = latinDigits(value.trim())
        val year = normalized.substringBefore('/').toIntOrNull() ?: 0
        // V37/V38 stored Gregorian text; retain compatibility and migrate it visually to Jalali.
        if (year >= 1900) return try { SimpleDateFormat("yyyy/MM/dd", Locale.US).apply { isLenient = false }.parse(normalized)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
        return parseJalaliText(normalized) ?: System.currentTimeMillis()
    }
    private fun displayHabitDate(value: String): String {
        if (value.isBlank()) return todayDateText()
        val normalized = latinDigits(value)
        val year = normalized.substringBefore('/').toIntOrNull() ?: 0
        return if (year >= 1900) formatJalaliDate(parseHabitDate(normalized)) else persianDigits(normalized)
    }
    private fun todayDateText(): String = formatJalaliDate(System.currentTimeMillis())
    private fun defaultHabitEmoji(index: Int): String = when (index) {
        0 -> "📖"
        1 -> "🕌"
        2 -> "📿"
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
            val channel = NotificationChannel(ReminderReceiver.CHANNEL, "یادآورهای قدم یار · نسخه ۴۴", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "یادآور روزانه و مسیر قدم یار"
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                vibrationPattern = longArrayOf(0, 650, 300, 650)
                enableLights(true)
                lightColor = android.graphics.Color.rgb(195, 154, 82)
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
    private val habitIconLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2048) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
                val source = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: error("تصویر باز نشد")
                val ratio = minOf(1f, 512f / maxOf(source.width, source.height).toFloat())
                val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(source, (source.width * ratio).toInt().coerceAtLeast(1), (source.height * ratio).toInt().coerceAtLeast(1), true) else source
                val filename = "habit_symbol_${System.currentTimeMillis()}.png"
                openFileOutput(filename, MODE_PRIVATE).use { scaled.compress(Bitmap.CompressFormat.PNG, 95, it) }
                if (scaled !== source) scaled.recycle()
                source.recycle()
                pendingHabitIconFile = filename
                revision++
                Toast.makeText(this, "نماد دلخواه انتخاب شد؛ برای ثبت نهایی ذخیرهٔ عادت را بزن.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) { Toast.makeText(this, "بارگذاری تصویر انجام نشد: ${e.message}", Toast.LENGTH_LONG).show() }
        }
    }
    private fun pickHabitIcon(target: Int) {
        iconPickerTarget = target
        habitIconLauncher.launch(arrayOf("image/*"))
    }
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
        val palette = listOf(Color(0xFF0B3B34), Color(0xFF0E8068), Color(0xFF2F6FBA), Color(0xFF7B61A8), Color(0xFFB84F63), Color(0xFFB87924))
        val chosenPrimary = palette[themeChoice.coerceIn(0, palette.lastIndex)]
        val density = LocalDensity.current
        val pageScrollStates = remember { mutableMapOf<String, ScrollState>() }
        val scrollKey = when (page) {
            "جزئیات عادت" -> "$page-$selectedHabit"
            "جزئیات چله" -> "$page-$selectedChillaId"
            else -> page
        }
        val activeScrollState = pageScrollStates.getOrPut(scrollKey) { ScrollState(prefs.getInt("page_scroll_$scrollKey", 0)) }
        SideEffect {
            // Keep clock, signal and battery indicators visible throughout the app.
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.statusBarColor = android.graphics.Color.rgb(243, 240, 230)
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.navigationBarColor = android.graphics.Color.rgb(243, 240, 230)
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        }
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
                            // Internal pages use their own section titles; avoid repeating the crossed-out app-name header.
                            when (page) {
                                "خانه" -> HomePage()
                                "چله من", "چله‌ها" -> ChillaPage()
                                "جزئیات چله" -> ChillaDetailPage()
                                "عادت‌ها" -> HabitsPage()
                                "ثبت روزانه" -> DailyPage()
                                "جزئیات عادت" -> HabitDetailPage()
                                "یادداشت‌ها" -> NotesPage()
                                "گزارش" -> ReportsPage()
                                "یادآورها" -> RemindersPage()
                                "ابزارهای پیشرفته" -> ToolsPage()
                                "جدول الف" -> VirtuePage()
                                "چک‌لیست انگیزه" -> MotivationPage()
                                "تاکتیک‌های شیطان" -> TacticsPage()
                                "جدول تحلیل‌ها" -> HabitAnalysisTablePage()
                                else -> SettingsPage()
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable private fun BottomNavigationBar(selected: String, onSelect: (String) -> Unit) {
        val items = listOf(
            Triple("خانه", Icons.Filled.Home, "خانه"),
            Triple("عادت‌ها", Icons.Filled.CheckCircle, "عادت‌ها"),
            Triple("تحلیل", Icons.Filled.Insights, "جدول تحلیل‌ها"),
            Triple("تنظیمات", Icons.Filled.Settings, "تنظیمات")
        )
        Surface(color = Color.Transparent, tonalElevation = 0.dp, shadowElevation = 12.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFFEFA), Color(0xFFE7EFE7))))
                    .border(1.dp, Color(0xFFB9CCBE), RoundedCornerShape(25.dp))
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (label, icon, target) ->
                    val isSelected = when (label) {
                        "عادت‌ها" -> selected == "عادت‌ها" || selected == "جزئیات عادت"
                        "تحلیل" -> selected == "جدول تحلیل‌ها"
                        else -> selected == target
                    }
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(19.dp))
                            .clickable { onSelect(target) }
                            .background(if (isSelected) Brush.verticalGradient(listOf(Color(0xFF14745D), Color(0xFF073D35))) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)))
                            .padding(vertical = 4.dp, horizontal = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(icon, contentDescription = label, tint = if (isSelected) Color(0xFFFFE7AC) else Color(0xFF28735E), modifier = Modifier.size(if (isSelected) 21.dp else 19.dp))
                        Text(label, color = if (isSelected) Color.White else Forest, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        Box(Modifier.width(23.dp).height(2.dp).clip(CircleShape).background(if (isSelected) Color(0xFFE6B64E) else Color.Transparent))
                    }
                }
            }
        }
    }

    @Composable private fun ScrollPage(content: @Composable ColumnScope.() -> Unit) {
        val rememberedPageScroll = LocalPageScrollState.current ?: rememberScrollState()
        Column(Modifier.fillMaxSize().verticalScroll(rememberedPageScroll).padding(16.dp), content = content)
    }
    @Composable private fun PremiumCard(content: @Composable ColumnScope.() -> Unit) { Card(Modifier.fillMaxWidth().padding(bottom = 11.dp).border(1.2.dp, Color(0xFFB9CFC2), RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CardCream), elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)) { Column(Modifier.background(Brush.verticalGradient(listOf(Color(0xFFFFFEFA), Color(0xFFF5F7EF)))).padding(16.dp), content = content) } }
    @Composable private fun SectionTitle(title: String, subtitle: String? = null) { Column(Modifier.padding(bottom = 11.dp)) { Text(title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = Forest); if (subtitle != null) Text(subtitle, fontSize = 12.sp, color = Muted, lineHeight = 18.sp) } }
    @Composable private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = PaleGold)) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Forest, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center) } } }
    @Composable private fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) { Button(onClick, modifier = modifier.heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Color(0xFFFFF4D7)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)) { Text(text, fontWeight = FontWeight.SemiBold) } }

    @Composable private fun PersianDateField(value: String, onValueChange: (String) -> Unit, label: String) {
        var showPicker by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = displayHabitDate(value), onValueChange = {}, readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
            label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(14.dp),
            trailingIcon = { IconButton(onClick = { showPicker = true }) { Icon(Icons.Filled.CalendarMonth, "انتخاب تاریخ شمسی", tint = Forest2) } }
        )
        if (showPicker) {
            val initial = gregorianToJalali(parseHabitDate(value.ifBlank { todayDateText() }))
            var year by remember { mutableIntStateOf(initial.year) }
            var month by remember { mutableIntStateOf(initial.month) }
            var day by remember { mutableIntStateOf(initial.day.coerceAtMost(jalaliMonthLength(initial.year, initial.month))) }
            val firstDayMillis = jalaliToGregorianMillis(year, month, 1)
            val offset = Calendar.getInstance().apply { timeInMillis = firstDayMillis }.get(Calendar.DAY_OF_WEEK) % 7
            val monthLength = jalaliMonthLength(year, month)
            AlertDialog(
                onDismissRequest = { showPicker = false },
                title = { Text("انتخاب تاریخ شمسی", color = Forest, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { year--; day = day.coerceAtMost(jalaliMonthLength(year, month)) }) { Icon(Icons.Filled.ChevronRight, null) }
                            Text(persianDigits(year.toString()), fontSize = 19.sp, color = Forest, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { year++; day = day.coerceAtMost(jalaliMonthLength(year, month)) }) { Icon(Icons.Filled.ChevronLeft, null) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            IconButton(onClick = { month = if (month == 1) 12 else month - 1; if (month == 12) year--; day = day.coerceAtMost(jalaliMonthLength(year, month)) }) { Icon(Icons.Filled.ChevronRight, null) }
                            Text(jalaliMonthNames[month - 1], modifier = Modifier.width(110.dp), textAlign = TextAlign.Center, color = Forest, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { month = if (month == 12) 1 else month + 1; if (month == 1) year++; day = day.coerceAtMost(jalaliMonthLength(year, month)) }) { Icon(Icons.Filled.ChevronLeft, null) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) { persianWeekDays.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
                        for (week in 0 until 6) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                for (column in 0 until 7) {
                                    val n = week * 7 + column - offset + 1
                                    if (n in 1..monthLength) {
                                        Box(Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(if (n == day) Forest else Cream).clickable { day = n }, contentAlignment = Alignment.Center) {
                                            Text(persianDigits(n.toString()), color = if (n == day) Color.White else Ink, fontWeight = if (n == day) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                        }
                                    } else Box(Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                        Text("${jalaliMonthNames[month - 1]} ${persianDigits(year.toString())} · روز ${persianDigits(day.toString())}", color = Muted, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                },
                confirmButton = { TextButton(onClick = { onValueChange(persianDigits("%04d/%02d/%02d".format(Locale.US, year, month, day))); showPicker = false }) { Text("تأیید", color = Forest) } },
                dismissButton = { TextButton(onClick = { showPicker = false }) { Text("انصراف") } },
                containerColor = CardCream
            )
        }
    }

    @Composable private fun HomePage() {
        val names = habitNames()
        val active = activeChilla()
        val rememberedScroll = LocalPageScrollState.current ?: rememberScrollState()
        BoxWithConstraints(Modifier.fillMaxSize().background(Cream)) {
            val heroHeight = (maxWidth / 2.35f).coerceIn(150.dp, 235.dp)
            val side = (maxWidth * 0.035f).coerceIn(10.dp, 16.dp)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberedScroll),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Keep the approved hero artwork unchanged; the framed image sits below the visible status bar.
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = side).padding(top = 8.dp)
                        .height(heroHeight + 6.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0B5948), Color(0xFFD5A642), Color(0xFF073B33))))
                        .border(1.6.dp, Color(0xFF2B8A6C), RoundedCornerShape(25.dp))
                        .padding(3.dp)
                        .clip(RoundedCornerShape(21.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.home_hero),
                        contentDescription = "رهرو جوان در مسیر نور، پیکرهٔ پوشیده با شال سبز و سایهٔ شیطان",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0x22101B17), Color.Transparent, Color(0x160C2C23)))))
                    Row(
                        Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xD91A493B)).border(1.dp, Gold, CircleShape).clickable { navigateTo("تنظیمات") }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Settings, "تنظیمات", tint = Color(0xFFFFE9B9), modifier = Modifier.size(19.dp))
                        }
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Color(0xD91A493B)).border(1.dp, Gold, CircleShape).clickable { navigateTo("یادآورها") }, contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Notifications, "یادآورها", tint = Color(0xFFFFE9B9), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Compact active-challenge header. The ring is elapsed-day progress; bars alone show quality counts.
                if (active != null) {
                    val elapsed = chillaElapsedDays(active).coerceIn(0, 40)
                    val counts = chillaQualityCounts(active.id)
                    val recorded = counts.sum()
                    val challengeColor = try { Color(android.graphics.Color.parseColor(active.colorHex)) } catch (_: Exception) { Forest2 }
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = side)
                            .border(1.3.dp, Color(0xFFC9A04A), RoundedCornerShape(20.dp))
                            .clickable {
                                selectedChillaId = active.id
                                selectedChillaDay = elapsed.coerceIn(1, 40)
                                navigateTo("جزئیات چله")
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF3)),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            // Header is now a separate, higher row; challenge artwork sits beneath it.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text("چلهٔ فعال", color = Color(0xFFB48731), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(active.title, color = Forest, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                                }
                                TextButton(onClick = { navigateTo("چله‌ها") }, contentPadding = PaddingValues(horizontal = 3.dp, vertical = 0.dp)) {
                                    Text("همهٔ چله‌ها", color = Forest2, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Filled.ChevronLeft, null, tint = Gold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(3.dp))
                            Row(Modifier.fillMaxWidth().height(78.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                HabitSymbol(
                                    active.icon, active.imageFile,
                                    Modifier.size(35.dp).clip(RoundedCornerShape(11.dp)).background(challengeColor.copy(alpha = .16f)),
                                    20.sp
                                )
                                Column(Modifier.weight(0.95f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        if (elapsed == 0) "شروع از ${formatJalaliDate(active.startMillis)}"
                                        else "روز ${persianDigits(elapsed.toString())} از ۴۰",
                                        color = Forest, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1
                                    )
                                    Text(
                                        if (elapsed == 0) "هنوز آغاز نشده" else "${persianDigits((40 - elapsed).coerceAtLeast(0).toString())} روز باقی‌مانده",
                                        color = Muted, fontSize = 9.sp, maxLines = 1
                                    )
                                }
                                ElapsedDaysRing(elapsed, Modifier.size(62.dp))
                                Box(Modifier.width(1.dp).height(56.dp).background(Color(0x66D4A13D)))
                                QualityBarChart(counts, Modifier.weight(1.75f).fillMaxHeight())
                            }
                            Text("روزهای ارزیابی‌شده: ${persianDigits(recorded.toString())}", color = Forest2, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = side).border(1.3.dp, Color(0xFFC9A04A), RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF3))
                    ) {
                        Row(Modifier.fillMaxWidth().clickable { navigateTo("چله‌ها") }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🌙", fontSize = 25.sp); Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) { Text("چلهٔ فعالی نداری", color = Forest, fontWeight = FontWeight.Bold); Text("یک مسیر ۴۰روزه برای خودت بساز", color = Muted, fontSize = 10.sp) }
                            Text("ساخت چله", color = Forest2, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Filled.ChevronLeft, null, tint = Gold)
                        }
                    }
                }

                Card(
                    Modifier.fillMaxWidth().padding(horizontal = side).border(1.4.dp, Color(0xFFDAB963), RoundedCornerShape(25.dp)),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEF8)),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(horizontal = 10.dp, vertical = 11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("عادت‌های من", color = Forest, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                Text("قدم‌های کوچک و پیوسته، مسیر تو را می‌سازند", color = Muted, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { openCreateFormOnArrival = true; navigateTo("عادت‌ها") },
                                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Color(0xFFFFF4D7)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(17.dp), tint = Color(0xFFFFE6AD))
                                Spacer(Modifier.width(3.dp)); Text("ایجاد عادت", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        names.forEachIndexed { index, name ->
                            val count = (0 until 400).count { prefs.getString("h400_${index}_$it", "")?.isNotEmpty() == true }
                            val ratio = count / 400f
                            val accent = habitColor(index)
                            Row(
                                Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(19.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFFFEFC8), Color(0xFFE8D19B))))
                                    .border(1.dp, Color(0xFFD5A33C), RoundedCornerShape(19.dp))
                                    .clickable { selectedHabit = index; navigateTo("جزئیات عادت") }
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(Icons.Filled.ChevronLeft, null, tint = Forest2, modifier = Modifier.size(18.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(name, color = Forest, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1)
                                    Text("${persianDigits(count.toString())} از ۴۰۰ قدم", color = Muted, fontSize = 9.sp)
                                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape), color = accent, trackColor = Color(0xFFE4D6B5))
                                }
                                Box(Modifier.size(43.dp).clip(CircleShape).background(Color(0xFFFFFDF6)).border(1.2.dp, Color(0xFFC9A04A), CircleShape), contentAlignment = Alignment.Center) {
                                    Text("${(ratio * 100).toInt()}٪", color = Forest, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                HabitSymbol(
                                    prefs.getString("hmeta_${index}_emoji", defaultHabitEmoji(index)) ?: defaultHabitEmoji(index),
                                    prefs.getString("hmeta_${index}_icon_file", "") ?: "",
                                    Modifier.size(50.dp).clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha = .15f)),
                                    28.sp
                                )
                            }
                        }
                    }
                }

                // Compact editorial-style miniatures replace the oversized image panels.
                Row(Modifier.fillMaxWidth().padding(horizontal = side), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniFeatureCard(R.drawable.feature_table, "جدول الف", "قدم‌های روشن زندگی", { navigateTo("جدول الف") }, Modifier.weight(1f))
                    MiniFeatureCard(R.drawable.feature_tactics, "تاکتیک‌های شیطان", "شناخت دام‌ها و راه مقابله", { navigateTo("تاکتیک‌های شیطان") }, Modifier.weight(1f))
                }
                Box(Modifier.padding(horizontal = side)) {
                    FeatureTile(R.drawable.feature_motivation, "چک‌لیست انگیزه", "با انگیزه باش؛ قدم‌هایت را مستمرتر بردار", { navigateTo("چک‌لیست انگیزه") }, Modifier.fillMaxWidth(), darkText = true, wide = true)
                }
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = side, vertical = 1.dp).border(1.2.dp, Color(0xFFB8CCBE), RoundedCornerShape(23.dp)),
                    shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF6E9C9)), elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFFE4F0E7), Color(0xFFFFF0CF), Color(0xFFE6F1E9)))).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("پیام امروز", color = Forest, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Text("به یاد ما باشید، که ما نیز به یاد شما هستیم...", color = Forest2, fontSize = 11.sp, lineHeight = 18.sp, textAlign = TextAlign.Center)
                            Text("با هر قدم کوچک، به امام زمان (عج) نزدیک‌تر می‌شوی.", color = Muted, fontSize = 9.sp, textAlign = TextAlign.Center)
                        }
                        Text("🌿", fontSize = 34.sp)
                    }
                }
                Spacer(Modifier.height(5.dp))
            }
        }
    }

    @Composable private fun MiniFeatureCard(
        imageRes: Int, title: String, subtitle: String, action: () -> Unit, modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.height(76.dp).clickable(onClick = action),
            shape = RoundedCornerShape(17.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E9)),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                Modifier.fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(Color(0xFFE8F2EA), Color(0xFFFFEAC0))))
                    .border(1.dp, Color(0xFFD9BD78), RoundedCornerShape(17.dp))
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier.size(49.dp).clip(RoundedCornerShape(13.dp))
                        .background(Color(0xFFE2EFE5)).border(1.dp, Color(0xFF9EBBAA), RoundedCornerShape(13.dp)).padding(2.dp)
                ) {
                    Image(painter = painterResource(imageRes), contentDescription = title, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.End) {
                    Text(title, color = Forest, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Text(subtitle, color = Forest2, fontSize = 8.sp, lineHeight = 11.sp, maxLines = 2)
                }
                Icon(Icons.Filled.ChevronLeft, null, tint = Color(0xFFB38A39), modifier = Modifier.size(16.dp))
            }
        }
    }

    @Composable private fun QuickAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit, modifier: Modifier = Modifier) { OutlinedButton(onClick = action, modifier = modifier, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9CCBE)), contentPadding = PaddingValues(6.dp)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Gold); Text(label, fontSize = 11.sp, color = Forest) } } }

    @Composable private fun FeatureTile(
        imageRes: Int, title: String, subtitle: String, action: () -> Unit,
        modifier: Modifier = Modifier, darkText: Boolean = true, wide: Boolean = false
    ) {
        Card(
            modifier = modifier.padding(bottom = 10.dp).height(if (wide) 92.dp else 132.dp).clickable(onClick = action),
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkText) Color(0xFFE4F1E7) else Forest),
            elevation = CardDefaults.cardElevation(5.dp)
        ) {
            if (wide) {
                Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(imageRes), contentDescription = title, modifier = Modifier.width(94.dp).fillMaxHeight(), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).fillMaxHeight().background(Brush.horizontalGradient(listOf(Color(0xFFE4F1E7), Color(0xFFFFE9BD)))).padding(horizontal = 12.dp, vertical = 9.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text(title, color = Forest, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(subtitle, color = Forest2, fontSize = 10.sp, lineHeight = 15.sp, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Text("مدیریت فهرست  ‹", color = Forest, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize()) {
                    Image(painter = painterResource(imageRes), contentDescription = title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(if (darkText) Brush.verticalGradient(listOf(Color.Transparent, Color(0xCCF4EBD9))) else Brush.verticalGradient(listOf(Color.Transparent, Color(0xE610332A)))))
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(title, color = if (darkText) Forest else Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Text(subtitle, color = if (darkText) Forest2 else Color(0xFFE8F0E8), fontSize = 9.sp, lineHeight = 13.sp, maxLines = 2, textAlign = TextAlign.Center)
                        Text("مشاهده  ‹", color = if (darkText) Forest else Color(0xFFFFE6B2), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable private fun ChillaPage() {
        val chillas = loadChillas().sortedByDescending { it.startMillis }
        val active = activeChilla()
        var showCreate by remember { mutableStateOf(false) }
        var title by remember { mutableStateOf("") }
        var goal by remember { mutableStateOf("") }
        var intention by remember { mutableStateOf("") }
        var date by remember { mutableStateOf(todayDateText()) }
        var icon by remember { mutableStateOf("🌙") }
        var colorHex by remember { mutableStateOf(habitColorOptions[0]) }
        val pickedChillaImage = if (iconPickerTarget == -2) pendingHabitIconFile else ""
        ScrollPage {
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { SectionTitle("چله‌های من", "مسیرهای مستقل ۴۰روزه؛ هر چله گزارش و بایگانی جداگانه دارد") }
                    Button(onClick = { showCreate = !showCreate }, shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Color(0xFFFFF4D7)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
                        Icon(if (showCreate) Icons.Filled.Close else Icons.Filled.Add, null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(5.dp)); Text(if (showCreate) "بستن" else "چلهٔ جدید", fontSize = 12.sp)
                    }
                }
                Text("هر چله هدف، نیت، تاریخ شروع و آمار کیفیت مخصوص خودش را دارد. فقط چلهٔ فعال در صفحهٔ خانه نمایش داده می‌شود.", color = Muted, fontSize = 11.sp, lineHeight = 18.sp)
            }
            if (showCreate) {
                PremiumCard {
                    SectionTitle("ساخت چلهٔ تازه", "عنوان و نیتت را مشخص کن؛ سپس برای هر روز کیفیت مسیر را ثبت کن")
                    OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("عنوان چله") }, singleLine = true, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(goal, { goal = it }, modifier = Modifier.fillMaxWidth(), label = { Text("هدف یا توضیح چله") }, minLines = 2, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(intention, { intention = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نیت و دلیل انتخاب این مسیر") }, minLines = 2, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(8.dp))
                    PersianDateField(date, { date = it }, "تاریخ شروع شمسی")
                    Spacer(Modifier.height(10.dp))
                    Text("نماد چله", color = Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HabitSymbol(icon, pickedChillaImage, Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(PaleGold), 25.sp)
                        OutlinedButton(onClick = { pickHabitIcon(-2) }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.AddPhotoAlternate, null, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("انتخاب تصویر")
                        }
                        if (pickedChillaImage.isNotBlank()) TextButton(onClick = { pendingHabitIconFile = ""; iconPickerTarget = -1 }) { Text("حذف تصویر", color = Rose, fontSize = 10.sp) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        listOf("🌙", "🤲", "📿", "🕌", "💚", "🌱", "⭐", "🕊️").forEach { option ->
                            Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (icon == option) PaleGold else Cream).border(if (icon == option) 2.dp else 1.dp, if (icon == option) Gold else Color(0xFFE8DDC8), RoundedCornerShape(12.dp)).clickable { icon = option }, contentAlignment = Alignment.Center) { Text(option, fontSize = 21.sp) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("رنگ چله", color = Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    habitColorOptions.chunked(6).forEach { rowColors ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowColors.forEach { option ->
                                val swatch = try { Color(android.graphics.Color.parseColor(option)) } catch (_: Exception) { Forest2 }
                                Box(Modifier.weight(1f).aspectRatio(1f).clip(CircleShape).background(swatch).border(if (colorHex == option) 3.dp else 1.dp, if (colorHex == option) Gold else Color.White, CircleShape).clickable { colorHex = option }, contentAlignment = Alignment.Center) {
                                    if (colorHex == option) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(17.dp))
                                }
                            }
                            repeat(6 - rowColors.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("ایجاد چله و فعال‌کردن آن", {
                        val cleanTitle = title.trim()
                        if (cleanTitle.isBlank()) {
                            Toast.makeText(this@MainActivity, "برای چله یک عنوان بنویس.", Toast.LENGTH_SHORT).show()
                        } else {
                            val nextId = prefs.getInt("chilla_next_id_v44", 1)
                            val newItem = ChillaData(
                                id = nextId, title = cleanTitle, goal = goal.trim(), intention = intention.trim(),
                                startMillis = parseHabitDate(date.ifBlank { todayDateText() }), icon = icon,
                                colorHex = colorHex, archived = false, imageFile = pickedChillaImage
                            )
                            saveChillas(loadChillas() + newItem)
                            if (iconPickerTarget == -2) { pendingHabitIconFile = ""; iconPickerTarget = -1 }
                            prefs.edit().putInt("chilla_next_id_v44", nextId + 1).putInt("chilla_active_id", nextId).apply()
                            selectedChillaId = nextId
                            selectedChillaDay = chillaElapsedDays(newItem).coerceIn(1, 40)
                            title = ""; goal = ""; intention = ""; date = todayDateText(); icon = "🌙"; colorHex = habitColorOptions[0]
                            showCreate = false
                            navigateTo("جزئیات چله")
                        }
                    }, Modifier.fillMaxWidth())
                }
            }
            if (active != null) {
                val activeDays = chillaElapsedDays(active)
                val recorded = chillaRecordedCount(active.id)
                val color = try { Color(android.graphics.Color.parseColor(active.colorHex)) } catch (_: Exception) { Forest2 }
                PremiumCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HabitSymbol(active.icon, active.imageFile, Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(color.copy(alpha = .13f)), 27.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("چلهٔ فعال", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(active.title, color = Forest, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(if (activeDays == 0) "شروع در ${formatJalaliDate(active.startMillis)}" else "روز ${persianDigits(activeDays.toString())} از ۴۰", color = Muted, fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.CheckCircle, null, tint = color, modifier = Modifier.size(23.dp))
                    }
                    Spacer(Modifier.height(9.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile(persianDigits(recorded.toString()), "روزهای ثبت‌شده", Modifier.weight(1f))
                        StatTile(persianDigits((40 - activeDays).coerceAtLeast(0).toString()), "روز باقی‌مانده", Modifier.weight(1f))
                        StatTile(persianDigits((activeDays - recorded).coerceAtLeast(0).toString()), "روز بدون ارزیابی", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    PrimaryButton("ورود به چله و ثبت کیفیت روزها", { selectedChillaId = active.id; selectedChillaDay = activeDays.coerceIn(1, 40); navigateTo("جزئیات چله") }, Modifier.fillMaxWidth())
                }
            } else {
                PremiumCard {
                    SectionTitle("هنوز چلهٔ فعالی نداری", "می‌توانی چند چله بسازی و هر زمان یکی را فعال کنی")
                    Text("چله‌های تمام‌شده در بایگانی می‌مانند و آمارشان حذف نمی‌شود.", color = Muted, fontSize = 12.sp)
                    PrimaryButton("ساخت اولین چله", { showCreate = true }, Modifier.fillMaxWidth())
                }
            }
            val others = chillas.filter { it.id != active?.id && !it.archived }
            if (others.isNotEmpty()) {
                PremiumCard {
                    SectionTitle("چله‌های دیگر", "برای ادامه، یکی را دوباره فعال کن")
                    others.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            HabitSymbol(item.icon, item.imageFile, Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(PaleGold), 23.sp)
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) { Text(item.title, color = Forest, fontWeight = FontWeight.Bold); Text("شروع: ${formatJalaliDate(item.startMillis)}", color = Muted, fontSize = 10.sp) }
                            TextButton(onClick = { setChillaActive(item.id); selectedChillaId = item.id; selectedChillaDay = chillaElapsedDays(item).coerceIn(1, 40) }) { Text("فعال‌سازی", color = Forest2, fontSize = 11.sp) }
                            IconButton(onClick = { selectedChillaId = item.id; selectedChillaDay = chillaElapsedDays(item).coerceIn(1, 40); navigateTo("جزئیات چله") }) { Icon(Icons.Filled.ChevronLeft, null, tint = Gold) }
                        }
                        Divider(color = PaleGold)
                    }
                }
            }
            val archived = chillas.filter { it.archived }
            if (archived.isNotEmpty()) {
                PremiumCard {
                    SectionTitle("بایگانی چله‌ها", "چله‌های پایان‌یافته همراه با آمار و یادداشت‌هایشان محفوظ‌اند")
                    archived.forEach { item ->
                        Row(Modifier.fillMaxWidth().clickable { selectedChillaId = item.id; selectedChillaDay = chillaElapsedDays(item).coerceIn(1, 40); navigateTo("جزئیات چله") }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            HabitSymbol(item.icon, item.imageFile, Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(PaleGold), 23.sp); Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) { Text(item.title, color = Forest, fontWeight = FontWeight.Bold); Text("${persianDigits(chillaRecordedCount(item.id).toString())} روز ارزیابی‌شده", color = Muted, fontSize = 10.sp) }
                            Icon(Icons.Filled.ChevronLeft, null, tint = Gold)
                        }
                    }
                }
            }
        }
    }

    @Composable private fun ChillaDetailPage() {
        val chilla = loadChillas().firstOrNull { it.id == selectedChillaId }
        if (chilla == null) {
            ScrollPage { PremiumCard { SectionTitle("چله پیدا نشد"); PrimaryButton("بازگشت به چله‌ها", { navigateBack() }, Modifier.fillMaxWidth()) } }
            return
        }
        val elapsed = chillaElapsedDays(chilla)
        val availableDays = elapsed.coerceIn(0, 40)
        val selectedDaySafe = selectedChillaDay.coerceIn(1, 40)
        val selectedQualityInitial = chillaQuality(chilla.id, selectedDaySafe)
        val counts = chillaQualityCounts(chilla.id)
        val recordedDays = (1..40).filter { chillaQuality(chilla.id, it).isNotBlank() }
        var note by remember(chilla.id, selectedDaySafe) { mutableStateOf(prefs.getString("chilla_${chilla.id}_note_$selectedDaySafe", "") ?: "") }
        val weekSummaries = (0 until 6).map { week ->
            val from = week * 7 + 1
            val to = minOf(40, from + 6)
            val weekValues = (from..to).map { chillaQuality(chilla.id, it) }
            val weekCounts = qualityKeys.map { key -> weekValues.count { it == key } }
            Triple(week + 1, weekValues.count { it.isNotBlank() }, weekCounts)
        }
        val strongestWeek = weekSummaries.maxByOrNull { it.third[2] + it.third[3] }
        val weakestWeek = weekSummaries.maxByOrNull { it.third[0] }
        var streak = 0; var longestStreak = 0
        (1..availableDays).forEach { day -> if (chillaQuality(chilla.id, day).isNotBlank()) { streak++; longestStreak = maxOf(longestStreak, streak) } else streak = 0 }
        val color = try { Color(android.graphics.Color.parseColor(chilla.colorHex)) } catch (_: Exception) { Forest2 }
        ScrollPage {
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitSymbol(chilla.icon, chilla.imageFile, Modifier.size(54.dp).clip(RoundedCornerShape(17.dp)).background(color.copy(alpha = .14f)), 30.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chilla.title, color = Forest, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        Text("شروع: ${formatJalaliDate(chilla.startMillis)}", color = Muted, fontSize = 11.sp)
                        Text(if (availableDays == 0) "هنوز زمان شروع نرسیده" else "روز ${persianDigits(availableDays.toString())} از ۴۰", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (chilla.goal.isNotBlank()) Text(chilla.goal, modifier = Modifier.padding(top = 10.dp), color = Ink, fontSize = 12.sp, lineHeight = 18.sp)
                if (chilla.intention.isNotBlank()) Text("نیت: ${chilla.intention}", modifier = Modifier.padding(top = 5.dp), color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(persianDigits(recordedDays.size.toString()), "روز ارزیابی‌شده", Modifier.weight(1f))
                    StatTile(persianDigits((40 - availableDays).coerceAtLeast(0).toString()), "روز باقی‌مانده", Modifier.weight(1f))
                    StatTile(persianDigits(longestStreak.toString()), "بیشترین تداوم ثبت", Modifier.weight(1f))
                }
            }
            PremiumCard {
                SectionTitle("تقویم ۴۰ روزه", "روزهای آینده غیرفعال‌اند؛ برای ثبت کیفیت، یکی از روزهای رسیده را انتخاب کن")
                (1..40).chunked(5).forEach { days ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        days.forEach { day ->
                            val allowed = day <= availableDays
                            val quality = chillaQuality(chilla.id, day)
                            val qualityIndex = qualityKeys.indexOf(quality)
                            val tileColor = if (qualityIndex >= 0) qualityColors[qualityIndex] else if (selectedDaySafe == day) PaleGold else Color(0xFFF5F2EA)
                            Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(tileColor).border(if (selectedDaySafe == day) 2.dp else 1.dp, if (selectedDaySafe == day) Gold else Color(0xFFE5DDCF), RoundedCornerShape(12.dp)).clickable(enabled = allowed) { selectedChillaDay = day }.padding(2.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(persianDigits(day.toString()), color = if (qualityIndex >= 0) Color.White else if (allowed) Forest else Muted.copy(alpha = .45f), fontSize = 13.sp, fontWeight = if (selectedDaySafe == day || qualityIndex >= 0) FontWeight.Bold else FontWeight.Normal)
                                    if (qualityIndex >= 0) Text(qualityLabels[qualityIndex], color = Color.White, fontSize = 7.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                if (availableDays == 0) Text("با رسیدن تاریخ شروع، خانهٔ روز اول فعال می‌شود.", color = Muted, fontSize = 11.sp)
            }
            PremiumCard {
                SectionTitle("ثبت کیفیت روز ${persianDigits(selectedDaySafe.toString())}", if (selectedDaySafe <= availableDays) "کیفیت این روز را انتخاب کن؛ این دسته‌بندی امتیاز عددی ندارد." else "این روز هنوز نرسیده است و فعلاً قابل ثبت نیست.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    qualityLabels.forEachIndexed { index, label ->
                        val key = qualityKeys[index]
                        val selected = selectedQualityInitial == key
                        FilterChip(
                            selected = selected,
                            onClick = { if (selectedDaySafe <= availableDays) saveChillaQuality(chilla.id, selectedDaySafe, key) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = qualityColors[index].copy(alpha = .18f), selectedLabelColor = Forest),
                            modifier = Modifier.weight(1f), enabled = selectedDaySafe <= availableDays
                        )
                    }
                }
                if (selectedDaySafe <= availableDays) {
                    val selectedNow = chillaQuality(chilla.id, selectedDaySafe)
                    if (selectedNow.isNotBlank()) Text("وضعیت ثبت‌شده: ${qualityLabels[qualityKeys.indexOf(selectedNow)]}", color = Forest2, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                    Spacer(Modifier.height(9.dp))
                    OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("یادداشت و تجربهٔ این روز") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("ذخیرهٔ یادداشت روز", { prefs.edit().putString("chilla_${chilla.id}_note_$selectedDaySafe", note).apply(); revision++; Toast.makeText(this@MainActivity, "یادداشت روز ذخیره شد", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
                }
            }
            PremiumCard {
                SectionTitle("آمار کیفیت کل روزهای چله", "فقط روزهای ارزیابی‌شده در نمودار شمرده می‌شوند؛ روزهای ثبت‌نشده صفر محسوب نمی‌شوند")
                QualityBarChart(counts)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("روزهای ثبت‌شده: ${persianDigits(recordedDays.size.toString())}", color = Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("از ${persianDigits(availableDays.toString())} روز سپری‌شده", color = Muted, fontSize = 10.sp)
                }
            }
            PremiumCard {
                SectionTitle("گزارش هفتگی و مقایسهٔ هفته‌ها", "توزیع کیفیت در هر هفته و روند مسیر")
                weekSummaries.forEach { (week, daysRecorded, weekCounts) ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("هفتهٔ ${persianDigits(week.toString())}", color = Forest, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text("${persianDigits(daysRecorded.toString())} روز ثبت‌شده", color = Muted, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(5.dp))
                        qualityLabels.forEachIndexed { index, label ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(label, color = Muted, fontSize = 9.sp, modifier = Modifier.width(46.dp))
                                LinearProgressIndicator(progress = { weekCounts[index] / 7f }, modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape), color = qualityColors[index], trackColor = Color(0xFFEFE9DE))
                                Text(persianDigits(weekCounts[index].toString()), color = Forest, fontSize = 10.sp, modifier = Modifier.width(23.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                    if (week != 6) Divider(color = PaleGold)
                }
                if (recordedDays.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("هفتهٔ با بیشترین روز خوب و عالی: ${persianDigits((strongestWeek?.first ?: 1).toString())}", color = Forest2, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("هفتهٔ نیازمند توجه بیشتر (بیشترین روز ضعیف): ${persianDigits((weakestWeek?.first ?: 1).toString())}", color = Muted, fontSize = 11.sp)
                }
            }
            PremiumCard {
                SectionTitle("نشان‌های مسیر", "نشان‌ها بر پایهٔ استمرار و ثبت روزها هستند، نه امتیازدهی عددی")
                val badges = listOf(
                    Triple("🌱", "آغازگر مسیر", recordedDays.isNotEmpty()),
                    Triple("🗓️", "هفت روز ثبت", recordedDays.size >= 7),
                    Triple("🔥", "تداوم هفت‌روزه", longestStreak >= 7),
                    Triple("🌟", "بیست روز همراهی", recordedDays.size >= 20),
                    Triple("🏅", "پایان چله", elapsed > 40)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    badges.forEach { (emoji, label, unlocked) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if (unlocked) PaleGold else Cream).padding(vertical = 9.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 21.sp)
                            Text(label, color = if (unlocked) Forest else Muted, fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 2)
                        }
                    }
                }
            }
            PremiumCard {
                SectionTitle("مدیریت چله")
                Text(if (chilla.archived) "این چله در بایگانی است؛ گزارش‌ها و یادداشت‌های آن محفوظ‌اند." else "می‌توانی این چله را بایگانی کنی یا چلهٔ دیگری را فعال کنی.", color = Muted, fontSize = 11.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { navigateTo("چله‌ها") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Icon(Icons.Filled.FolderOpen, null); Spacer(Modifier.width(6.dp)); Text("مدیریت همهٔ چله‌ها")
                }
                if (!chilla.archived) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { archiveChilla(chilla.id); navigateBack() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)) {
                        Icon(Icons.Filled.Archive, null); Spacer(Modifier.width(6.dp)); Text("انتقال چله به بایگانی")
                    }
                }
            }
        }
    }

    @Composable private fun ElapsedDaysRing(elapsed: Int, modifier: Modifier = Modifier) {
        val safeElapsed = elapsed.coerceIn(0, 40)
        Box(modifier, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val stroke = 6.dp.toPx()
                val arcTopLeft = androidx.compose.ui.geometry.Offset(stroke / 2f, stroke / 2f)
                val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = Color(0xFFE7D7AE), startAngle = -90f, sweepAngle = 360f,
                    useCenter = false, topLeft = arcTopLeft, size = arcSize,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                if (safeElapsed > 0) {
                    drawArc(
                        color = Color(0xFF18765A), startAngle = -90f, sweepAngle = safeElapsed / 40f * 360f,
                        useCenter = false, topLeft = arcTopLeft, size = arcSize,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(persianDigits(safeElapsed.toString()), color = Forest, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Text("از ۴۰ روز", color = Muted, fontSize = 7.sp, maxLines = 1)
            }
        }
    }

    @Composable private fun QualityBarChart(counts: List<Int>, modifier: Modifier = Modifier) {
        val maxCount = (counts.maxOrNull() ?: 0).coerceAtLeast(1)
        Row(modifier.height(78.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            // RTL layout: excellent is the first/right-most column, weak is the left-most.
            qualityLabels.indices.reversed().forEach { index ->
                val label = qualityLabels[index]
                val count = counts.getOrElse(index) { 0 }
                val barHeight = if (count == 0) 3.dp else (count.toFloat() / maxCount * 31f).dp
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(persianDigits(count.toString()), color = Forest, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.width(18.dp).height(barHeight).clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)).background(qualityColors[index]))
                    Spacer(Modifier.height(4.dp))
                    Text(label, color = Muted, fontSize = 7.sp, maxLines = 1)
                }
            }
        }
    }

    @Composable private fun QualityPieChart(counts: List<Int>, modifier: Modifier = Modifier) {
        val total = counts.sum()
        Box(modifier, contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(80.dp)) {
                val diameter = size.minDimension
                val inset = 3.dp.toPx()
                val arcSize = androidx.compose.ui.geometry.Size(diameter - inset * 2, diameter - inset * 2)
                var angle = -90f
                if (total == 0) {
                    drawArc(Color(0xFFE6D5AB), -90f, 360f, useCenter = true, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize)
                } else {
                    counts.forEachIndexed { index, value ->
                        if (value > 0) {
                            val sweep = value.toFloat() / total * 360f
                            drawArc(qualityColors[index], angle, sweep, useCenter = true, topLeft = androidx.compose.ui.geometry.Offset(inset, inset), size = arcSize)
                            angle += sweep
                        }
                    }
                }
                val hole = diameter * .48f
                drawCircle(Color(0xFFFFFCF3), radius = hole / 2f, center = center)
                drawCircle(Color(0xFFC9A04A), radius = hole / 2f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(persianDigits(total.toString()), color = Forest, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Text("ثبت‌شده", color = Muted, fontSize = 8.sp)
            }
        }
    }

    @Composable private fun HabitsPage() {
        val names = habitNames()
        var showCreateForm by remember { mutableStateOf(false) }
        var createEmoji by remember { mutableStateOf("📖") }
        var createColor by remember { mutableStateOf(habitColorOptions[0]) }
        var createStartChunk by remember { mutableIntStateOf(0) }
        var createChunkMenu by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { if (openCreateFormOnArrival) { showCreateForm = true; openCreateFormOnArrival = false } }
        ScrollPage {
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { SectionTitle("عادت‌های من", "هر عادت یک مسیر مستقل ۴۰۰ قدمی دارد") }
                    Button(onClick = { showCreateForm = !showCreateForm }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Forest, contentColor = Color(0xFFFFF4D7)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp), contentPadding = PaddingValues(horizontal = 13.dp, vertical = 9.dp)) {
                        Icon(if (showCreateForm) Icons.Filled.Close else Icons.Filled.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(if (showCreateForm) "بستن" else "ایجاد عادت", fontSize = 12.sp)
                    }
                }
                names.forEachIndexed { index, name ->
                    val total = (0 until 400).count { prefs.getString("h400_${index}_$it", "") != "" }
                    val accent = habitColor(index)
                    Column(Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFFFFBF3)).border(1.dp, Color(0xFFE8DDC9), RoundedCornerShape(20.dp)).padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(Modifier.weight(1f).clickable { selectedHabit = index; navigateTo("جزئیات عادت") }, verticalAlignment = Alignment.CenterVertically) {
                                HabitSymbol(
                                    prefs.getString("hmeta_${index}_emoji", defaultHabitEmoji(index)) ?: defaultHabitEmoji(index),
                                    prefs.getString("hmeta_${index}_icon_file", "") ?: "",
                                    Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(accent.copy(alpha=.13f)), 27.sp
                                )
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.Bold, color = Forest, fontSize = 15.sp)
                                    Text("$total از ۴۰۰ قدم ثبت‌شده", fontSize = 11.sp, color = Muted)
                                }
                                Icon(Icons.Filled.ChevronLeft, null, tint = accent)
                            }
                        }
                        Spacer(Modifier.height(9.dp))
                        LinearProgressIndicator(progress = { total / 400f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = accent, trackColor = PaleGold)
                    }
                }
            }
            if (showCreateForm) {
                PremiumCard {
                    SectionTitle("ایجاد عادت جدید", "اطلاعات را هنگام ساخت وارد کن؛ بعداً فقط در صفحهٔ اختصاصی همان عادت قابل ویرایش‌اند")
                    OutlinedTextField(customHabitDraft, { customHabitDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام عادت") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(Modifier.height(8.dp))
                    PersianDateField(customDateDraft, { customDateDraft = it }, "تاریخ شروع شمسی")
                    Spacer(Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { createChunkMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                            Text("شروع مسیر از لقمهٔ ${persianDigits((createStartChunk + 1).toString())} · خانهٔ ${persianDigits((createStartChunk * 40 + 1).toString())}", color = Forest)
                            Spacer(Modifier.width(5.dp)); Icon(Icons.Filled.ExpandMore, null, tint = Forest)
                        }
                        DropdownMenu(expanded = createChunkMenu, onDismissRequest = { createChunkMenu = false }) {
                            (0 until 10).forEach { chunk ->
                                DropdownMenuItem(text = { Text("لقمهٔ ${persianDigits((chunk + 1).toString())} · خانه‌های ${persianDigits((chunk * 40 + 1).toString())} تا ${persianDigits(((chunk + 1) * 40).toString())}") }, onClick = { createStartChunk = chunk; createChunkMenu = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HabitStylePicker(createEmoji, { createEmoji = it }, createColor, { createColor = it },
                        imageFile = if (iconPickerTarget == -1) pendingHabitIconFile else "",
                        onPickImage = { pickHabitIcon(-1) }, onClearImage = { pendingHabitIconFile = ""; iconPickerTarget = -1 })
                    Spacer(Modifier.height(10.dp))
                    SectionTitle("سه سطح انجام کار", "برای روزهای عادی، سخت و خیلی سخت")
                    OutlinedTextField(customTargetDraft, { customTargetDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه کوچک؛ مقدار معمول") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(customSpecialDraft, { customSpecialDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه ویژه؛ مقدار کمتر") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(customEmergencyDraft, { customEmergencyDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه اضطراری؛ کوچک‌ترین قدم") }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("ثبت عادت و شروع مسیر", {
                        val name = customHabitDraft.trim()
                        if (name.isNotEmpty() && name !in names) {
                            val newIndex = names.size
                            val date = customDateDraft.ifBlank { todayDateText() }
                            val editor = prefs.edit()
                                .putString("custom_habits", (prefs.getString("custom_habits", "")!! + "\n" + name).trim())
                                .putString("hmeta_${newIndex}_target", customTargetDraft)
                                .putString("hmeta_${newIndex}_special", customSpecialDraft)
                                .putString("hmeta_${newIndex}_emergency", customEmergencyDraft)
                                .putString("hmeta_${newIndex}_date", date)
                                .putString("hmeta_${newIndex}_emoji", createEmoji)
                                .putString("hmeta_${newIndex}_color", createColor)
                                .putString("hmeta_${newIndex}_icon_file", if (iconPickerTarget == -1) pendingHabitIconFile else "")
                                .putInt("hmeta_${newIndex}_start_chunk", createStartChunk)
                                .putLong("hmeta_${newIndex}_start_millis", parseHabitDate(date))
                            editor.putBoolean("h400_expand_${newIndex}_$createStartChunk", true)
                            editor.apply()
                            customHabitDraft = ""; customDateDraft = ""; customTargetDraft = ""; customSpecialDraft = ""; customEmergencyDraft = ""
                            createEmoji = "📖"; createColor = habitColorOptions[0]; createStartChunk = 0; showCreateForm = false
                            pendingHabitIconFile = ""; iconPickerTarget = -1
                            selectedHabit = newIndex; revision++; navigateTo("جزئیات عادت")
                        } else Toast.makeText(this@MainActivity, "نام عادت را وارد کن یا نام تکراری را تغییر بده.", Toast.LENGTH_SHORT).show()
                    }, Modifier.fillMaxWidth())
                }
            }
        }
    }

    @Composable private fun HabitSymbol(emoji: String, imageFile: String, modifier: Modifier = Modifier, emojiSize: androidx.compose.ui.unit.TextUnit = 26.sp) {
        val bitmap: ImageBitmap? = remember(imageFile) {
            if (imageFile.isBlank()) null else try {
                BitmapFactory.decodeFile(File(filesDir, imageFile).absolutePath)?.asImageBitmap()
            } catch (_: Exception) { null }
        }
        if (bitmap != null) Image(bitmap = bitmap, contentDescription = "نماد عادت", modifier = modifier.clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
        else Box(modifier, contentAlignment = Alignment.Center) { Text(emoji, fontSize = emojiSize) }
    }

    @Composable private fun HabitStylePicker(emoji: String, onEmoji: (String) -> Unit, colorHex: String, onColor: (String) -> Unit, imageFile: String = "", onPickImage: () -> Unit = {}, onClearImage: () -> Unit = {}) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("نماد عادت", color = Forest, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitSymbol(emoji, imageFile, Modifier.size(54.dp).clip(RoundedCornerShape(15.dp)).background(PaleGold), 27.sp)
                Column(Modifier.weight(1f)) {
                    Text("نماد انتخاب‌شده", color = Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(if (imageFile.isBlank()) "ایموجی یا شکل دلخواه" else "تصویر شخصی از گالری", color = Muted, fontSize = 10.sp)
                    TextButton(onClick = onPickImage, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) { Icon(Icons.Filled.AddPhotoAlternate, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp)); Text("انتخاب تصویر از گالری", fontSize = 11.sp) }
                }
            }
            Text("نمادهای مذهبی، رشد فردی و ترک عادت‌های نامطلوب", color = Muted, fontSize = 11.sp)
            habitIconOptions.chunked(6).forEach { group ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    group.forEach { option ->
                        Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (emoji == option) PaleGold else Cream).border(if (emoji == option) 2.dp else 1.dp, if (emoji == option) Gold else Color(0xFFE4DED2), RoundedCornerShape(12.dp)).clickable { onEmoji(option); onClearImage() }, contentAlignment = Alignment.Center) {
                            Text(option, fontSize = 23.sp)
                        }
                    }
                    repeat(6-group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            OutlinedTextField(emoji, { onEmoji(it); onClearImage() }, modifier = Modifier.fillMaxWidth(), label = { Text("نماد دلخواه؛ می‌توانی ایموجی خودت را وارد کنی") }, singleLine = true, shape = RoundedCornerShape(12.dp))
            if (imageFile.isNotBlank()) TextButton(onClick = onClearImage, modifier = Modifier.align(Alignment.End)) { Text("بازگشت به نماد ایموجی", color = Forest2, fontSize = 11.sp) }
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
            val hsv = remember(colorHex) {
                FloatArray(3).also { values ->
                    try { android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(colorHex), values) }
                    catch (_: Exception) { values[0] = 145f; values[1] = .72f; values[2] = .72f }
                }
            }
            var hue by remember(colorHex) { mutableFloatStateOf(hsv[0]) }
            var saturation by remember(colorHex) { mutableFloatStateOf(hsv[1]) }
            var brightness by remember(colorHex) { mutableFloatStateOf(hsv[2]) }
            val selectedSwatch = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (_: Exception) { Forest2 }
            fun publishColor(h: Float = hue, sat: Float = saturation, value: Float = brightness) {
                val argb = android.graphics.Color.HSVToColor(floatArrayOf(h.coerceIn(0f, 360f), sat.coerceIn(0f, 1f), value.coerceIn(0f, 1f)))
                onColor(String.format(Locale.US, "#%06X", 0xFFFFFF and argb))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(selectedSwatch).border(2.dp, Gold, CircleShape))
                Column(Modifier.weight(1f)) {
                    Text("انتخاب آزاد رنگ", color = Forest, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("با تنظیم طیف، شدت و روشنایی، هر رنگ دلخواه را بساز", color = Muted, fontSize = 10.sp)
                }
            }
            Text("طیف رنگ", color = Muted, fontSize = 10.sp)
            Slider(value = hue, onValueChange = { hue = it; publishColor(h = it) }, valueRange = 0f..360f, steps = 11)
            Text("شدت رنگ", color = Muted, fontSize = 10.sp)
            Slider(value = saturation, onValueChange = { saturation = it; publishColor(sat = it) }, valueRange = 0f..1f)
            Text("روشنایی", color = Muted, fontSize = 10.sp)
            Slider(value = brightness, onValueChange = { brightness = it; publishColor(value = it) }, valueRange = .18f..1f)
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
        var editableStartDate by remember(habit) { mutableStateOf(displayHabitDate(prefs.getString("hmeta_${habit}_date", "") ?: "")) }
        var editableEmoji by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_emoji", defaultHabitEmoji(habit)) ?: defaultHabitEmoji(habit)) }
        var editableColor by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_color", habitColorOptions[habit % habitColorOptions.size]) ?: habitColorOptions[habit % habitColorOptions.size]) }
        var editableIconFile by remember(habit) { mutableStateOf(prefs.getString("hmeta_${habit}_icon_file", "") ?: "") }
        var editableStartChunk by remember(habit) { mutableIntStateOf(prefs.getInt("hmeta_${habit}_start_chunk", 0).coerceIn(0, 9)) }
        var editChunkMenu by remember(habit) { mutableStateOf(false) }
        LaunchedEffect(habit) { if (iconPickerTarget != habit) pendingHabitIconFile = "" }
        var showDeleteConfirm by remember(habit) { mutableStateOf(false) }
        var showHabitEditor by remember(habit) { mutableStateOf(false) }
        var reminderOn by remember(habit) { mutableStateOf(prefs.getBoolean("habit_reminder_${habit}_on", false)) }
        var reminderTime by remember(habit) { mutableStateOf(prefs.getString("habit_reminder_${habit}_time", "20:00") ?: "20:00") }
        var reminderText by remember(habit) { mutableStateOf(prefs.getString("habit_reminder_${habit}_text", ReminderReceiver.MESSAGES[habit % ReminderReceiver.MESSAGES.size]) ?: ReminderReceiver.MESSAGES[habit % ReminderReceiver.MESSAGES.size]) }
        ScrollPage {
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitSymbol(
                        prefs.getString("hmeta_${habit}_emoji", defaultHabitEmoji(habit)) ?: defaultHabitEmoji(habit),
                        prefs.getString("hmeta_${habit}_icon_file", "") ?: "",
                        Modifier.size(54.dp).clip(RoundedCornerShape(18.dp)).background(habitColor(habit).copy(alpha=.14f)), 30.sp
                    )
                    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { SectionTitle(names[habit], "جدول ۴۰۰ قدم · ۱۰ لقمهٔ پیوسته"); Text("تغییر وضعیت هر خانه با لمس آن", fontSize = 11.sp, color = Muted) }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { StatTile("${completed}", "ثبت‌شده", Modifier.weight(1f)); StatTile("${small}", "لقمه کوچک", Modifier.weight(1f)); StatTile("${special}", "ویژه", Modifier.weight(1f)); StatTile("${emergency}", "اضطراری", Modifier.weight(1f)) }
                Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { completed / 400f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Gold, trackColor = PaleGold)
                Text("${completed * 100 / 400}% تکمیل · ${400-completed} خانه باقی‌مانده", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
            }
            if (showHabitEditor) {
                PremiumCard {
                    SectionTitle("ویرایش عادت", "تغییرات فقط با ذخیره ثبت می‌شوند؛ خانه‌های قبلی دست‌نخورده می‌مانند")
                    OutlinedTextField(editableName, { editableName = it }, modifier = Modifier.fillMaxWidth(), label = { Text("نام عادت") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    PersianDateField(editableStartDate, { editableStartDate = it }, "تاریخ شروع شمسی")
                    Spacer(Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { editChunkMenu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                            Text("لقمهٔ آغازین: ${persianDigits((editableStartChunk + 1).toString())}", color = Forest)
                            Spacer(Modifier.width(5.dp)); Icon(Icons.Filled.ExpandMore, null, tint = Forest)
                        }
                        DropdownMenu(expanded = editChunkMenu, onDismissRequest = { editChunkMenu = false }) {
                            (0 until 10).forEach { chunk -> DropdownMenuItem(text = { Text("لقمهٔ ${persianDigits((chunk + 1).toString())}") }, onClick = { editableStartChunk = chunk; editChunkMenu = false }) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    HabitStylePicker(editableEmoji, { editableEmoji = it }, editableColor, { editableColor = it },
                        imageFile = if (iconPickerTarget == habit && pendingHabitIconFile.isNotBlank()) pendingHabitIconFile else editableIconFile,
                        onPickImage = { pickHabitIcon(habit) }, onClearImage = { editableIconFile = ""; pendingHabitIconFile = ""; iconPickerTarget = habit })
                    Spacer(Modifier.height(12.dp)); SectionTitle("سه سطح انجام کار")
                    OutlinedTextField(target, { target = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه کوچک؛ مقدار معمول") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(specialTarget, { specialTarget = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه ویژه؛ مقدار کمتر") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(7.dp)); OutlinedTextField(emergencyTarget, { emergencyTarget = it }, modifier = Modifier.fillMaxWidth(), label = { Text("لقمه اضطراری؛ کوچک‌ترین قدم") }, shape = RoundedCornerShape(13.dp))
                    Spacer(Modifier.height(10.dp))
                    PrimaryButton("ذخیرهٔ همهٔ تغییرات", {
                        val newName = editableName.trim()
                        if (newName.isNotEmpty()) {
                            val editor = prefs.edit()
                                .putString("habit_name_override_$habit", newName)
                                .putString("hmeta_${habit}_date", editableStartDate.ifBlank { todayDateText() })
                                .putLong("hmeta_${habit}_start_millis", parseHabitDate(editableStartDate.ifBlank { todayDateText() }))
                                .putString("hmeta_${habit}_emoji", editableEmoji)
                                .putString("hmeta_${habit}_color", editableColor)
                                .putString("hmeta_${habit}_icon_file", if (iconPickerTarget == habit && pendingHabitIconFile.isNotBlank()) pendingHabitIconFile else editableIconFile)
                                .putInt("hmeta_${habit}_start_chunk", editableStartChunk)
                                .putBoolean("h400_expand_${habit}_$editableStartChunk", true)
                                .putString("hmeta_${habit}_target", target)
                                .putString("hmeta_${habit}_special", specialTarget)
                                .putString("hmeta_${habit}_emergency", emergencyTarget)
                            if (habit >= defaultHabits.size) {
                                val extras = (prefs.getString("custom_habits", "") ?: "").split("\n").filter { it.isNotBlank() }.toMutableList()
                                val extraIndex = habit - defaultHabits.size
                                if (extraIndex in extras.indices) { extras[extraIndex] = newName; editor.putString("custom_habits", extras.joinToString("\n")) }
                            }
                            editor.apply()
                            editableIconFile = if (iconPickerTarget == habit && pendingHabitIconFile.isNotBlank()) pendingHabitIconFile else editableIconFile
                            if (iconPickerTarget == habit) { pendingHabitIconFile = ""; iconPickerTarget = -1 }
                            if (reminderOn) configureHabitReminder(habit, newName, true, reminderTime, reminderText)
                            revision++; showHabitEditor = false
                            Toast.makeText(this@MainActivity, "همهٔ مشخصات و هدف‌ها ذخیره شد", Toast.LENGTH_SHORT).show()
                        }
                    }, Modifier.fillMaxWidth())
                    if (habit >= defaultHabits.size) {
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose)) { Icon(Icons.Filled.Delete, null); Spacer(Modifier.width(6.dp)); Text("حذف این عادت شخصی") }
                    }
                    if (showDeleteConfirm) AlertDialog(onDismissRequest = { showDeleteConfirm = false }, title = { Text("حذف عادت؟") }, text = { Text("عادت و داده‌های وابسته به آن از این دستگاه حذف می‌شود. اگر لازم است، ابتدا نسخهٔ پشتیبان بگیر.") }, confirmButton = { TextButton(onClick = { showDeleteConfirm = false; deleteCustomHabit(habit) }) { Text("حذف", color = Rose) } }, dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") } })
                }
            } else {
                OutlinedButton(
                    onClick = { showHabitEditor = true },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .65f))
                ) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(15.dp), tint = Forest2)
                    Spacer(Modifier.width(5.dp))
                    Text("ویرایش این عادت", fontSize = 11.sp, color = Forest)
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
                            Box(Modifier.size(38.dp).clip(CircleShape).background(Forest), contentAlignment = Alignment.Center) { Text(persianDigits((row+1).toString()), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text("لقمهٔ ${persianDigits((row+1).toString())}", fontSize = 15.sp, color = Forest, fontWeight = FontWeight.Bold)
                                Text("خانه‌های ${persianDigits(firstNumber.toString())} تا ${persianDigits(lastNumber.toString())} · ${persianDigits(filledInRow.toString())} از ۴۰", fontSize = 10.sp, color = Muted)
                            }
                            IconButton(onClick = { expanded = !expanded; prefs.edit().putBoolean("h400_expand_${habit}_$row", expanded).apply() }) {
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = if(expanded) "جمع‌کردن" else "نمایش چهل خانه", tint = Forest, modifier = Modifier.size(30.dp))
                            }
                        }
                        if (expanded && rowTarget.isNotBlank()) Text(rowTarget, Modifier.fillMaxWidth().padding(top = 6.dp), color = Ink, fontSize = 12.sp)
                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(rowTarget, { rowTarget = it; prefs.edit().putString("h400_row_${habit}_$row",it).apply() }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("شرح این لقمه") }, shape = RoundedCornerShape(12.dp))
                            Spacer(Modifier.height(10.dp))
                            // Forty square tiles are visible together in an adaptive five-column grid.
                            (0 until 40).chunked(5).forEach { tileRow ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    tileRow.forEach { col ->
                                        val idx = row * 40 + col
                                        val globalNumber = idx + 1
                                        val value = prefs.getString("h400_${habit}_$idx", "") ?: ""
                                        val bg = when(value) { "s" -> Color(0xFF3D8B6E); "v" -> Color(0xFFD6A64D); "e" -> Color(0xFFB96865); else -> Color.White }
                                        Box(
                                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(bg)
                                                .border(1.dp, if(value.isEmpty()) Color(0xFFD5D8D1) else bg, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    val next = when(value) { "" -> "s"; "s" -> "v"; "v" -> "e"; else -> "" }
                                                    prefs.edit().putString("h400_${habit}_$idx", next).apply(); revision++
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(persianDigits(globalNumber.toString()), color = if(value.isEmpty()) Ink else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
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
                val exactAllowed = Build.VERSION.SDK_INT < 31 || getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
                Text("دسترسی هشدار دقیق: ${if (exactAllowed) "فعال" else "غیرفعال؛ زمان‌بندی ممکن است با تأخیر باشد"}", color = if (exactAllowed) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("اعلان‌های برنامه: ${if (appEnabled) "روشن" else "خاموش"}", color = if (appEnabled) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("کانال اعلان: ${if (channelEnabled) "فعال" else "خاموش"}", color = if (channelEnabled) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("محدودیت باتری: ${if (batteryFree) "مستثنا از بهینه‌سازی" else "ممکن است محدود باشد"}", color = if (batteryFree) Forest2 else Rose, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { requestExactAlarmAccessIfNeeded() }, modifier = Modifier.fillMaxWidth(), enabled = !exactAllowed) { Text("فعال‌کردن هشدار دقیق") }
                OutlinedButton(onClick = { try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) } }, modifier = Modifier.fillMaxWidth()) { Text("بازکردن تنظیمات محدودیت باتری") }
                Text("اول «آزمایش فوری اعلان» را بزن. اگر رسید، بعد «آزمایش زمان‌بندی‌شده» را بزن. فوری‌رسیدن و نرسیدن زمان‌بندی‌شده، مسیر ایراد را مشخص می‌کند.", fontSize = 11.sp, color = Muted, lineHeight = 17.sp)
            }
            PremiumCard { SectionTitle("نکتهٔ مهم")
                Text("در بعضی گوشی‌ها علاوه بر مجوز اعلان، اجرای خودکار و فعالیت پس‌زمینه باید در تنظیمات مدیریت باتری گوشی فعال باشد.", fontSize = 13.sp, color = Muted)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        when (requestCode) {
            29 -> {
                if (granted) {
                    createNotificationChannel()
                    if (reminderEnabled) ReminderReceiver.schedule(this, prefs)
                    habitNames().forEachIndexed { index, name ->
                        if (prefs.getBoolean("habit_reminder_${index}_on", false)) {
                            ReminderReceiver.scheduleHabit(this, index, name, prefs.getString("habit_reminder_${index}_time", "20:00") ?: "20:00", prefs.getString("habit_reminder_${index}_text", "") ?: "")
                        }
                    }
                    Toast.makeText(this, "مجوز اعلان فعال شد؛ یادآورها آماده‌اند.", Toast.LENGTH_LONG).show()
                } else Toast.makeText(this, "تا وقتی مجوز اعلان فعال نباشد، پیام هشدار نمایش داده نمی‌شود.", Toast.LENGTH_LONG).show()
            }
            30 -> {
                if (granted) showTestNotification()
                else Toast.makeText(this, "برای نمایش اعلان آزمایشی، مجوز اعلان را در تنظیمات برنامه فعال کن.", Toast.LENGTH_LONG).show()
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
        val notification = builder.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("قدم یار · پیام امروز")
            .setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_ALARM).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).setVibrate(longArrayOf(0, 650, 300, 650)).setAutoCancel(true).build()
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
                SettingRow(Icons.Filled.AutoAwesome, "ابزارهای همراه مسیر", "تاکتیک‌های شیطان، جدول الف و چک‌لیست انگیزه") { navigateTo("ابزارهای پیشرفته") }
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
            PremiumCard { SectionTitle("دربارهٔ قدم یار")
                Text("قدم یار · نسخهٔ بومی اندروید ۴۰", color = Forest, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp)); Text(APP_TAGLINE + "\nبرای همراهی در یک مسیر چهل‌روزه از عهد، مراقبت و رشد معنوی.", color = Muted, fontSize = 13.sp, lineHeight = 21.sp)
            }
        }
    }
    @Composable private fun ToolsPage() {
        ScrollPage {
            PremiumCard { SectionTitle("ابزارهای مسیر قدم یار", "سه فضای مستقل برای خودشناسی، نظم و انگیزه")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeatureTile(R.drawable.feature_tactics, "تاکتیک‌های شیطان", "شناخت دام‌ها و راه مقابله", { navigateTo("تاکتیک‌های شیطان") }, Modifier.weight(1f), darkText = false)
                    FeatureTile(R.drawable.feature_table, "جدول الف", "قدم به سوی موفقیت", { navigateTo("جدول الف") }, Modifier.weight(1f), darkText = true)
                }
                FeatureTile(R.drawable.feature_motivation, "چک‌لیست انگیزه", "۱۶ عامل انگیزشی؛ قابل ویرایش و شخصی‌سازی", { navigateTo("چک‌لیست انگیزه") }, Modifier.fillMaxWidth(), darkText = true, wide = true)
            }
            PremiumCard {
                SectionTitle("ابزارهای تکمیلی")
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
        val activeDate = selectedVirtueDate.ifBlank { todayDateText() }
        val dateKey = latinDigits(activeDate).replace("/", "_").replace(" ", "")
        val categoryColors = listOf(Color(0xFFF3D5AE), Color(0xFFF4BEB8), Color(0xFFC9D9ED), Color(0xFFE8D9B6), Color(0xFFCEE3D0), Color(0xFFBFE0D8), Color(0xFFD8CBE8), Color(0xFFF5D8B8), Color(0xFFD8E6C9), Color(0xFFC9DDE9))
        val sampleFirstRow = listOf("روزهٔ قضا", "غیبت و خشم", "قرائت قرآن", "اتلاف وقت و پرخوری", "عفو و گذشت", "ورزش", "برنامه‌ریزی کار", "تفریح سالم", "گفت‌وگو با مادر", "حدیث‌خوانی")
        ScrollPage {
            PremiumCard {
                SectionTitle("جدول الف", "ده حوزهٔ اصلی زندگی؛ هر ردیف یک نوبت مرور و ارزیابی")
                PersianDateField(selectedVirtueDate, { selectedVirtueDate = it }, "تاریخ ثبت شمسی")
                Spacer(Modifier.height(7.dp))
                Text("برای دیدن همهٔ ستون‌ها جدول را افقی جابه‌جا کن. هر خانه با لمس و نوشتن، خودکار ذخیره می‌شود.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
            }
            PremiumCard {
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(42.dp).height(70.dp), contentAlignment = Alignment.Center) { Text("ردیف", color = Muted, fontSize = 10.sp) }
                        virtueHeaders.forEachIndexed { i, title ->
                            Box(Modifier.width(112.dp).height(70.dp).clip(RoundedCornerShape(18.dp)).background(categoryColors[i]).padding(5.dp), contentAlignment = Alignment.Center) {
                                Text(title, color = Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    (0 until 10).forEach { row ->
                        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(42.dp).height(66.dp).clip(RoundedCornerShape(14.dp)).background(PaleGold), contentAlignment = Alignment.Center) { Text(persianDigits((row + 1).toString()), color = Forest, fontWeight = FontWeight.Bold) }
                            virtueHeaders.forEachIndexed { col, label ->
                                val legacyPersianKey = "virtue_${activeDate.replace("/", "_").replace(" ", "")}_$col"
                                val legacyLatinKey = "virtue_${dateKey}_$col"
                                val legacyValue = if (row == 0) prefs.getString(legacyPersianKey, prefs.getString(legacyLatinKey, sampleFirstRow[col])) else ""
                                val initialCell = prefs.getString("virtue_${dateKey}_${row}_${col}", legacyValue) ?: ""
                                var value by remember(dateKey, row, col) { mutableStateOf(initialCell) }
                                OutlinedTextField(
                                    value, { value = it; prefs.edit().putString("virtue_${dateKey}_${row}_${col}", it).apply() },
                                    modifier = Modifier.width(112.dp).height(66.dp), singleLine = true,
                                    placeholder = { Text(label, fontSize = 10.sp, color = Muted) },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Ink, textAlign = TextAlign.Center),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = categoryColors[col].copy(alpha = .18f), focusedContainerColor = categoryColors[col].copy(alpha = .28f), unfocusedBorderColor = Color.Transparent, focusedBorderColor = Forest2)
                                )
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = {
                val editor = prefs.edit()
                (0 until 10).forEach { row -> virtueHeaders.indices.forEach { col -> editor.remove("virtue_${dateKey}_${row}_${col}") } }
                virtueHeaders.indices.forEach { col ->
                    editor.remove("virtue_${activeDate.replace("/", "_").replace(" ", "")}_$col")
                    editor.remove("virtue_${dateKey}_$col")
                }
                editor.apply(); revision++
            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Filled.DeleteOutline, null); Spacer(Modifier.width(5.dp)); Text("پاک‌کردن جدول این تاریخ") }
        }
    }

    @Composable private fun MotivationPage() {
        val activeDate = motivationDateDraft.ifBlank { todayDateText() }
        val dateKey = latinDigits(activeDate).replace("/", "_").replace(" ", "")
        val legacyDateKey = activeDate.replace("/", "_").replace(" ", "")
        val colors = listOf(Color(0xFFF4C9C3), Color(0xFFF6D9B8), Color(0xFFD6E7CF), Color(0xFFC9D8EF))
        var items by remember { mutableStateOf(loadMotivationItems()) }
        var editingItems by remember { mutableStateOf(false) }
        var newItemTitle by remember { mutableStateOf("") }
        var titleDraft by remember(dateKey) { mutableStateOf(prefs.getString("mot_title_$dateKey", prefs.getString("mot_title_$legacyDateKey", motivationTitleDraft)) ?: motivationTitleDraft) }
        val checkedCount = items.count { item -> prefs.getBoolean("mot_${dateKey}_${item.id}", prefs.getBoolean("mot_${legacyDateKey}_${item.id}", false)) }
        ScrollPage {
            PremiumCard {
                SectionTitle("چک‌لیست انگیزه", "یادآورهای کوچک برای زنده نگه‌داشتن هدف و امید")
                OutlinedTextField(titleDraft, { titleDraft = it; motivationTitleDraft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("عنوان ثبت") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                PersianDateField(motivationDateDraft, { motivationDateDraft = it }, "تاریخ ثبت شمسی")
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("پیشرفت انگیزه", color = Forest, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${persianDigits(checkedCount.toString())} از ${persianDigits(items.size.toString())}", color = Gold, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(progress = { if (items.isEmpty()) 0f else checkedCount / items.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 7.dp).height(8.dp).clip(CircleShape), color = Forest2, trackColor = PaleGold)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { editingItems = !editingItems }, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Filled.Edit, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(4.dp))
                    Text(if (editingItems) "پایان مدیریت گزینه‌ها" else "ویرایش / افزودن / حذف گزینه‌ها", color = Forest2, fontSize = 11.sp)
                }
            }

            if (editingItems) {
                PremiumCard {
                    SectionTitle("مدیریت گزینه‌های انگیزه", "متن هر گزینه را عوض کن یا مورد تازه‌ای اضافه و موارد غیرضروری را حذف کن")
                    items.forEachIndexed { index, item ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = item.title,
                                onValueChange = { updated -> items = items.toMutableList().also { it[index] = item.copy(title = updated) } },
                                modifier = Modifier.weight(1f), singleLine = true,
                                leadingIcon = { Text(item.icon) }, shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(onClick = { items = items.filterNot { it.id == item.id } }) {
                                Icon(Icons.Filled.DeleteOutline, "حذف گزینه", tint = Rose)
                            }
                        }
                    }
                    OutlinedTextField(newItemTitle, { newItemTitle = it }, modifier = Modifier.fillMaxWidth(), label = { Text("عنوان گزینهٔ جدید") }, singleLine = true)
                    Spacer(Modifier.height(7.dp))
                    OutlinedButton(
                        onClick = {
                            val title = newItemTitle.trim()
                            if (title.isNotBlank()) {
                                val nextId = maxOf(prefs.getInt("motivation_next_id", 16), (items.maxOfOrNull { it.id } ?: -1) + 1)
                                prefs.edit().putInt("motivation_next_id", nextId + 1).apply()
                                items = items + MotivationItem(nextId, title, "✨")
                                newItemTitle = ""
                            }
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    ) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(5.dp)); Text("افزودن گزینه") }
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton("ذخیرهٔ تغییرات گزینه‌ها", { saveMotivationItems(items); editingItems = false; Toast.makeText(this@MainActivity, "فهرست انگیزه به‌روزرسانی شد", Toast.LENGTH_SHORT).show() }, Modifier.fillMaxWidth())
                }
            }

            if (items.isEmpty()) {
                PremiumCard { Text("هنوز گزینه‌ای در چک‌لیست نیست. از بخش مدیریت، گزینه‌های دلخواهت را اضافه کن.", color = Muted, fontSize = 13.sp) }
            } else {
                items.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        pair.forEachIndexed { pairIndex, item ->
                            val isChecked = prefs.getBoolean("mot_${dateKey}_${item.id}", prefs.getBoolean("mot_${legacyDateKey}_${item.id}", false))
                            val tint = colors[(item.id % colors.size)]
                            Card(
                                Modifier.weight(1f).padding(bottom = 8.dp).clickable {
                                    prefs.edit().putBoolean("mot_${dateKey}_${item.id}", !isChecked).putString("mot_title_$dateKey", titleDraft).apply(); revision++
                                }, shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isChecked) Color(0xFFE4F0E5) else tint.copy(alpha = .72f)),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = .85f)), contentAlignment = Alignment.Center) { Text(item.icon, fontSize = 19.sp) }
                                    Spacer(Modifier.width(7.dp))
                                    Text(item.title, Modifier.weight(1f), color = Forest, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 15.sp)
                                    Checkbox(isChecked, { checked -> prefs.edit().putBoolean("mot_${dateKey}_${item.id}", checked).putString("mot_title_$dateKey", titleDraft).apply(); revision++ }, modifier = Modifier.size(28.dp), colors = CheckboxDefaults.colors(checkedColor = Forest))
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            PrimaryButton("ذخیرهٔ چک‌لیست این روز", {
                saveMotivationItems(items)
                val selected = items.filter { prefs.getBoolean("mot_${dateKey}_${it.id}", prefs.getBoolean("mot_${legacyDateKey}_${it.id}", false)) }.joinToString(",") { it.id.toString() }
                prefs.edit().putString("mot_title_$dateKey", titleDraft).putString("mot_saved_$dateKey", selected).apply()
                Toast.makeText(this@MainActivity, "چک‌لیست ذخیره شد", Toast.LENGTH_SHORT).show(); revision++
            }, Modifier.fillMaxWidth())
            PremiumCard {
                SectionTitle("سابقهٔ ثبت‌ها")
                val history = prefs.all.keys.filter { it.startsWith("mot_saved_") }.sortedDescending()
                if (history.isEmpty()) Text("هنوز سابقه‌ای ثبت نشده است.", color = Muted)
                else history.forEach { key ->
                    val k = key.removePrefix("mot_saved_")
                    val title = prefs.getString("mot_title_$k", "چک‌لیست انگیزه") ?: "چک‌لیست انگیزه"
                    val count = (prefs.getString(key, "") ?: "").split(",").count { it.isNotBlank() }
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, null, tint = Gold); Spacer(Modifier.width(7.dp))
                        Text("$title · ${persianDigits(k.replace("_", "/"))} · ${persianDigits(count.toString())} مورد", color = Forest, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    @Composable private fun TacticsPage() {
        val tactics = listOf(
            "حمله از راست (عبادت)" to "افراط یا اشتباه در کارهای فرعی که می‌تواند تعادل و توجه به واجبات را کم‌رنگ کند.",
            "حمله از عقب (ناامیدی)" to "بزرگ‌نمایی شکست‌های گذشته و القای این فکر که دیگر راه بازگشتی نیست.",
            "حمله از چپ (تسویل)" to "زیبا جلوه دادن کار نادرست و عادی‌سازی تدریجی آن.",
            "گام‌به‌گام" to "عجله کردن یا نداشتن برنامهٔ مرحله‌ای و قابل اجرا برای مسیر رشد.",
            "تسویف" to "به تعویق انداختن کار درست و موکول کردن شروع به فردا.",
            "عجله در گناه" to "تصمیم شتاب‌زده و لغزش بدون مکث، فکر و سنجیدن پیامدها.",
            "فراموشی" to "از یاد بردن هدف، نیت و عهدی که برای خودت بسته‌ای.",
            "دوری از جبههٔ حق" to "فاصله گرفتن از دوستان، محیط و محتوایی که در مسیر درست پشتیبان تو هستند.",
            "حمله از جلو (ترس)" to "بزرگ‌نمایی سختی‌ها و ترساندن از قدم اول یا ادامهٔ مسیر.",
            "عجله و بی‌حوصلگی" to "انتظار نتیجهٔ فوری و رها کردن تمرین پیش از شکل‌گیری استمرار."
        )
        val tints = listOf(Color(0xFFDDEBE3), Color(0xFFF3DAD6), Color(0xFFF4E5C6), Color(0xFFDCE6F2), Color(0xFFE8DDF0), Color(0xFFF2E0D2), Color(0xFFE5EAD4), Color(0xFFD7EAE5), Color(0xFFE4E2F0), Color(0xFFF2E5D6))
        var selectedIndices by remember {
            mutableStateOf(
                (prefs.getStringSet("satan_selected_tactics_v41", emptySet()) ?: emptySet())
                    .mapNotNull { it.toIntOrNull() }.filter { it in tactics.indices }.toSet()
            )
        }
        var selectedTactic by remember { mutableIntStateOf(selectedIndices.firstOrNull() ?: 0) }
        var defense by remember(selectedTactic) { mutableStateOf(prefs.getString("satan_defense_$selectedTactic", "") ?: "") }

        fun toggleTactic(index: Int, checked: Boolean) {
            val updated = if (checked) selectedIndices + index else selectedIndices - index
            selectedIndices = updated
            prefs.edit().putStringSet("satan_selected_tactics_v41", updated.map { it.toString() }.toSet()).apply()
            if (checked) {
                selectedTactic = index
                defense = prefs.getString("satan_defense_$index", "") ?: ""
            } else if (selectedTactic == index) {
                selectedTactic = updated.firstOrNull() ?: 0
                defense = prefs.getString("satan_defense_${selectedTactic}", "") ?: ""
            }
            revision++
        }

        ScrollPage {
            Card(
                Modifier.fillMaxWidth().padding(bottom = 13.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = Forest),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(154.dp).clip(RoundedCornerShape(25.dp))) {
                    Image(painterResource(R.drawable.feature_tactics), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xD9001713), Color(0x44101712)))))
                    Column(Modifier.align(Alignment.CenterEnd).padding(17.dp), horizontalAlignment = Alignment.End) {
                        Text("تاکتیک‌های شیطان", color = Color(0xFFFFE3AE), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(5.dp))
                        Text("آگاهی، اولین سپر در برابر وسوسه‌هاست", color = Color.White, fontSize = 12.sp)
                        Text("موانع را بشناس؛ برای هرکدام پاسخی آگاهانه انتخاب کن.", color = Color(0xFFE5EBDD), fontSize = 10.sp)
                    }
                }
            }
            PremiumCard {
                SectionTitle("حمله‌ها و دام‌های رایج", "هر موردی را که تجربه کرده‌ای انتخاب کن؛ می‌توانی چند مورد را هم‌زمان علامت بزنی.")
                tactics.indices.toList().chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.Top) {
                        pair.forEach { index ->
                            val tactic = tactics[index]
                            val checked = index in selectedIndices
                            Card(
                                Modifier.weight(1f).heightIn(min = 112.dp).padding(bottom = 8.dp)
                                    .clickable { toggleTactic(index, !checked) },
                                shape = RoundedCornerShape(17.dp),
                                colors = CardDefaults.cardColors(containerColor = if (checked) tints[index] else Color(0xFFFFFEFA)),
                                elevation = CardDefaults.cardElevation(if (checked) 3.dp else 1.dp),
                                border = androidx.compose.foundation.BorderStroke(if (checked) 1.5.dp else 1.dp, if (checked) Forest2 else Color(0xFFDCE7DF))
                            ) {
                                Row(Modifier.fillMaxSize().padding(10.dp), verticalAlignment = Alignment.Top) {
                                    // RTL: checkbox is the first child so it appears at the right edge of each card.
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { toggleTactic(index, it) },
                                        modifier = Modifier.size(25.dp),
                                        colors = CheckboxDefaults.colors(checkedColor = Forest2, uncheckedColor = Muted)
                                    )
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(tactic.first, color = Forest, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp)
                                        Text(tactic.second, color = Muted, fontSize = 10.sp, lineHeight = 15.sp, maxLines = 3)
                                    }
                                }
                            }
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            PremiumCard {
                val current = tactics[selectedTactic]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(tints[selectedTactic]), contentAlignment = Alignment.Center) {
                        Text(persianDigits((selectedTactic + 1).toString()), color = Forest, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text("راهکار من برای دفعهٔ بعد", color = Forest, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(current.first, color = Muted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(9.dp))
                OutlinedTextField(
                    defense,
                    { defense = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("قدم کوچک و قابل اجرای من") },
                    placeholder = { Text("دقیقاً چه کاری انجام می‌دهم؟ اولین قدم کوچک و قابل اجرا چیست؟") },
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton("ذخیرهٔ این راهکار", {
                    prefs.edit().putString("satan_defense_$selectedTactic", defense).apply()
                    Toast.makeText(this@MainActivity, "راهکار ذخیره شد", Toast.LENGTH_SHORT).show()
                    revision++
                }, Modifier.fillMaxWidth())
                Text("راهکارها روی همین دستگاه ذخیره می‌شوند. هر بار می‌توانی آن‌ها را بازبینی و بهتر کنی.", color = Muted, fontSize = 10.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 7.dp))
            }
            PremiumCard {
                SectionTitle("چهار راهکار نجات‌بخش من", "چند کار ساده که در لحظهٔ دشوار کمک می‌کنند.")
                (1..4).forEach { i ->
                    var action by remember(i) { mutableStateOf(prefs.getString("satan_action_$i", "") ?: "") }
                    OutlinedTextField(action, { action = it; prefs.edit().putString("satan_action_$i", it).apply() }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), label = { Text("راهکار ${persianDigits(i.toString())}") }, minLines = 2, shape = RoundedCornerShape(12.dp))
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
            val channel = NotificationChannel(CHANNEL, "یادآورهای قدم یار · نسخه ۴۴", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                vibrationPattern = longArrayOf(0, 650, 300, 650)
                enableLights(true)
                lightColor = android.graphics.Color.rgb(195, 154, 82)
            }
            manager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            when (intent?.action) {
                "ir.chillehyar.mahdi.HABIT_REMINDER" -> {
                    val index = intent.getIntExtra("habit_index", 0)
                    if (prefs.getBoolean("habit_reminder_${index}_on", false)) {
                        scheduleHabit(context, index, intent.getStringExtra("habit_name") ?: "عادت روزانه", intent.getStringExtra("habit_time") ?: "20:00", intent.getStringExtra("habit_message") ?: "یک قدم کوچک امروز هم ارزشمند است.")
                    }
                }
                ACTION_TEST -> Unit
                else -> schedule(context, prefs)
            }
            return
        }
        if (intent?.action == ACTION_TEST) {
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
            val message = "گیرندهٔ آلارم اجرا شد و اعلان زمان‌بندی‌شده به سیستم تحویل داده شد."
            manager.notify(2030, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("قدم یار · آزمایش زمان‌بندی").setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_ALARM).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).setVibrate(longArrayOf(0, 650, 300, 650)).setAutoCancel(true).build())
            return
        }
        if (intent?.action == "ir.chillehyar.mahdi.HABIT_REMINDER") {
            val index = intent.getIntExtra("habit_index",0)
            if (!prefs.getBoolean("habit_reminder_${index}_on", false)) return
            val name = intent.getStringExtra("habit_name") ?: "عادت روزانه"
            val message = intent.getStringExtra("habit_message")?.takeIf { it.isNotBlank() } ?: MESSAGES[(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + index) % MESSAGES.size]
            val time = intent.getStringExtra("habit_time") ?: "20:00"
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
            manager.notify(10000 + index, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("قدم یار · $name").setContentText(message).setCategory(Notification.CATEGORY_ALARM).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).setVibrate(longArrayOf(0, 650, 300, 650)).setAutoCancel(true).build())
            scheduleHabit(context,index,name,time,message)
            return
        }
        if (!prefs.getBoolean("reminder_enabled", false)) return
        val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, CHANNEL) else Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH).setDefaults(Notification.DEFAULT_ALL)
        val message = MESSAGES[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % MESSAGES.size]
        manager.notify(4002, b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("قدم یار · یادآوری مسیر").setContentText(message).setStyle(Notification.BigTextStyle().bigText(message)).setCategory(Notification.CATEGORY_ALARM).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)).setVibrate(longArrayOf(0, 650, 300, 650)).setAutoCancel(true).build())
        schedule(context, prefs)
    }
    companion object {
        const val CHANNEL = "qadam_yar_reminders_v42"
        const val REQUEST_CODE = 730
        const val ACTION_TEST = "ir.chillehyar.mahdi.TEST_REMINDER"
        fun scheduleAlarm(context: Context, triggerAtMillis: Long, pendingIntent: PendingIntent) {
            val alarm = context.getSystemService(AlarmManager::class.java)
            try {
                if (Build.VERSION.SDK_INT >= 31) {
                    if (alarm.canScheduleExactAlarms()) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                    else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= 23) {
                    alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else if (Build.VERSION.SDK_INT >= 19) {
                    alarm.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (_: SecurityException) {
                // If exact-alarm access changed between checking and scheduling, retain a best-effort alarm.
                try { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent) }
                catch (_: Exception) { alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent) }
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
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED || intent?.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED || intent?.action == Intent.ACTION_TIME_CHANGED || intent?.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val prefs = context.getSharedPreferences("chillehyar_native_v30", Context.MODE_PRIVATE)
            ReminderReceiver.schedule(context, prefs)
            val defaults = listOf("قرائت قرآن", "نماز اول وقت", "ذکر و دعا")
            val extras = (prefs.getString("custom_habits", "") ?: "").split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            (defaults + extras).forEachIndexed { index, name ->
                if (prefs.getBoolean("habit_reminder_${index}_on", false)) ReminderReceiver.scheduleHabit(context,index,name,prefs.getString("habit_reminder_${index}_time","20:00") ?: "20:00",prefs.getString("habit_reminder_${index}_text","") ?: "یک قدم کوچک امروز هم ارزشمند است.")
            }
        }
    }
}
