package com.example.loginapp.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.core.app.ActivityScenario.launch
import androidx.wear.compose.material.ThresholdConfig
import com.example.loginapp.R
import com.example.loginapp.data.model.login.Profile
import com.example.loginapp.tasks.TasksFilterType
import com.example.loginapp.tasks.TasksViewModel
import com.example.loginapp.util.TasksTopAppBar
import kotlinx.coroutines.delay
import java.text.Normalizer.normalize
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TinderLikeScreen(
    profiles: List<Profile>,
    openDrawer: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    ) {

    var currentProfileIndex by remember { mutableIntStateOf(0) }

    Scaffold (
        topBar = {
            TasksTopAppBar(
                openDrawer = openDrawer,
                onFilterAllTasks = { },//viewModel.setFiltering(TasksFilterType.ALL_TASKS) },
                onFilterActiveTasks = { },//viewModel.setFiltering(TasksFilterType.ACTIVE_TASKS) },
                onFilterCompletedTasks = { },//viewModel.setFiltering(TasksFilterType.COMPLETED_TASKS) },
                onClearCompletedTasks = { },//viewModel.clearCompletedTasks() },
                onRefresh = { },//viewModel.refresh() }
            )
        },
        bottomBar = {

        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .background(Color.Yellow)
        ) {
            if (profiles.isNotEmpty()) {
                profiles.forEach() { profile ->
                    SwipeCard(
                        onSwipeLeft = {
                            viewModel.onSwipeLeft(profile)
                        },
                        onSwipeRight = {
                            viewModel.onSwipeRight(profile)
                        }
                    ) {
                        ImageWithTextAndButtons(
                            profile = profiles[currentProfileIndex],
                            buttonTexts = listOf("1", "2", "3"),
                            onButtonClicked = {}
                        )
                    }
                }
            }
            //profiles.forEach() { profile -> }
        }
    }
}

@Composable
fun ImageWithTextAndButtons(
    profile: Profile,
    buttonTexts: List<String>,
    onButtonClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.background(Color.Blue),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_menu),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 8.dp)
        )
        Text(text = profile.profileName)
        Text(text = profile.profileDescription ?: "", modifier = Modifier.padding(bottom = 8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            buttonTexts.forEach { buttonText ->
                Button(onClick = { onButtonClicked(buttonText) }) {
                    Text(text = buttonText)
                }
            }
        }
    }
}

@Composable
fun SwipeCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeThreshold: Float = 400f,
    sensitivityFactor: Float = 3f,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableFloatStateOf(0f) }
    var dismissRight by remember { mutableStateOf(false) }
    var dismissLeft by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    LaunchedEffect(dismissRight) {
        if (dismissRight) {
            delay(300)
            onSwipeRight.invoke()
            dismissRight = false
        }
    }

    LaunchedEffect(dismissLeft) {
        if (dismissLeft) {
            delay(300)
            onSwipeLeft.invoke()
            dismissLeft = false
        }
    }

    Box(modifier = Modifier
        .offset { IntOffset(offset.roundToInt(), 0) }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(onDragEnd = {
                offset = 0f
            }) { change, dragAmount ->

                offset += (dragAmount / density) * sensitivityFactor
                when {
                    offset > swipeThreshold -> {
                        dismissRight = true
                        onSwipeRight.invoke()
                    }

                    offset < -swipeThreshold -> {
                        dismissLeft = true
                        onSwipeLeft.invoke()
                    }
                }
                if (change.positionChange() != Offset.Zero) change.consume()
            }
        }
        .graphicsLayer(
            alpha = 10f - animateFloatAsState(if (dismissRight) 1f else 0f, label = "").value,
            rotationZ = animateFloatAsState(offset / 50, label = "").value
        )
        .fillMaxHeight()
    ) {
        content()
    }
}

@Composable
fun SwipeableImage(
    image: Int, offset: Float,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = if (offset != 0f) (offset / 1000f) * 45f else 0f,
            animationSpec = tween(durationMillis = 500)
        )
        scale.animateTo(
            targetValue = if (offset != 0f) 0.95f else 1f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .offset(y = offset.dp)
            .background(Color.White)
            .padding(8.dp)
            .rotate(rotation.value)
            .scale(scale.value)
    ) {
        Card(
            shape = CircleShape,
            //elevation = 8.dp
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier.size(300.dp)
            )
        }
    }
}