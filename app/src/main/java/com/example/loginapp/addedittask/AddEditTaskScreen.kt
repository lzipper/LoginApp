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

package com.example.loginapp.addedittask

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.ContentAlpha
import com.example.loginapp.R
import com.example.loginapp.util.AddEditTaskTopAppBar


@Composable
fun AddEditTaskScreen(
    @StringRes topBarTitle: Int,
    onTaskUpdate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        //scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AddEditTaskTopAppBar(topBarTitle, onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::saveTask) {
                Icon(Icons.Filled.Done, stringResource(id = R.string.cd_save_task))
            }
        }
    ) { paddingValues ->
        val uiState by viewModel.uiState.collectAsState()

        AddEditTaskContent(
            loading = uiState.isLoading,
            title = uiState.title,
            description = uiState.description,
            onTitleChanged = viewModel::updateTitle,
            onDescriptionChanged = viewModel::updateDescription,
            modifier = Modifier.padding(paddingValues)
        )

        // Check if the task is saved and call onTaskUpdate event
        LaunchedEffect(uiState.isTaskSaved) {
            if (uiState.isTaskSaved) {
                onTaskUpdate()
            }
        }

        // Check for user messages to display on the screen
        uiState.userMessage?.let { userMessage ->
            val snackbarText = stringResource(userMessage)
            LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.snackbarMessageShown()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTaskContent(
    loading: Boolean,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (loading) {
        PullToRefreshContainer(
            state = rememberPullToRefreshState(),
            modifier = modifier,
        )

        //SwipeRefresh(
            // Show the loading spinner—`loading` is `true` in this code path
        //    state = rememberSwipeRefreshState(true),
        //    onRefresh = { /* DO NOTHING */ },
        //    content = { },
        //)
    } else {
        Column(
            modifier
                .fillMaxWidth()
                .padding(all = dimensionResource(id = R.dimen.horizontal_margin))
                .verticalScroll(rememberScrollState())
        ) {
            val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.secondary.copy(alpha = ContentAlpha.high)
            )
            OutlinedTextField(
                value = title,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.title_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                colors = textFieldColors
            )
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = { Text(stringResource(id = R.string.description_hint)) },
                modifier = Modifier
                    .height(350.dp)
                    .fillMaxWidth(),
                colors = textFieldColors
            )
        }
    }
}
