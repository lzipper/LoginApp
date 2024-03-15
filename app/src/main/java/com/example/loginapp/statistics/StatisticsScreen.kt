/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.loginapp.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loginapp.R
import com.example.loginapp.ui.theme.LoginAppTheme
import com.example.loginapp.util.LoadingContent
import com.example.loginapp.util.StatisticsTopAppBar

@Composable
fun StatisticsScreen(
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
    //scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    Scaffold(
        //scaffoldState = scaffoldState,
        topBar = { StatisticsTopAppBar(openDrawer) }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsState()

        StatisticsContent(
            loading = uiState.isLoading,
            empty = uiState.isEmpty,
            activeTasksPercent = uiState.activeTasksPercent,
            completedTasksPercent = uiState.completedTasksPercent,
            onRefresh = { viewModel.refresh() },
            modifier = modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsContent(
    loading: Boolean,
    empty: Boolean,
    activeTasksPercent: Float,
    completedTasksPercent: Float,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commonModifier = modifier
        .fillMaxWidth()
        .padding(all = dimensionResource(id = R.dimen.horizontal_margin))

    LoadingContent(
        loading = loading,
        empty = empty,
        onRefresh = onRefresh,
        modifier = modifier,
        emptyContent = {
            Text(
                text = stringResource(id = R.string.statistics_no_tasks),
                modifier = commonModifier
            )
        }
    ) {
        Column(
            commonModifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!loading) {
                Text(stringResource(id = R.string.statistics_active_tasks, activeTasksPercent))
                Text(
                    stringResource(
                        id = R.string.statistics_completed_tasks,
                        completedTasksPercent
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun StatisticsContentPreview() {
    LoginAppTheme {
        Surface {
            StatisticsContent(
                loading = false,
                empty = false,
                activeTasksPercent = 80f,
                completedTasksPercent = 20f,
                onRefresh = { }
            )
        }
    }
}

@Preview
@Composable
fun StatisticsContentEmptyPreview() {
    LoginAppTheme {
        Surface {
            StatisticsContent(
                loading = false,
                empty = true,
                activeTasksPercent = 0f,
                completedTasksPercent = 0f,
                onRefresh = { }
            )
        }
    }
}
