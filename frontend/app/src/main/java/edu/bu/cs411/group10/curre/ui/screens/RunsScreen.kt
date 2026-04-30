package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.components.BottomNavTab
import edu.bu.cs411.group10.curre.ui.components.CurreBottomBar
import edu.bu.cs411.group10.curre.ui.components.StatCard
import edu.bu.cs411.group10.curre.ui.model.RunDto
import edu.bu.cs411.group10.curre.ui.theme.CurreBackground
import edu.bu.cs411.group10.curre.ui.theme.CurreIconMuted
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreStreakCard
import edu.bu.cs411.group10.curre.ui.theme.CurreSurface
import edu.bu.cs411.group10.curre.ui.theme.CurreSurfaceSoft
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Date filter options for the Runs page.
enum class DateFilterOption(val label: String) {
    ALL("All Dates"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

// Duration filter options for the Runs page.
enum class DurationFilterOption(val label: String) {
    ALL("All Times"),
    UNDER_15("Under 15 Min"),
    BETWEEN_15_30("15–30 Min"),
    OVER_30("Over 30 Min")
}

@Composable
fun RunsScreen(
    runs: List<RunDto>,
    onHomeClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onStartRunClick: () -> Unit,
    onRunsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRunClick: (Long) -> Unit
) {
    // Current selected filters.
    var dateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var durationFilter by remember { mutableStateOf(DurationFilterOption.ALL) }

    // Filter and sort the runs whenever the source data or filters change.
    val filteredRuns = remember(runs, dateFilter, durationFilter) {
        runs
            .filter { matchesDateFilter(it, dateFilter) }
            .filter { matchesDurationFilter(it, durationFilter) }
            .sortedByDescending { it.startedAt }
    }

    // Summary values shown at the top of the Runs page.
    val totalRuns = filteredRuns.size
    val totalMiles = filteredRuns.sumOf { it.distanceMiles }
    val avgMiles = if (filteredRuns.isNotEmpty()) totalMiles / filteredRuns.size else 0.0

    Scaffold(
        containerColor = CurreBackground,
        bottomBar = {
            CurreBottomBar(
                selectedTab = BottomNavTab.RUNS,
                onHomeClick = onHomeClick,
                onSafetyClick = onSafetyClick,
                onStartRunClick = onStartRunClick,
                onRunsClick = onRunsClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CurreBackground)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Page title
            item {
                Text(
                    text = "Run Summary",
                    color = CurreNavy,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "",
                        value = totalRuns.toString(),
                        subtitle = "Total Runs",
                        backgroundColor = CurreStreakCard,
                        contentColor = CurreNavy
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "",
                        value = String.format("%.1f", totalMiles),
                        subtitle = "Total Miles",
                        backgroundColor = CurreSurface,
                        contentColor = CurreNavy
                    )

                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "",
                        value = String.format("%.1f", avgMiles),
                        subtitle = "Avg Miles",
                        backgroundColor = CurreSurface,
                        contentColor = CurreNavy
                    )
                }
            }

            // Filter controls
            item {
                FilterRow(
                    selectedDateFilter = dateFilter,
                    selectedDurationFilter = durationFilter,
                    onDateFilterChange = { dateFilter = it },
                    onDurationFilterChange = { durationFilter = it }
                )
            }

            // Runs list section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CurreSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "All runs",
                                tint = CurreOrange
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "All Runs",
                                color = CurreNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredRuns.isEmpty()) {
                            Text(
                                text = "No runs match the selected filters.",
                                color = CurreTextMuted,
                                fontSize = 15.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                filteredRuns.forEach { run ->
                                    RunListItem(
                                        run = run,
                                        onClick = {
                                            run.id?.let(onRunClick)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedDateFilter: DateFilterOption,
    selectedDurationFilter: DurationFilterOption,
    onDateFilterChange: (DateFilterOption) -> Unit,
    onDurationFilterChange: (DurationFilterOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterDropdown(
            modifier = Modifier.weight(1f),
            label = "Date",
            selectedOption = selectedDateFilter.label,
            options = DateFilterOption.entries.map { it.label },
            onOptionSelected = { label ->
                onDateFilterChange(DateFilterOption.entries.first { it.label == label })
            }
        )

        FilterDropdown(
            modifier = Modifier.weight(1f),
            label = "Time",
            selectedOption = selectedDurationFilter.label,
            options = DurationFilterOption.entries.map { it.label },
            onOptionSelected = { label ->
                onDurationFilterChange(DurationFilterOption.entries.first { it.label == label })
            }
        )
    }
}

@Composable
private fun FilterDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    // Tracks whether the dropdown is currently open.
    var expanded by remember { mutableStateOf(false) }

    // Keeps the latest callback reference stable.
    val currentOnOptionSelected by rememberUpdatedState(onOptionSelected)

    Box(
        modifier = modifier.wrapContentSize(Alignment.TopStart)
    ) {
        // The visible filter button.
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "$label: $selectedOption",
                color = CurreNavy,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = "Open $label filter",
                tint = CurreTextMuted
            )
        }

        // Standard stable Material dropdown menu.
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CurreSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = CurreNavy
                        )
                    },
                    onClick = {
                        currentOnOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RunListItem(
    run: RunDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = CurreSurfaceSoft
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Orange pace circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CurreOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.1f", run.avgPaceSecsPerMile / 60.0),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Run summary text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${run.durationSeconds / 60} Minutes",
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(run.startedAt)),
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open run",
                tint = CurreIconMuted
            )
        }
    }
}

// Returns true if the run matches the selected date filter.
private fun matchesDateFilter(run: RunDto, filter: DateFilterOption): Boolean {
    val now = Calendar.getInstance()
    val runTime = Calendar.getInstance().apply { timeInMillis = run.startedAt }

    return when (filter) {
        DateFilterOption.ALL -> true

        DateFilterOption.THIS_WEEK -> {
            val startOfWeek = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            run.startedAt >= startOfWeek.timeInMillis
        }

        DateFilterOption.THIS_MONTH -> {
            runTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    runTime.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
    }
}

// Returns true if the run matches the selected duration filter.
private fun matchesDurationFilter(run: RunDto, filter: DurationFilterOption): Boolean {
    val minutes = run.durationSeconds / 60.0
    return when (filter) {
        DurationFilterOption.ALL -> true
        DurationFilterOption.UNDER_15 -> minutes < 15
        DurationFilterOption.BETWEEN_15_30 -> minutes in 15.0..30.0
        DurationFilterOption.OVER_30 -> minutes > 30
    }
}