package com.example.testpaging

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.*
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

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

    private val queryFlow = MutableStateFlow(true)

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


    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

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
                    items(
                        lazyPagingItems,
                        key = { it }
                    ) { sample ->
                        var visible by remember { mutableStateOf(true) }
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
                                    modifier = Modifier
                                        .height(100.dp)
                                        .fillMaxWidth(),
                                    text = sample ?: ""
                                )
                            }
                        }
                    }
                }
            }


        }
    }
}