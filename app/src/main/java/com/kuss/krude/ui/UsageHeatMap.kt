package com.kuss.krude.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeek
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.kuss.krude.db.AppInfo
import com.kuss.krude.ui.components.AppItem
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private enum class Level(val color: Color) {
    Zero(Color(0xFFEBEDF0)),
    One(Color(0xFF9BE9A8)),
    Two(Color(0xFF40C463)),
    Three(Color(0xFF30A14E)),
    Four(Color(0xFF216E3A)),
    Five(Color(0xFF1A4E2C));
}

private fun findColorLevel(count: Int): Level {
    return when {
        count == 0 -> Level.Zero
        count in 1..20 -> Level.One
        count in 20..40 -> Level.Two
        count in 40..60 -> Level.Three
        count in 60..80 -> Level.Four
        count >=80 -> Level.Five
        else -> Level.Five
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsageHeatMap(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val endDate = remember { LocalDate.now() }
    // GitHub only shows contributions for the past 12 months
    val startDate = remember { endDate.minusMonths(12) }
    val data = remember { mutableStateOf<Map<LocalDate, Level>>(emptyMap()) }
    var selection by remember { mutableStateOf<Pair<LocalDate, Level>?>(null) }
    LaunchedEffect(startDate, endDate) {
        selection = Pair(endDate, Level.Zero)

        data.value = withContext(Dispatchers.IO) {
            val result = mainViewModel.getUsageCountByDay(context)
            Timber.d("UsageHeatMap: $result")
            result.associateTo(hashMapOf()) {
                LocalDate.parse(it.day) to findColorLevel(it.count)
            }
        }
    }
    val state = rememberHeatMapCalendarState(
        startMonth = startDate.yearMonth,
        endMonth = endDate.yearMonth,
        firstVisibleMonth = endDate.yearMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale(),
    )
    HeatMapCalendar(
        modifier = Modifier.padding(vertical = 10.dp),
        state = state,
        contentPadding = PaddingValues(end = 6.dp),
        dayContent = { day, week ->
            Day(
                day = day,
                startDate = startDate,
                endDate = endDate,
                week = week,
                level = data.value[day.date] ?: Level.Zero,
            ) { clicked ->
                selection = Pair(clicked, data.value[clicked] ?: Level.Zero)
            }
        },
        weekHeader = { WeekHeader(it) },
        monthHeader = { MonthHeader(it, endDate, state) },
    )
    if (selection != null) {
        val (date, _) = selection!!
        val selectedDayData = remember { mutableStateOf<List<AppInfo>>(emptyList()) }

        LaunchedEffect(selectedDayData, selection) {
            withContext(Dispatchers.IO) {
                selectedDayData.value = mainViewModel.getAppsByDay(context, date.toString())
            }
        }

        LazyColumn{
            val items = selectedDayData.value
            if (items.isNotEmpty()) {
                stickyHeader {
                    Text(text = "$date, ${items.size} Activities", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacing(x = 1)
                }
                items(items.size) { index ->
                    val item = items[index]
                    AppItem(item = item,
                        onClick = {
//                                openApp(item)
                        }, onLongClick = {
//                                toAppDetail(item)
                        })
                    Spacing(2)

                }
            } else {
                item {
                    Text(text = "No data")
                }
            }
        }
    }
}


private val daySize = 18.dp

@Composable
private fun Day(
    day: CalendarDay,
    startDate: LocalDate,
    endDate: LocalDate,
    week: HeatMapWeek,
    level: Level,
    onClick: (LocalDate) -> Unit,
) {
    // We only want to draw boxes on the days that are in the
    // past 12 months. Since the calendar is month-based, we ignore
    // the future dates in the current month and those in the start
    // month that are older than 12 months from today.
    // We draw a transparent box on the empty spaces in the first week
    // so the items are laid out properly as the column is top to bottom.
    val weekDates = week.days.map { it.date }
    if (day.date in startDate..endDate) {
        LevelBox(level.color) { onClick(day.date) }
    } else if (weekDates.contains(startDate)) {
        LevelBox(Color.Transparent)
    }
}

@Composable
private fun LevelBox(color: Color, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(daySize) // Must set a size on the day.
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = color)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    )
}

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier
            .height(daySize) // Must set a height on the day of week so it aligns with the day.
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = dayOfWeek.displayText(),
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate,
    state: HeatMapCalendarState,
) {
    val density = LocalDensity.current
    val firstFullyVisibleMonth by remember {
        // Find the first index with at most one box out of bounds.
        derivedStateOf { getMonthWithYear(state.layoutInfo, daySize, density) }
    }
    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        val title = if (month == firstFullyVisibleMonth) {
            month.displayText(short = true)
        } else {
            month.month.displayText()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 1.dp, start = 2.dp),
        ) {
            Text(text = title, fontSize = 10.sp)
        }
    }
}

fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale.ENGLISH)
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}


// Find the first index with at most one box out of bounds.
private fun getMonthWithYear(
    layoutInfo: CalendarLayoutInfo,
    daySize: Dp,
    density: Density,
): YearMonth? {
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month.yearMonth
        else -> {
            val firstItem = visibleItemsInfo.first()
            val daySizePx = with(density) { daySize.toPx() }
            if (
                firstItem.size < daySizePx * 3 || // Ensure the Month + Year text can fit.
                firstItem.offset < layoutInfo.viewportStartOffset && // Ensure the week row size - 1 is visible.
                (layoutInfo.viewportStartOffset - firstItem.offset > daySizePx)
            ) {
                visibleItemsInfo[1].month.yearMonth
            } else {
                firstItem.month.yearMonth
            }
        }
    }
}