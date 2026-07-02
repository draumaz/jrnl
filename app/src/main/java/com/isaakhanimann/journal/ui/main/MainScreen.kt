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

package com.isaakhanimann.journal.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.isaakhanimann.journal.ui.main.navigation.JournalTopLevelRoute
import com.isaakhanimann.journal.ui.main.navigation.graphs.AddIngestionRoute
import com.isaakhanimann.journal.ui.main.navigation.graphs.AddIngestionSearchRoute
import com.isaakhanimann.journal.ui.main.navigation.graphs.journalGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.saferGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.searchGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.settingsGraph
import com.isaakhanimann.journal.ui.main.navigation.graphs.statsGraph
import com.isaakhanimann.journal.ui.main.navigation.topLevelRoutes
import com.isaakhanimann.journal.ui.tabs.journal.addingestion.search.AddIngestionSearchViewModel
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    if (viewModel.isAcceptedFlow.collectAsState().value) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val isSearchScreenActive = currentDestination?.hasRoute(AddIngestionSearchRoute::class) == true

        var isBottomBarVisibleByScroll by remember { mutableStateOf(true) }
        val isBottomBarVisible = isBottomBarVisibleByScroll

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -10) {
                        isBottomBarVisibleByScroll = false
                    }
                    if (available.y > 10) {
                        isBottomBarVisibleByScroll = true
                    }
                    return Offset.Zero
                }
            }
        }

        val expressiveSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
        val expressiveSpringIntOffset = spring<IntOffset>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(nestedScrollConnection)
        ) {
            NavHost(
                navController,
                startDestination = JournalTopLevelRoute,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = expressiveSpringIntOffset
                    ) + fadeIn(animationSpec = expressiveSpring)
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0.92f,
                        animationSpec = expressiveSpring
                    ) + fadeOut(animationSpec = expressiveSpring)
                },
                popEnterTransition = {
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec = expressiveSpring
                    ) + fadeIn(animationSpec = expressiveSpring)
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = expressiveSpringIntOffset
                    ) + fadeOut(animationSpec = expressiveSpring)
                }
            ) {
                journalGraph(navController)
                statsGraph(navController)
                searchGraph(navController)
                saferGraph(navController)
                settingsGraph(navController)
            }

            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(600)
                ) + scaleOut(
                    targetScale = 0.5f,
                    transformOrigin = TransformOrigin(0.5f, 1f),
                    animationSpec = tween(600)
                ) + fadeOut(animationSpec = tween(600)) + shrinkVertically(
                    shrinkTowards = Alignment.Bottom,
                    animationSpec = tween(600)
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = isSearchScreenActive,
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                    ) {
                        val addIngestionEntry = remember(navBackStackEntry) {
                            try {
                                navController.getBackStackEntry(AddIngestionSearchRoute)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (addIngestionEntry != null) {
                            val searchViewModel: AddIngestionSearchViewModel =
                                hiltViewModel(addIngestionEntry)
                            val categories by searchViewModel.chipCategoriesFlow.collectAsState()

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(categories) { category ->
                                    FilterChip(
                                        selected = category.isActive,
                                        onClick = { searchViewModel.toggleCategory(category.chipName) },
                                        label = { Text(category.chipName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = category.color.copy(alpha = 0.1f),
                                            selectedContainerColor = category.color.copy(alpha = 0.35f),
                                            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = category.isActive,
                                            borderColor = category.color.copy(alpha = 0.35f),
                                            selectedBorderColor = category.color
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                        tonalElevation = 4.dp,
                        shadowElevation = 12.dp
                    ) {
                        AnimatedContent(
                            targetState = isSearchScreenActive,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(250))
                                    .togetherWith(fadeOut(animationSpec = tween(250)))
                                    .using(
                                        SizeTransform { _, _ ->
                                            spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        }
                                    )
                            },
                            label = "pillTransformation",
                            contentAlignment = Alignment.Center
                        ) { isSearch ->
                            if (isSearch) {
                                val addIngestionEntry = remember(navBackStackEntry) {
                                    try {
                                        navController.getBackStackEntry(AddIngestionSearchRoute)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                if (addIngestionEntry != null) {
                                    val searchViewModel: AddIngestionSearchViewModel =
                                        hiltViewModel(addIngestionEntry)
                                    val searchText by searchViewModel.searchTextFlow.collectAsState()
                                    val focusManager = LocalFocusManager.current

                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .padding(horizontal = 12.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .widthIn(min = 180.dp, max = 300.dp)
                                        ) {
                                            if (searchText.isEmpty()) {
                                                Text(
                                                    "Search substances...",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            }
                                            BasicTextField(
                                                value = searchText,
                                                onValueChange = searchViewModel::updateSearchText,
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                                ),
                                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    autoCorrectEnabled = false,
                                                    imeAction = ImeAction.Search,
                                                    capitalization = KeyboardCapitalization.Words,
                                                ),
                                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                                singleLine = true
                                            )
                                        }
                                        if (searchText.isNotEmpty()) {
                                            IconButton(
                                                onClick = { searchViewModel.updateSearchText("") },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    topLevelRoutes.forEach { topLevelRoute ->
                                        val selected = currentDestination?.hierarchy?.any {
                                            it.hasRoute(topLevelRoute.route::class)
                                        } == true

                                        val scale by animateFloatAsState(
                                            targetValue = if (selected) 1.2f else 1.0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.35f,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "scale"
                                        )

                                        val backgroundColor by animateColorAsState(
                                            targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                            label = "backgroundColor"
                                        )

                                        val contentColor by animateColorAsState(
                                            targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            label = "contentColor"
                                        )

                                        Row(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(backgroundColor)
                                                .clickable {
                                                    if (selected) {
                                                        val isAlreadyOnTopOfTab = topLevelRoutes.any { it.route == currentDestination.route }
                                                        if (!isAlreadyOnTopOfTab) {
                                                            navController.popBackStack()
                                                        }
                                                    } else {
                                                        navController.navigate(topLevelRoute.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                                .scale(scale),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (selected) topLevelRoute.filledIcon else topLevelRoute.outlinedIcon,
                                                contentDescription = topLevelRoute.name,
                                                tint = contentColor
                                            )
                                            AnimatedVisibility(visible = selected) {
                                                Text(
                                                    text = topLevelRoute.name,
                                                    color = contentColor,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1
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
        }
    } else {
        AcceptConditionsScreen(onTapAccept = viewModel::accept)
    }
}
