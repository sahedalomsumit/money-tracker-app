package com.sahed.money_tracker.ui.screens.entry

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.ui.components.AllocationCategoryCard
import com.sahed.money_tracker.ui.designsystem.theme.DialogShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.ui.theme.DonateColor
import com.sahed.money_tracker.ui.theme.InvestColor
import com.sahed.money_tracker.ui.theme.RestColor
import com.sahed.money_tracker.ui.theme.SavingColor
import com.sahed.money_tracker.util.CurrencyHelper
import com.sahed.money_tracker.util.DateUtils
import com.sahed.money_tracker.util.MathExpressionEvaluator
import com.sahed.money_tracker.viewmodel.AddEntryViewModel

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun AddEntryScreen(
    viewModel: AddEntryViewModel,
    onEntrySaved: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var mainSourceDropdownExpanded by remember { mutableStateOf(false) }
    var subSourceDropdownExpanded by remember { mutableStateOf(false) }
    var showAddSubSourceDialog by remember { mutableStateOf(false) }
    var newSubSourceName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.entrySaved) {
        if (uiState.entrySaved) {
            Toast.makeText(context, "Income entry saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onEntrySaved()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Inline Add New Sub-Source Dialog
    if (showAddSubSourceDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubSourceDialog = false },
            title = {
                Text(
                    text = "Add New Sub-Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Adding to: ${uiState.selectedMainSource?.name ?: "Main Source"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTheme.extended.subText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newSubSourceName,
                        onValueChange = { newSubSourceName = it },
                        label = { Text("Sub-source name") },
                        placeholder = { Text("e.g. Client X, Project Y") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubSourceName.isNotBlank()) {
                            viewModel.addNewSubSourceInline(newSubSourceName)
                            newSubSourceName = ""
                            showAddSubSourceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = newSubSourceName.isNotBlank()
                ) {
                    Text("Add & Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubSourceDialog = false }) {
                    Text("Cancel", color = EmeraldTheme.extended.subText)
                }
            },
            shape = DialogShape,
            containerColor = EmeraldTheme.extended.surfaceTier2
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // Pinned/Fixed Header Row with Close Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Income Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = {
                            focusManager.clearFocus()
                            onDismiss()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Scrollable form body below fixed header
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Month and Year Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Month selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    monthDropdownExpanded = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateUtils.getMonthShortName(uiState.month),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            (1..12).forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(DateUtils.getMonthFullName(m)) },
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.setMonth(m)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Year selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Year",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    yearDropdownExpanded = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.year.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            DateUtils.getAvailableYears().forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.setYear(y)
                                        yearDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Net Salary Input with Inline Math Expressions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Net Salary (${uiState.userProfile.currencySymbol.ifBlank { "$" }})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (MathExpressionEvaluator.containsOperator(uiState.salaryInput) && uiState.salaryAmount > 0.0) {
                        Surface(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.evaluateAndApplySalary()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldPalette.SoftEmerald.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "= ${CurrencyHelper.format(uiState.salaryAmount, uiState.userProfile.currencySymbol.ifBlank { "$" }, uiState.userProfile.currencyCode)} (Tap to apply)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPalette.SoftEmerald
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                var isSalaryFocused by remember { mutableStateOf(false) }
                val salaryFocusRequester = remember { FocusRequester() }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSalaryFocused) EmeraldPalette.SoftEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                salaryFocusRequester.requestFocus()
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.salaryInput.isEmpty()) {
                                Text(
                                    text = "905.95 or e.g. (33+17)-10",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = uiState.salaryInput,
                                onValueChange = { input ->
                                    if (input.endsWith("=")) {
                                        viewModel.evaluateAndApplySalary()
                                        focusManager.clearFocus()
                                    } else {
                                        viewModel.setSalaryInput(input)
                                    }
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(EmeraldPalette.SoftEmerald),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        viewModel.evaluateAndApplySalary()
                                        focusManager.clearFocus()
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(salaryFocusRequester)
                                    .onFocusChanged { isSalaryFocused = it.isFocused }
                            )
                        }

                        if (MathExpressionEvaluator.containsOperator(uiState.salaryInput) && uiState.salaryAmount > 0.0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.evaluateAndApplySalary()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPalette.SoftEmerald.copy(alpha = 0.20f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPalette.SoftEmerald.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "= ${MathExpressionEvaluator.formatResult(uiState.salaryAmount)}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPalette.SoftEmerald
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Main Source Dropdown
                Text(
                    text = "Main Source *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                focusManager.clearFocus()
                                mainSourceDropdownExpanded = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedMainSource?.name ?: "Select Main Source",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (uiState.selectedMainSource != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = mainSourceDropdownExpanded,
                        onDismissRequest = { mainSourceDropdownExpanded = false }
                    ) {
                        uiState.mainSources.forEach { source ->
                            DropdownMenuItem(
                                text = { Text(source.name) },
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.selectMainSource(source)
                                    mainSourceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Sub-Source Dropdown with inline "+ Add New"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sub-Source (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.selectedMainSource != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    showAddSubSourceDialog = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldPalette.SoftEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add new",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPalette.SoftEmerald
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                focusManager.clearFocus()
                                subSourceDropdownExpanded = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.selectedSubSource?.name ?: "None / Direct",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (uiState.selectedSubSource != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = subSourceDropdownExpanded,
                        onDismissRequest = { subSourceDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None / Direct") },
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.selectSubSource(null)
                                subSourceDropdownExpanded = false
                            }
                        )
                        uiState.subSources.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.selectSubSource(sub)
                                    subSourceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Allocation Breakdown Preview
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Live Allocation Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val salary = uiState.salaryAmount
                        val alloc = uiState.allocationSettings
                        val symbol = uiState.userProfile.currencySymbol.ifBlank { "$" }
                        val code = uiState.userProfile.currencyCode

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LivePreviewChip(
                                label = alloc.savingLabel,
                                percent = alloc.savingPercent,
                                amount = salary * (alloc.savingPercent / 100.0),
                                symbol = symbol,
                                color = SavingColor,
                                modifier = Modifier.weight(1f)
                            )
                            LivePreviewChip(
                                label = alloc.investLabel,
                                percent = alloc.investPercent,
                                amount = salary * (alloc.investPercent / 100.0),
                                symbol = symbol,
                                color = InvestColor,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LivePreviewChip(
                                label = alloc.donateLabel,
                                percent = alloc.donatePercent,
                                amount = salary * (alloc.donatePercent / 100.0),
                                symbol = symbol,
                                color = DonateColor,
                                modifier = Modifier.weight(1f)
                            )
                            LivePreviewChip(
                                label = alloc.restLabel,
                                percent = alloc.restPercent,
                                amount = salary * (alloc.restPercent / 100.0),
                                symbol = symbol,
                                color = RestColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Save Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.saveEntry()
                    },
                    enabled = uiState.isValid && !uiState.isSaving,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPalette.SoftEmerald,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Entry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun LivePreviewChip(
    label: String,
    percent: Double,
    amount: Double,
    symbol: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "${percent.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$symbol ${String.format(java.util.Locale.US, "%,.2f", amount)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
