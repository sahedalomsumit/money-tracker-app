package com.sahed.money_tracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.data.model.IncomeEntry
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.ui.theme.ChartWorstMonth
import com.sahed.money_tracker.util.DateUtils

@Composable
fun EditEntryDialog(
    entry: IncomeEntry,
    mainSources: List<MainSource>,
    subSources: List<SubSource>,
    currencySymbol: String,
    onFetchSubSources: (String) -> Unit,
    onSave: (IncomeEntry) -> Unit,
    onDelete: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var salaryText by remember { mutableStateOf(if (entry.netSalary % 1.0 == 0.0) entry.netSalary.toLong().toString() else entry.netSalary.toString()) }
    var selectedMonth by remember { mutableIntStateOf(entry.month) }
    var selectedYear by remember { mutableIntStateOf(entry.year) }
    var selectedMainSourceId by remember { mutableStateOf(entry.mainSourceId) }
    var selectedMainSourceName by remember { mutableStateOf(entry.mainSourceName) }
    var selectedSubSourceId by remember { mutableStateOf(entry.subSourceId) }
    var selectedSubSourceName by remember { mutableStateOf(entry.subSourceName) }

    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var mainSourceDropdownExpanded by remember { mutableStateOf(false) }
    var subSourceDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMainSourceId) {
        if (selectedMainSourceId.isNotBlank()) {
            onFetchSubSources(selectedMainSourceId)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to permanently delete this income entry?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(entry.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChartWorstMonth)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Income Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = ChartWorstMonth)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Month and Year Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Month dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Month",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = DateUtils.getMonthShortName(selectedMonth),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { monthDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { monthDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            (1..12).forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(DateUtils.getMonthFullName(m)) },
                                    onClick = {
                                        selectedMonth = m
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Year dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Year",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { yearDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { yearDropdownExpanded = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = yearDropdownExpanded,
                            onDismissRequest = { yearDropdownExpanded = false }
                        ) {
                            val currentY = DateUtils.getCurrentYear()
                            (currentY - 5..currentY + 2).reversed().forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        selectedYear = y
                                        yearDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Net Salary input
                Text(
                    text = "Net Salary ($currencySymbol)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main Source dropdown
                Text(
                    text = "Main Source",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = selectedMainSourceName.ifBlank { "Select Main Source" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { mainSourceDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mainSourceDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = mainSourceDropdownExpanded,
                    onDismissRequest = { mainSourceDropdownExpanded = false }
                ) {
                    mainSources.forEach { source ->
                        DropdownMenuItem(
                            text = { Text(source.name) },
                            onClick = {
                                selectedMainSourceId = source.id
                                selectedMainSourceName = source.name
                                selectedSubSourceId = null
                                selectedSubSourceName = null
                                mainSourceDropdownExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sub-Source dropdown
                Text(
                    text = "Sub-Source (Optional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = selectedSubSourceName ?: "None / Direct",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { subSourceDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { subSourceDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = subSourceDropdownExpanded,
                    onDismissRequest = { subSourceDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None / Direct") },
                        onClick = {
                            selectedSubSourceId = null
                            selectedSubSourceName = null
                            subSourceDropdownExpanded = false
                        }
                    )
                    subSources.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub.name) },
                            onClick = {
                                selectedSubSourceId = sub.id
                                selectedSubSourceName = sub.name
                                subSourceDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = salaryText.toDoubleOrNull() ?: 0.0
                    if (salary > 0 && selectedMainSourceName.isNotBlank()) {
                        val updated = entry.copy(
                            month = selectedMonth,
                            year = selectedYear,
                            netSalary = salary,
                            mainSourceId = selectedMainSourceId,
                            mainSourceName = selectedMainSourceName,
                            subSourceId = selectedSubSourceId,
                            subSourceName = selectedSubSourceName
                        )
                        onSave(updated)
                    }
                },
                enabled = (salaryText.toDoubleOrNull() ?: 0.0) > 0 && selectedMainSourceName.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
