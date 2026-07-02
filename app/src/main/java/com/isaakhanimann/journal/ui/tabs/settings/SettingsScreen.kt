/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.ui.VERSION_NAME
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import kotlinx.coroutines.launch
import java.time.Instant

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(
        deleteEverything = {},
        navigateToFAQ = {},
        navigateToComboSettings = {},
        navigateToSubstanceColors = {},
        navigateToCustomUnits = {},
        navigateToDonate = {},
        importFile = {},
        exportFile = {},
        snackbarHostState = remember { SnackbarHostState() },
        areDosageDotsHidden = false,
        saveDosageDotsAreHidden = {},
        isTimelineHidden = false,
        saveIsTimelineHidden = {},
        areSubstanceHeightsIndependent = false,
        saveAreSubstanceHeightsIndependent = {},
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToDonate: () -> Unit,
) {
    SettingsScreen(
        navigateToFAQ = navigateToFAQ,
        navigateToComboSettings = navigateToComboSettings,
        navigateToSubstanceColors = navigateToSubstanceColors,
        navigateToCustomUnits = navigateToCustomUnits,
        navigateToDonate = navigateToDonate,
        deleteEverything = viewModel::deleteEverything,
        importFile = viewModel::importFile,
        exportFile = viewModel::exportFile,
        snackbarHostState = viewModel.snackbarHostState,
        areDosageDotsHidden = viewModel.areDosageDotsHiddenFlow.collectAsState().value,
        saveDosageDotsAreHidden = viewModel::saveDosageDotsAreHidden,
        isTimelineHidden = viewModel.isTimelineHiddenFlow.collectAsState().value,
        saveIsTimelineHidden = viewModel::saveIsTimelineHidden,
        areSubstanceHeightsIndependent = viewModel.areSubstanceHeightsIndependentFlow.collectAsState().value,
        saveAreSubstanceHeightsIndependent = viewModel::saveAreSubstanceHeightsIndependent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    navigateToSubstanceColors: () -> Unit,
    navigateToCustomUnits: () -> Unit,
    navigateToDonate: () -> Unit,
    deleteEverything: () -> Unit,
    importFile: (uri: Uri) -> Unit,
    exportFile: (uri: Uri) -> Unit,
    snackbarHostState: SnackbarHostState,
    areDosageDotsHidden: Boolean,
    saveDosageDotsAreHidden: (Boolean) -> Unit,
    isTimelineHidden: Boolean,
    saveIsTimelineHidden: (Boolean) -> Unit,
    areSubstanceHeightsIndependent: Boolean,
    saveAreSubstanceHeightsIndependent: (Boolean) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp) // Space for floating bar
        ) {
            SettingsSection(title = "Interface") {
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.Medication,
                    text = "Custom units",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    onClick = navigateToCustomUnits
                )
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.Palette,
                    text = "Substance colors",
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    onClick = navigateToSubstanceColors
                )
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.WarningAmber,
                    text = "Interaction settings",
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    onClick = navigateToComboSettings
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                ExpressiveSwitchButton(
                    text = "Hide dosage dots",
                    checked = areDosageDotsHidden,
                    onCheckedChange = saveDosageDotsAreHidden
                )
                ExpressiveSwitchButton(
                    text = "Hide timeline",
                    checked = isTimelineHidden,
                    onCheckedChange = saveIsTimelineHidden
                )
                
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var showBottomSheet by remember { mutableStateOf(false) }
                
                ExpressiveSwitchButton(
                    text = "Independent substance heights",
                    checked = areSubstanceHeightsIndependent,
                    onCheckedChange = saveAreSubstanceHeightsIndependent,
                    onInfoClick = { showBottomSheet = true }
                )

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState
                    ) {
                        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text(
                                text = "Independent substance heights",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = """
                                    Enable this setting if you want the timeline of different substances and routes of administration (roas) to be independent. Then ingestions of different substances and roas will always take the full height of the timeline.
                                    
                                    If this setting is disabled then timelines of different substances have a height relative to each other. In that case the average of the common dose is used as the point to compare it to.
                                    E.g. if the oral average common dose of MDMA is 100mg and the average common dose of insufflated MDMA is 50mg then the timeline for 100mg of oral MDMA is the same height as for 50mg of insufflated MDMA.
                                    This is also applied across substances. E.g. if the common dose of oral 2C-B is 20mg then the timeline of 40mg oral 2C-B will be twice as high as 100mg of oral MDMA.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }

            SettingsSection(title = "App data") {
                var isShowingExportDialog by remember { mutableStateOf(false) }
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.FileUpload,
                    text = "Export database",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { isShowingExportDialog = true }
                )
                
                val jsonMIMEType = "application/json"
                val launcherExport = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(mimeType = jsonMIMEType)
                ) { uri -> if (uri != null) exportFile(uri) }

                if (isShowingExportDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingExportDialog = false },
                        title = { Text(text = "Export database?") },
                        text = { Text("This will export all your data from the app into a file so you can send it to someone or import it again on a new phone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                isShowingExportDialog = false
                                launcherExport.launch("Journal ${Instant.now().getStringOfPattern("dd MMM yyyy")}.json")
                            }) { Text("Export") }
                        },
                        dismissButton = {
                            TextButton(onClick = { isShowingExportDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                var isShowingImportDialog by remember { mutableStateOf(false) }
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.FileDownload,
                    text = "Import database",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { isShowingImportDialog = true }
                )
                
                val launcherImport = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri -> if (uri != null) importFile(uri) }

                if (isShowingImportDialog) {
                    AlertDialog(
                        onDismissRequest = { isShowingImportDialog = false },
                        title = { Text(text = "Import database?") },
                        text = { Text("Import a file that was exported before. Note that this will delete all current data in the app.") },
                        confirmButton = {
                            TextButton(onClick = {
                                isShowingImportDialog = false
                                launcherImport.launch(jsonMIMEType)
                            }) { Text("Import") }
                        },
                        dismissButton = {
                            TextButton(onClick = { isShowingImportDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                var isShowingDeleteDialog by remember { mutableStateOf(false) }
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.DeleteForever,
                    text = "Delete everything",
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    onClick = { isShowingDeleteDialog = true }
                )
                
                if (isShowingDeleteDialog) {
                    val scope = rememberCoroutineScope()
                    AlertDialog(
                        onDismissRequest = { isShowingDeleteDialog = false },
                        title = { Text(text = "Delete everything?") },
                        text = { Text("This will permanently delete all your experiences, ingestions and custom substances.") },
                        confirmButton = {
                            TextButton(onClick = {
                                isShowingDeleteDialog = false
                                deleteEverything()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Deleted everything",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { isShowingDeleteDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            SettingsSection(title = "Community & Support") {
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.QuestionAnswer,
                    text = "Frequently Asked Questions",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = navigateToFAQ
                )
                ExpressiveSettingsItem(
                    imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                    text = "Question or Bug report",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { uriHandler.openUri("https://t.me/+ss8uZhBF6g00MTY8") }
                )
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.VolunteerActivism,
                    text = "Support development (Donate)",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = navigateToDonate
                )
            }

            SettingsSection(title = "About") {
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.Code,
                    text = "Source code (GitHub)",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { uriHandler.openUri("https://github.com/isaakhanimann/psychonautwiki-journal-android") }
                )
                
                val context = LocalContext.current
                ExpressiveSettingsItem(
                    imageVector = Icons.Outlined.Share,
                    text = "Share app",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, SHARE_APP_URL)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }
                )
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Version $VERSION_NAME",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun ExpressiveSettingsItem(
    imageVector: ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ExpressiveSwitchButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onInfoClick: (() -> Unit)? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        label = "backgroundColor"
    )
    
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        tonalElevation = if (checked) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (onInfoClick != null) {
                    IconButton(
                        onClick = { onInfoClick() },
                        modifier = Modifier.size(32.dp).padding(start = 8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(18.dp),
                            tint = if (checked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        }
    }
}

const val SHARE_APP_URL = "https://psychonautwiki.org/wiki/PsychonautWiki_Journal"
