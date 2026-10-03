package com.example.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitEntity
import com.example.data.model.HabitSnapshot
import com.example.data.model.HabitType
import com.example.ui.components.HabitTypeBadge
import com.example.ui.theme.AmberAttention
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CellBorder
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessContainer
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TableBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HabitUiState
import kotlin.math.roundToInt

@Composable
fun HabitsScreen(
    state: HabitUiState,
    onAddHabitClick: () -> Unit,
    onEditHabit: (HabitEntity) -> Unit,
    onToggleActive: (habitId: String, currentActive: Boolean) -> Unit,
    onMoveOrder: (habitId: String, direction: Int) -> Unit,
    onDeleteHabit: (habitId: String) -> Unit,
    onHabitClick: (HabitSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = Archived
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    val habitsToDisplay = if (selectedTab == 0) {
        state.allHabits.filter { it.isActive }
    } else {
        state.allHabits.filter { !it.isActive }
    }

    Box(modifier = modifier.fillMaxSize().background(ObsidianBg)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FormatListBulleted,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "HABIT MANAGEMENT",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Customize habits, targets, tracking methods & priorities",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Tabs: Active vs Archived
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardSurface,
                    contentColor = EmeraldSuccess,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EmeraldSuccess
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.heightIn(min = 48.dp),
                        text = {
                            Text(
                                "Active (${state.allHabits.count { it.isActive }})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.heightIn(min = 48.dp),
                        text = {
                            Text(
                                "Archived (${state.allHabits.count { !it.isActive }})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            if (habitsToDisplay.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (selectedTab == 0) "No active habits." else "No archived habits.",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (selectedTab == 0) "Tap the '+' button below to create your first habit."
                                else "Archived habits stay intact in historical weeks without appearing in your active tracker.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(habitsToDisplay.size) { idx ->
                    val habit = habitsToDisplay[idx]
                    HabitManagementCard(
                        habit = habit,
                        canMoveUp = idx > 0 && selectedTab == 0,
                        canMoveDown = idx < habitsToDisplay.size - 1 && selectedTab == 0,
                        onMoveUp = { onMoveOrder(habit.id, -1) },
                        onMoveDown = { onMoveOrder(habit.id, 1) },
                        onEdit = { onEditHabit(habit) },
                        onToggleArchive = { onToggleActive(habit.id, habit.isActive) },
                        onDelete = { habitToDelete = habit },
                        onClick = { onHabitClick(HabitSnapshot.fromEntity(habit)) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add New Habit
        FloatingActionButton(
            onClick = onAddHabitClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_habit_fab"),
            containerColor = EmeraldSuccess,
            contentColor = ObsidianBg
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit")
        }
    }

    // Delete Confirmation Dialog
    if (habitToDelete != null) {
        val h = habitToDelete!!
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit?", color = CoralError, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Prefer archiving instead! Deleting \"${h.name}\" may permanently remove its historical records from previous weeks. Are you sure?",
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(h.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                ) {
                    Text("Delete Permanently", color = ObsidianBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
fun HabitManagementCard(
    habit: HabitEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TableBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("manage_habit_${habit.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(habit.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HabitTypeBadge(type = habit.trackingType)
                            Text(
                                text = "· ${habit.category} · Target: ${habit.weeklyTarget}d/wk",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onToggleArchive,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (habit.isActive) Icons.Default.Archive else Icons.Default.Unarchive,
                            contentDescription = if (habit.isActive) "Archive" else "Reactivate",
                            tint = if (habit.isActive) TextSecondary else EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (habit.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = habit.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Order manipulation row if active
            if (habit.isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap card to view history deep-dive",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue,
                        fontSize = 10.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (canMoveUp) {
                            IconButton(
                                onClick = onMoveUp,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (canMoveDown) {
                            IconButton(
                                onClick = onMoveDown,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog for creating or modifying a habit entity.
 */
@Composable
fun HabitEditDialog(
    habit: HabitEntity,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    var name by remember { mutableStateOf(habit.name) }
    var icon by remember { mutableStateOf(habit.icon) }
    var category by remember { mutableStateOf(habit.category) }
    var trackingType by remember { mutableStateOf(habit.trackingType) }
    var weeklyTarget by remember { mutableIntStateOf(habit.weeklyTarget) }
    var dailyTargetText by remember { mutableStateOf(habit.dailyTarget.toString()) }
    var unit by remember { mutableStateOf(habit.unit) }
    var notes by remember { mutableStateOf(habit.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Add New Habit" else "Edit Habit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    // Free-form Emoji / Icon Picker
                    Text("Icon / Emoji", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current preview box
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldSuccessContainer)
                                .border(1.dp, EmeraldSuccess, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = icon.ifBlank { "🎯" },
                                fontSize = 24.sp
                            )
                        }

                        // Freeform input textfield
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("Emoji or Symbol") },
                            placeholder = { Text("Type/paste emoji (🏃‍♂️, 🧘‍♀️, ⚡)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("habit_icon_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Quick Presets (or type your own above):", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("🏃‍♂️", "🧘‍♀️", "📚", "💧", "🎯", "⚡", "💻").forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (icon == emo) EmeraldSuccessContainer else SurfaceElevated)
                                    .border(
                                        0.5.dp,
                                        if (icon == emo) EmeraldSuccess else TableBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { icon = emo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("🧠", "🌿", "📵", "🏋️", "📝", "🛁", "🌙").forEach { emo ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (icon == emo) EmeraldSuccessContainer else SurfaceElevated)
                                    .border(
                                        0.5.dp,
                                        if (icon == emo) EmeraldSuccess else TableBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { icon = emo },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emo, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Habit Name") },
                        placeholder = { Text("e.g. Exercise, Deep Work") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        placeholder = { Text("Health, Productivity, Mindset, Routine, Discipline") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tracking Type Selector
                    Text("Tracking Type", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HabitType.values().forEach { type ->
                            val isSel = trackingType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) ElectricBlue else SurfaceElevated)
                                    .clickable {
                                        trackingType = type
                                        if (type == HabitType.LIMIT && unit.isBlank()) unit = "hrs"
                                        if (type == HabitType.DURATION && unit.isBlank()) unit = "mins"
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.name.take(4),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) ObsidianBg else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Target (1 to 7 days)
                    Text("Weekly Target: $weeklyTarget days / week", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Slider(
                        value = weeklyTarget.toFloat(),
                        onValueChange = { weeklyTarget = it.roundToInt() },
                        valueRange = 1f..7f,
                        steps = 5
                    )

                    if (trackingType != HabitType.BOOLEAN) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dailyTargetText,
                                onValueChange = { dailyTargetText = it },
                                label = { Text("Daily Target") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit") },
                                placeholder = { Text("hrs, mins, L") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val parsedDaily = dailyTargetText.toFloatOrNull() ?: 1f
                        onSave(
                            habit.copy(
                                name = name.trim(),
                                icon = icon.ifBlank { "🎯" },
                                category = category.trim().ifBlank { "General" },
                                trackingType = trackingType,
                                weeklyTarget = weeklyTarget,
                                dailyTarget = parsedDaily,
                                unit = unit.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("save_habit_button")
            ) {
                Text("Save Habit", color = ObsidianBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CardSurface
    )
}
