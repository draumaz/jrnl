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

package com.isaakhanimann.journal.ui.tabs.journal.addingestion.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.entities.CustomSubstance
import com.isaakhanimann.journal.data.room.experiences.entities.CustomUnit
import com.isaakhanimann.journal.data.substances.AdministrationRoute
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.SuggestionRow
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.suggestion.models.Suggestion
import com.isaakhanimann.journal.ui.tabs.search.SubstanceModel
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Composable
fun AddIngestionSearchScreen(
    navigateToCheckInteractions: (substanceName: String) -> Unit,
    navigateToCheckSaferUse: (substanceName: String) -> Unit,
    navigateToChooseRoute: (substanceName: String) -> Unit,
    navigateToDose: (substanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseCustomSubstanceDose: (customSubstanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseTime: (substanceName: String, route: AdministrationRoute, dose: Double?, units: String?, isEstimate: Boolean, estimatedDoseStandardDeviation: Double?, customUnitId: Int?) -> Unit,
    navigateToCustomSubstanceChooseRoute: (customSubstanceName: String) -> Unit,
    navigateToCustomUnitChooseDose: (customUnitId: Int) -> Unit,
    navigateToAddCustomSubstanceScreen: (searchText: String) -> Unit,
    viewModel: AddIngestionSearchViewModel = hiltViewModel(),
) {
    val searchText = viewModel.searchTextFlow.collectAsState().value
    AddIngestionSearchScreen(
        navigateToCheckInteractions = navigateToCheckInteractions,
        navigateToCheckSaferUse = navigateToCheckSaferUse,
        navigateToChooseRoute = navigateToChooseRoute,
        navigateToCustomDose = navigateToChooseCustomSubstanceDose,
        navigateToCustomSubstanceChooseRoute = navigateToCustomSubstanceChooseRoute,
        navigateToChooseTime = navigateToChooseTime,
        navigateToDose = navigateToDose,
        navigateToAddCustomSubstanceScreen = {
            navigateToAddCustomSubstanceScreen(searchText)
        },
        navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose,
        suggestions = viewModel.filteredSuggestions.collectAsState().value,
        filteredSubstances = viewModel.filteredSubstancesFlow.collectAsState().value,
        filteredCustomUnits = viewModel.filteredCustomUnitsFlow.collectAsState().value,
        filteredCustomSubstances = viewModel.filteredCustomSubstancesFlow.collectAsState().value
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddIngestionSearchScreen(
    navigateToCheckInteractions: (substanceName: String) -> Unit,
    navigateToChooseRoute: (substanceName: String) -> Unit,
    navigateToCheckSaferUse: (substanceName: String) -> Unit,
    navigateToDose: (substanceName: String, route: AdministrationRoute) -> Unit,
    navigateToCustomDose: (customSubstanceName: String, route: AdministrationRoute) -> Unit,
    navigateToChooseTime: (substanceName: String, route: AdministrationRoute, dose: Double?, units: String?, isEstimate: Boolean, estimatedDoseStandardDeviation: Double?, customUnitId: Int?) -> Unit,
    navigateToCustomSubstanceChooseRoute: (customSubstanceName: String) -> Unit,
    navigateToAddCustomSubstanceScreen: () -> Unit,
    navigateToCustomUnitChooseDose: (customUnitId: Int) -> Unit,
    suggestions: List<Suggestion>,
    filteredSubstances: List<SubstanceModel>,
    filteredCustomUnits: List<CustomUnit>,
    filteredCustomSubstances: List<CustomSubstance>
) {
    Scaffold(
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            LinearProgressIndicator(
                progress = { 0.17f },
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { },
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    stickyHeader {
                        SectionHeader(title = "Quick logging")
                    }
                }
                itemsIndexed(suggestions) { index, suggestion ->
                    SuggestionRow(
                        suggestion = suggestion,
                        navigateToDose = navigateToDose,
                        navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose,
                        navigateToCustomDose = navigateToCustomDose,
                        navigateToChooseTime = navigateToChooseTime
                    )
                    if (index < (suggestions.size - 1)) {
                        HorizontalDivider()
                    }
                }
                if (filteredCustomSubstances.isNotEmpty()) {
                    stickyHeader {
                        SectionHeader(title = "Custom substances")
                    }
                }
                itemsIndexed(filteredCustomSubstances) { index, customSubstance ->
                    SubstanceRowAddIngestion(
                        substanceModel = SubstanceModel(
                            name = customSubstance.name,
                            commonNames = emptyList(),
                            categories = emptyList(),
                            hasSaferUse = false,
                            hasInteractions = false
                        ), 
                        onTap = {
                            navigateToCustomSubstanceChooseRoute(customSubstance.name)
                        }
                    )
                    if (index < (filteredCustomSubstances.size - 1)) {
                        HorizontalDivider()
                    }
                }
                if (filteredCustomUnits.isNotEmpty()) {
                    stickyHeader {
                        SectionHeader(title = "Custom units")
                    }
                }
                itemsIndexed(filteredCustomUnits) { index, customUnit ->
                    CustomUnitRowAddIngestion(
                        customUnit = customUnit,
                        navigateToCustomUnitChooseDose = navigateToCustomUnitChooseDose)
                    if (index < (filteredCustomUnits.size - 1)) {
                        HorizontalDivider()
                    }
                }
                items(filteredSubstances) { substance ->
                    SubstanceRowAddIngestion(substanceModel = substance, onTap = {
                        if (substance.hasSaferUse) {
                            navigateToCheckSaferUse(substance.name)
                        } else if (substance.hasInteractions) {
                            navigateToCheckInteractions(substance.name)
                        } else {
                            navigateToChooseRoute(substance.name)
                        }
                    })
                    HorizontalDivider()
                }
                item {
                    HorizontalDivider()
                    TextButton(
                        onClick = navigateToAddCustomSubstanceScreen,
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    ) {
                        Icon(
                            Icons.Outlined.Add, contentDescription = "Add"
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = "Add custom substance")
                    }
                    HorizontalDivider()
                }
                item {
                    if (filteredSubstances.isEmpty() && filteredCustomSubstances.isEmpty()) {
                        Text("No matching substance found", modifier = Modifier.padding(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 5.dp)
                    .fillMaxWidth(),
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}


@Composable
fun ColorCircle(adaptiveColor: AdaptiveColor) {
    val isDarkTheme = isSystemInDarkTheme()
    Surface(
        shape = CircleShape,
        color = adaptiveColor.getComposeColor(isDarkTheme),
        modifier = Modifier.size(25.dp)
    ) {}
}