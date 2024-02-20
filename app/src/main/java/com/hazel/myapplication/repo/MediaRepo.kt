package com.hazel.myapplication.repo

import android.content.Context
import com.hazel.myapplication.dataSource.DocsSource
import com.hazel.myapplication.dataSource.ImagesSource
import com.hazel.myapplication.dataSource.PdfSource
import com.hazel.myapplication.dataSource.PptSource
import com.hazel.myapplication.dataSource.VideoSource
import com.hazel.myapplication.dataSource.XlsSource

class MediaRepo {
    fun imageSource(context: Context) = ImagesSource(context)
    fun videoSource(context: Context) = VideoSource(context)
    fun pdfSource(context: Context) = PdfSource(context)
    fun docsSource(context: Context) = DocsSource(context)
    fun xlsSource(context: Context) = XlsSource(context)
    fun pptSource(context: Context) = PptSource(context)
}