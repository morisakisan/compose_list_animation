package com.example.testpaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testpaging.data.Sample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {

    private var _data = MutableStateFlow(Sample.lists)
    private val data: StateFlow<List<Sample>> = _data.asStateFlow()

    private val animateMethod: AnimateMethod = AnimateMethod.ANIMATED_CONTENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val list by data.collectAsState()

            when (animateMethod) {
                AnimateMethod.ANIMATED_VISIBILITY -> AnimatedVisibility(list)
                AnimateMethod.ANIMATED_CONTENT -> AnimatedContent(list)
                AnimateMethod.ANIMATE_ITEM_PLACEMENT -> AnimateItemPlacement(list)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AnimatedVisibility(list: List<Sample>) {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(list, key = { it.id }) { item ->
                    var visible by rememberSaveable { mutableStateOf(true) }

                    AnimatedVisibility(
                        modifier = Modifier.animateItemPlacement(),
                        visible = visible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        CardItem(item = item) {
                            visible = !visible
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun AnimatedContent(list: List<Sample>) {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(list) { item ->
                    AnimatedContent(
                        targetState = item,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                        }
                    ) { sample ->
                        CardItem(item = item) {
                            _data.value = data.value - sample
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AnimateItemPlacement(list: List<Sample>) {
        Scaffold { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(list, key = { it.id }) { item ->
                    CardItem(modifier = Modifier.animateItemPlacement(), item = item) {
                        _data.value = data.value - item
                    }
                }
            }
        }
    }

    @Composable
    fun CardItem(modifier: Modifier = Modifier, item: Sample, onClick: () -> Unit) {
        Card(
            modifier = modifier
                .padding(10.dp)
                .clickable(onClick = onClick)
        ) {
            Text(
                text = item.name,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            )
        }
    }

    enum class AnimateMethod {
        ANIMATED_VISIBILITY,
        ANIMATED_CONTENT,
        ANIMATE_ITEM_PLACEMENT
    }
}