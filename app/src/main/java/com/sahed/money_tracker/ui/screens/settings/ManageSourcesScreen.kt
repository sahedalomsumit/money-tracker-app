package com.sahed.money_tracker.ui.screens.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.sahed.money_tracker.ui.designsystem.components.bouncyClickable
import com.sahed.money_tracker.ui.designsystem.components.gentleEntrance
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sahed.money_tracker.data.model.MainSource
import com.sahed.money_tracker.data.model.SubSource
import com.sahed.money_tracker.ui.designsystem.components.EmeraldAlertDialog
import com.sahed.money_tracker.ui.designsystem.theme.DialogShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.viewmodel.ManageSourcesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSourcesScreen(
    viewModel: ManageSourcesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddMainDialog by remember { mutableStateOf(false) }
    var editingMainSource by remember { mutableStateOf<MainSource?>(null) }
    var deleteMainSourceConfirm by remember { mutableStateOf<MainSource?>(null) }

    var showAddSubDialog by remember { mutableStateOf(false) }
    var editingSubSource by remember { mutableStateOf<SubSource?>(null) }
    var deleteSubSourceConfirm by remember { mutableStateOf<SubSource?>(null) }

    var inputName by remember { mutableStateOf("") }

    var draggingMainSourceId by remember { mutableStateOf<String?>(null) }
    var mainSourceDragOffset by remember { mutableFloatStateOf(0f) }

    var draggingSubSourceId by remember { mutableStateOf<String?>(null) }
    var subSourceDragOffset by remember { mutableFloatStateOf(0f) }

    // Dialog: Add Main Source
    if (showAddMainDialog) {
        AlertDialog(
            onDismissRequest = { showAddMainDialog = false },
            title = { Text("Add Main Income Source") },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Source name") },
                    placeholder = { Text("e.g. Real Estate, Dividend") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            viewModel.addMainSource(inputName)
                            inputName = ""
                            showAddMainDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = inputName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMainDialog = false }) { Text("Cancel", color = EmeraldTheme.extended.subText) }
            },
            shape = DialogShape,
            containerColor = EmeraldTheme.extended.surfaceTier2
        )
    }

    // Dialog: Rename Main Source
    if (editingMainSource != null) {
        var renameText by remember(editingMainSource) { mutableStateOf(editingMainSource!!.name) }
        AlertDialog(
            onDismissRequest = { editingMainSource = null },
            title = { Text("Rename Main Source") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameMainSource(editingMainSource!!, renameText)
                            editingMainSource = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMainSource = null }) { Text("Cancel", color = EmeraldTheme.extended.subText) }
            },
            shape = DialogShape,
            containerColor = EmeraldTheme.extended.surfaceTier2
        )
    }

    // Dialog: Delete Main Source Confirmation
    if (deleteMainSourceConfirm != null) {
        EmeraldAlertDialog(
            onDismissRequest = { deleteMainSourceConfirm = null },
            onConfirm = {
                viewModel.deleteMainSource(deleteMainSourceConfirm!!.id)
                deleteMainSourceConfirm = null
            },
            title = "Delete Main Source?",
            message = "Are you sure you want to delete '${deleteMainSourceConfirm!!.name}' and its sub-sources?",
            confirmText = "Delete",
            cancelText = "Cancel",
            isDestructive = true
        )
    }

    // Dialog: Add Sub Source
    if (showAddSubDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubDialog = false },
            title = { Text("Add Sub-Source") },
            text = {
                Column {
                    Text(
                        text = "Adding to: ${uiState.selectedMainSource?.name ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldTheme.extended.subText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
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
                        if (inputName.isNotBlank()) {
                            viewModel.addSubSource(inputName)
                            inputName = ""
                            showAddSubDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = inputName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubDialog = false }) { Text("Cancel", color = EmeraldTheme.extended.subText) }
            },
            shape = DialogShape,
            containerColor = EmeraldTheme.extended.surfaceTier2
        )
    }

    // Dialog: Rename Sub Source
    if (editingSubSource != null) {
        var renameText by remember(editingSubSource) { mutableStateOf(editingSubSource!!.name) }
        AlertDialog(
            onDismissRequest = { editingSubSource = null },
            title = { Text("Rename Sub-Source") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameSubSource(editingSubSource!!, renameText)
                            editingSubSource = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                    shape = RoundedCornerShape(12.dp),
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSubSource = null }) { Text("Cancel", color = EmeraldTheme.extended.subText) }
            },
            shape = DialogShape,
            containerColor = EmeraldTheme.extended.surfaceTier2
        )
    }

    // Dialog: Delete Sub Source Confirmation
    if (deleteSubSourceConfirm != null) {
        EmeraldAlertDialog(
            onDismissRequest = { deleteSubSourceConfirm = null },
            onConfirm = {
                viewModel.deleteSubSource(deleteSubSourceConfirm!!.id)
                deleteSubSourceConfirm = null
            },
            title = "Delete Sub-Source?",
            message = "Are you sure you want to delete '${deleteSubSourceConfirm!!.name}'?",
            confirmText = "Delete",
            cancelText = "Cancel",
            isDestructive = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Income Sources",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Main Sources
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gentleEntrance(0),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Main Sources",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPalette.SoftEmerald
                    )

                    Button(
                        onClick = {
                            inputName = ""
                            showAddMainDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Main", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            itemsIndexed(uiState.mainSources, key = { _, mainSource -> mainSource.id }) { index, mainSource ->
                val isSelected = uiState.selectedMainSource?.id == mainSource.id
                val isDragging = draggingMainSourceId == mainSource.id
                val density = LocalDensity.current
                val thresholdPx = with(density) { 56.dp.toPx() }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = when {
                        isDragging -> EmeraldPalette.SoftEmerald.copy(alpha = 0.22f)
                        isSelected -> EmeraldPalette.SoftEmerald.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        if (isDragging) 1.5.dp else 1.dp,
                        when {
                            isDragging -> EmeraldPalette.EmeraldGlow
                            isSelected -> EmeraldPalette.SoftEmerald
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .gentleEntrance((index + 1).coerceAtMost(6))
                        .zIndex(if (isDragging) 10f else 1f)
                        .graphicsLayer {
                            if (isDragging) {
                                translationY = mainSourceDragOffset.coerceIn(-thresholdPx, thresholdPx)
                                scaleX = 1.02f
                                scaleY = 1.02f
                            }
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .bouncyClickable(pressedScale = 0.98f) { viewModel.selectMainSource(mainSource) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Draggable Handle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .pointerInput(mainSource.id) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                draggingMainSourceId = mainSource.id
                                                mainSourceDragOffset = 0f
                                            },
                                            onDragEnd = {
                                                draggingMainSourceId = null
                                                mainSourceDragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggingMainSourceId = null
                                                mainSourceDragOffset = 0f
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                mainSourceDragOffset += dragAmount
                                                val idx = uiState.mainSources.indexOfFirst { it.id == mainSource.id }
                                                if (idx != -1) {
                                                    if (mainSourceDragOffset > thresholdPx && idx < uiState.mainSources.size - 1) {
                                                        viewModel.moveMainSource(idx, idx + 1)
                                                        mainSourceDragOffset -= thresholdPx
                                                    } else if (mainSourceDragOffset < -thresholdPx && idx > 0) {
                                                        viewModel.moveMainSource(idx, idx - 1)
                                                        mainSourceDragOffset += thresholdPx
                                                    }
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = if (isDragging) EmeraldPalette.EmeraldGlow else EmeraldTheme.extended.subText.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isSelected) EmeraldPalette.SoftEmerald else MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mainSource.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { editingMainSource = mainSource },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { deleteMainSourceConfirm = mainSource },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = EmeraldPalette.ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Sub-Sources for selected Main Source
            item {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gentleEntrance(2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sub-Sources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPalette.SoftEmerald
                        )
                        Text(
                            text = "Under: ${uiState.selectedMainSource?.name ?: "None"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.selectedMainSource != null) {
                        Button(
                            onClick = {
                                inputName = ""
                                showAddSubDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Sub", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (uiState.subSourcesForSelected.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .gentleEntrance(3)
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sub-sources for this category yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.subSourcesForSelected, key = { _, subSource -> subSource.id }) { index, subSource ->
                    val isDragging = draggingSubSourceId == subSource.id
                    val density = LocalDensity.current
                    val thresholdPx = with(density) { 50.dp.toPx() }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDragging) EmeraldPalette.SoftEmerald.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isDragging) 1.5.dp else 1.dp,
                            if (isDragging) EmeraldPalette.EmeraldGlow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .gentleEntrance((index + 3).coerceAtMost(8))
                            .zIndex(if (isDragging) 10f else 1f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationY = subSourceDragOffset.coerceIn(-thresholdPx, thresholdPx)
                                    scaleX = 1.02f
                                    scaleY = 1.02f
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Draggable Handle
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .pointerInput(subSource.id) {
                                            detectVerticalDragGestures(
                                                onDragStart = {
                                                    draggingSubSourceId = subSource.id
                                                    subSourceDragOffset = 0f
                                                },
                                                onDragEnd = {
                                                    draggingSubSourceId = null
                                                    subSourceDragOffset = 0f
                                                },
                                                onDragCancel = {
                                                    draggingSubSourceId = null
                                                    subSourceDragOffset = 0f
                                                },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    subSourceDragOffset += dragAmount
                                                    val idx = uiState.subSourcesForSelected.indexOfFirst { it.id == subSource.id }
                                                    if (idx != -1) {
                                                        if (subSourceDragOffset > thresholdPx && idx < uiState.subSourcesForSelected.size - 1) {
                                                            viewModel.moveSubSource(idx, idx + 1)
                                                            subSourceDragOffset -= thresholdPx
                                                        } else if (subSourceDragOffset < -thresholdPx && idx > 0) {
                                                            viewModel.moveSubSource(idx, idx - 1)
                                                            subSourceDragOffset += thresholdPx
                                                        }
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Drag to reorder",
                                        tint = if (isDragging) EmeraldPalette.EmeraldGlow else EmeraldTheme.extended.subText.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = subSource.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { editingSubSource = subSource },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { deleteSubSourceConfirm = subSource },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = EmeraldPalette.ErrorRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
