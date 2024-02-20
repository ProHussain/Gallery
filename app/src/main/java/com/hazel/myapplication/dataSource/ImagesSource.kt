package com.hazel.myapplication.dataSource

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.io.File

class ImagesSource(private val context: Context) : PagingSource<Int, File>() {
    override fun getRefreshKey(state: PagingState<Int, File>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        val prevKey = anchorPage.prevKey
        val nextKey = anchorPage.nextKey
        return when {
            prevKey != null -> prevKey + 1
            nextKey != null -> nextKey - 1
            else -> null
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, File> {
        val start = params.key ?: 0
        val end = 100
        return try {
            val list = mutableListOf<File>()

            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )
            val order = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                order
            )
            cursor?.let {
                Log.d("ImagesSource", "load: $start")
                Log.d("ImagesSource", "load: $end")
                if (it.moveToPosition(start)) {
                    do {
                        val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        list.add(File(path))
                    } while (it.moveToNext() && it.position < start + end)
                }
                it.close()
            }

            LoadResult.Page(
                data = list,
                prevKey = if (start > 0) start-1 else null,
                nextKey = if (list.isEmpty()) null else start + end
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}