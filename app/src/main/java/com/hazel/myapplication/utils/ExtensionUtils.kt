package com.hazel.myapplication.utils

import java.io.File
import java.util.Locale

object ExtensionUtils {
    fun File.isImage(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "jpg" || ex == "jpeg" || ex == "png" || ex == "webp" || ex == "heic" || ex == "heif" || ex == "gif" || ex == "bmp"
    }

    fun File.isVideo(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "mp4" || ex == "mkv" || ex == "avi" || ex == "webm" || ex == "3gp" || ex == "flv" || ex == "mov" || ex == "wmv"
    }

    fun File.isPdf(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "pdf"
    }

    fun File.isDoc(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "doc" || ex == "docx"
    }

    fun File.isXls(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "xls" || ex == "xlsx"
    }

    fun File.isPpt(): Boolean {
        val ex = this.extension.lowercase(Locale.ROOT)
        return ex == "ppt" || ex == "pptx"
    }
}