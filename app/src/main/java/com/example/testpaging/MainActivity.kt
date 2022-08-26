package com.example.testpaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.testpaging.data.Favorite
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val queryFlow = MutableStateFlow(true)

    private class SamplePagingSource(private val query: Boolean) : PagingSource<Int, String>() {

        companion object {
            const val MAX_PAGE = 10
        }

        override fun getRefreshKey(state: PagingState<Int, String>): Int? {
            return state.anchorPosition
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
            val perPage = params.loadSize
            val page = params.key ?: 0
            val next = if (page + 1 > MAX_PAGE) null else page + 1
            val prev = params.key?.let { it - 1 }

            return LoadResult.Page(
                data = (0..perPage).map { "query = $query:$page-$it-DATA" },
                prevKey = prev,
                nextKey = next
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val samplePagingFlow = queryFlow.flatMapLatest { query ->
        Pager(
            PagingConfig(
                initialLoadSize = 20,
                pageSize = 20
            )
        ) {
            SamplePagingSource(query)
        }.flow
    }.cachedIn(lifecycleScope)

    private var _data = MutableStateFlow(Favorite.lists)
    private val data: StateFlow<List<Favorite>> = _data.asStateFlow()

    // Paging使用フラグ
    private val paging: Boolean = false

    @OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            if (paging) {
                val lazyPagingItems = samplePagingFlow.collectAsLazyPagingItems()

                Scaffold(
                    floatingActionButton = {
                        Button(onClick = {
                            lifecycleScope.launch {
                                queryFlow.emit(!queryFlow.value)
                            }
                        }) {
                            Text(text = "おしてね")
                        }
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(lazyPagingItems, key = { it }) { sample ->
                            var visible by rememberSaveable { mutableStateOf(true) }

                            AnimatedVisibility(
                                modifier = Modifier.animateItemPlacement(),
                                visible = visible,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .clickable {
                                            visible = !visible
                                        }
                                ) {
                                    Text(
                                        text = sample ?: "",
                                        modifier = Modifier
                                            .height(100.dp)
                                            .fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val list by data.collectAsState()

                Scaffold { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(list) { item ->
                            AnimatedContent(
                                targetState = item,
                                modifier = Modifier.animateItemPlacement(),
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut())
                                        .using(SizeTransform(clip = false))
                                }
                            ) { favorite ->
                                Text(
                                    text = favorite.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(LightGray.copy(alpha = 0.2f))
                                        .padding(20.dp)
                                        .clickable {
                                            _data.value = data.value - favorite
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}