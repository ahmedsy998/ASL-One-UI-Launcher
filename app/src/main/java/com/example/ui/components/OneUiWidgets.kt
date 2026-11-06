package com.example.ui.components

import android.content.Context
import android.media.AudioManager
import android.os.BatteryManager
import android.view.KeyEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BuiltInWidgetType
import com.example.model.LauncherSettings
import com.example.ui.theme.parseHexColor
import com.example.widget.LauncherWidgetHost
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BuiltInOneUiWidgetCard(
    type: BuiltInWidgetType,
    settings: LauncherSettings,
    bgColorHex: String = "#1E293B",
    alpha: Float = 0.85f,
    isHomeHeroTransparent: Boolean = false,
    onOpenSearch: () -> Unit = {},
    onOpenWeatherEdit: () -> Unit = {},
    onOpenClockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (type) {
        BuiltInWidgetType.CLOCK_WEATHER_DUAL -> ClockWeatherDualWidget(
            settings = settings,
            isTransparentHero = isHomeHeroTransparent,
            bgColorHex = bgColorHex,
            alpha = alpha,
            onClockClick = onOpenClockApp,
            onWeatherClick = onOpenWeatherEdit,
            modifier = modifier
        )

        BuiltInWidgetType.GOOGLE_SEARCH_BAR -> OneUiGoogleSearchPill(
            onClick = onOpenSearch,
            modifier = modifier
        )

        BuiltInWidgetType.ANALOG_CLOCK -> OneUiAnalogClockWidget(
            bgColorHex = bgColorHex,
            alpha = alpha,
            onClick = onOpenClockApp,
            modifier = modifier
        )

        BuiltInWidgetType.CALENDAR_MONTH -> OneUiCalendarMonthWidget(
            bgColorHex = bgColorHex,
            alpha = alpha,
            modifier = modifier
        )

        BuiltInWidgetType.BATTERY_RING -> OneUiBatteryRingWidget(
            bgColorHex = bgColorHex,
            alpha = alpha,
            modifier = modifier
        )

        BuiltInWidgetType.MUSIC_WAVE -> OneUiMusicWaveWidget(
            alpha = alpha,
            modifier = modifier
        )

        BuiltInWidgetType.WORLD_CLOCK -> OneUiWorldClockWidget(
            alpha = alpha,
            modifier = modifier
        )

        BuiltInWidgetType.WEATHER_DETAILED -> OneUiWeatherDetailedWidget(
            settings = settings,
            onClick = onOpenWeatherEdit,
            modifier = modifier
        )
    }
}

@Composable
fun ClockWeatherDualWidget(
    settings: LauncherSettings,
    isTransparentHero: Boolean,
    bgColorHex: String,
    alpha: Float,
    onClockClick: () -> Unit,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(15_000L)
        }
    }

    val timeStr = remember(nowMillis) {
        SimpleDateFormat("hh:mm", Locale.US).format(Date(nowMillis))
    }
    val dateArStr = remember(nowMillis) {
        SimpleDateFormat("EEEE، d MMMM", Locale("ar")).format(Date(nowMillis))
    }

    val shape = RoundedCornerShape(26.dp)
    val baseColor = parseHexColor(bgColorHex, Color(0xFF1D4ED8))
    val containerMod = if (isTransparentHero) {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    } else {
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        baseColor.copy(alpha = alpha),
                        Color(0xFF2563EB).copy(alpha = alpha)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    }

    Row(
        modifier = containerMod,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right side in RTL: Clock + Date + Weather summary (matches center phone top right)
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.clickable { onClockClick() }
        ) {
            Text(
                text = timeStr,
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 46.sp
            )
            Text(
                text = dateArStr,
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Left side in RTL: Weather Icon + Temperature + City
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onWeatherClick() }
                .padding(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "مشمس",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier
                            .size(26.dp)
                            .padding(bottom = 4.dp, end = 6.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "سحاب",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 6.dp, start = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "°${settings.weatherTempCelsius}",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = settings.weatherCityAr,
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun OneUiGoogleSearchPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(pillShape)
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, Color.White, pillShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "بحث صوتي",
                tint = Color(0xFF4285F4),
                modifier = Modifier.size(22.dp)
            )
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "عدسة البحث",
                tint = Color(0xFFEA4335),
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "ابحث في الهاتف أو الويب...",
            color = Color(0xFF475569),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // Iconic colorful G emblem
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
fun OneUiAnalogClockWidget(
    bgColorHex: String,
    alpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            timeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val shape = RoundedCornerShape(26.dp)
    val bgColor = parseHexColor(bgColorHex, Color(0xFF1A1E36)).copy(alpha = alpha)

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0xFF252A48)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30.0) - 90.0)
                    val isQuarter = i % 3 == 0
                    val innerR = radius * (if (isQuarter) 0.78f else 0.86f)
                    val start = Offset(
                        center.x + (innerR * cos(angle)).toFloat(),
                        center.y + (innerR * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        center.x + (radius * 0.95f * cos(angle)).toFloat(),
                        center.y + (radius * 0.95f * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = if (isQuarter) Color.White else Color.White.copy(alpha = 0.45f),
                        start = start,
                        end = end,
                        strokeWidth = if (isQuarter) 4f else 2f,
                        cap = StrokeCap.Round
                    )
                }

                val cal = Calendar.getInstance().apply { timeInMillis = timeMillis }
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE) + sec / 60f
                val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

                val hrAngle = Math.toRadians((hr * 30.0) - 90.0)
                val minAngle = Math.toRadians((min * 6.0) - 90.0)
                val secAngle = Math.toRadians((sec * 6.0) - 90.0)

                // Hour hand
                drawLine(
                    color = Color.White,
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.50f * cos(hrAngle)).toFloat(),
                        center.y + (radius * 0.50f * sin(hrAngle)).toFloat()
                    ),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                // Minute hand
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.72f * cos(minAngle)).toFloat(),
                        center.y + (radius * 0.72f * sin(minAngle)).toFloat()
                    ),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                // Second hand
                drawLine(
                    color = Color(0xFFA855F7),
                    start = center,
                    end = Offset(
                        center.x + (radius * 0.80f * cos(secAngle)).toFloat(),
                        center.y + (radius * 0.80f * sin(secAngle)).toFloat()
                    ),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color(0xFFA855F7), radius = 5f, center = center)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "الساعة",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun OneUiCalendarMonthWidget(
    bgColorHex: String,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val today = cal.get(Calendar.DAY_OF_MONTH)
    val monthNameAr = remember {
        SimpleDateFormat("MMMM", Locale("ar")).format(cal.time)
    }
    val shape = RoundedCornerShape(26.dp)
    val bgColor = parseHexColor(bgColorHex, Color(0xFF1A1E36)).copy(alpha = alpha)
    val weekDays = listOf("أحد", "إثن", "ثلاث", "أربع", "خميس", "جمع", "سبت")

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .padding(12.dp)
    ) {
        Text(
            text = monthNameAr,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { d ->
                Text(
                    text = d,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 8.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        for (week in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (dow in 1..7) {
                    val dayNum = week * 7 + dow
                    if (dayNum <= 31) {
                        val isToday = dayNum == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) Color(0xFF7C3AED) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                color = if (isToday) Color.White else Color.White.copy(alpha = 0.85f),
                                fontSize = 9.5.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun OneUiBatteryRingWidget(
    bgColorHex: String,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var batteryPct by remember { mutableIntStateOf(78) }

    LaunchedEffect(context) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val cap = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 78
        batteryPct = if (cap in 1..100) cap else 78
    }

    val shape = RoundedCornerShape(26.dp)
    val bgColor = parseHexColor(bgColorHex, Color(0xFF1A1E36)).copy(alpha = alpha)

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(74.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF10B981))
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * (batteryPct / 100f),
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "البطارية",
                tint = Color(0xFF34D399),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "%$batteryPct",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "البطارية",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun OneUiMusicWaveWidget(
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    val infiniteTransition = rememberInfiniteTransition(label = "music_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    fun sendMediaKey(keyCode: Int) {
        runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            am?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        }
    }

    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4C1D95).copy(alpha = alpha),
                        Color(0xFF2E1065).copy(alpha = alpha)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF7C3AED)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "موسيقى",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Animated Waveform Bars (matches Screen 2 Music widget)
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .padding(horizontal = 8.dp)
            ) {
                val barCount = 18
                val spacing = size.width / barCount
                for (i in 0 until barCount) {
                    val ratio = if (isPlaying) {
                        0.28f + 0.65f * kotlin.math.abs(sin(phase + i * 0.45f))
                    } else {
                        0.22f
                    }
                    val barH = size.height * ratio
                    val x = i * spacing + spacing / 2f
                    drawLine(
                        color = Color(0xFFD8B4FE),
                        start = Offset(x, (size.height - barH) / 2f),
                        end = Offset(x, (size.height + barH) / 2f),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = "السابق", tint = Color.White)
            }
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                    sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "تشغيل أو إيقاف",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "التالي", tint = Color.White)
            }
        }

        Text(
            text = "الموسيقى",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun OneUiWorldClockWidget(
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val cities = listOf(
        "دمشق" to "Asia/Damascus",
        "مكة المكرمة" to "Asia/Riyadh",
        "القاهرة" to "Africa/Cairo",
        "دبي" to "Asia/Dubai"
    )
    val now = remember { Date() }
    val shape = RoundedCornerShape(26.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF1A1E36).copy(alpha = alpha))
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        cities.forEach { (cityAr, tzId) ->
            val formatted = remember(now) {
                SimpleDateFormat("HH:mm", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone(tzId)
                }.format(now)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatted,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = cityAr,
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun OneUiWeatherDetailedWidget(
    settings: LauncherSettings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF1E3A8A))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.22f), shape)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = settings.weatherCityAr,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = settings.weatherConditionAr,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "العظمى °${settings.weatherTempCelsius + 4}  •  الصغرى °${settings.weatherTempCelsius - 6}",
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            text = "°${settings.weatherTempCelsius}",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun SystemAppWidgetCard(
    widgetHost: LauncherWidgetHost,
    appWidgetId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hostView = remember(appWidgetId) {
        widgetHost.createHostView(context, appWidgetId)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.55f))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hostView != null) {
            AndroidView(
                factory = { hostView },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "ويدجيت النظام (#$appWidgetId)",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
