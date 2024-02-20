package com.hazel.myapplication.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.hazel.myapplication.repo.MediaRepo
import com.hazel.myapplication.utils.ExtensionUtils.isImage
import com.hazel.myapplication.utils.ExtensionUtils.isPdf
import com.hazel.myapplication.utils.ExtensionUtils.isVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    var images = listOf<File>()
    var videos = listOf<File>()

    private val _savedFiles: MutableStateFlow<List<File>> = MutableStateFlow(listOf())
    val savedFiles = _savedFiles.asStateFlow()

    private val _deleteTask = MutableStateFlow(false)
    val deleteTask = _deleteTask.asStateFlow()

    private val _saveImageTask = MutableStateFlow(false)
    val saveImageTask = _saveImageTask.asStateFlow()

    private val _saveVideoTask = MutableStateFlow(false)
    val saveVideoTask = _saveVideoTask.asStateFlow()

    private val _savePdfTask = MutableStateFlow(false)
    val savePdfTask = _savePdfTask.asStateFlow()

    private val _saveDocTask = MutableStateFlow(false)
    val saveDocTask = _saveDocTask.asStateFlow()

    private val _saveXlsTask = MutableStateFlow(false)
    val saveXlsTask = _saveXlsTask.asStateFlow()

    private val _savePptTask = MutableStateFlow(false)
    val savePptTask = _savePptTask.asStateFlow()

    private val _progressMessage = MutableStateFlow("")
    val progressMessage = _progressMessage.asStateFlow()

    fun readSavedFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myFolder = File(
                    getApplication<Application>().filesDir,
                    "MyApp"
                )
                if (!myFolder.exists())
                    myFolder.mkdir()

                val files = myFolder.listFiles()
                if (files != null) {
                    if (files.size == savedFiles.value.size)
                        return@withContext
                    else
                        _savedFiles.value = files.toList()
                }
            }
        }
    }

    fun saveImage(position: Int) {
        try {
            saveFile(images[position])
        } catch (e: Exception) {
            Log.e("TAG", "saveImage: ${e.message}")
        }
    }

    fun saveVideo(position: Int) {
        try {
            saveFile(videos[position])
        } catch (e: Exception) {
            Log.e("TAG", "saveImage: ${e.message}")
        }
    }

    private fun saveFile(myFile: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myFolder = File(
                    getApplication<Application>().filesDir,
                    "MyApp"
                )
                if (!myFolder.exists())
                    myFolder.mkdir()

                val randomName = System.currentTimeMillis().toString()
                val newFile = File(myFolder, randomName + myFile.name)
                myFile.copyTo(newFile, true)
                Toast.makeText(
                    getApplication(),
                    "File Saved at ${newFile.absolutePath}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun saveSelectedFiles(selectedItems: ArrayList<File>, type: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myFolder = File(
                    getApplication<Application>().filesDir,
                    "MyApp"
                )
                if (!myFolder.exists())
                    myFolder.mkdir()

                for (myFile in selectedItems) {
                    try {
                        _progressMessage.value =
                            "Save ${selectedItems.indexOf(myFile) + 1} of ${selectedItems.size}"
                        val randomName = System.currentTimeMillis().toString()
                        val newFile = File(myFolder, randomName + myFile.name)
                        myFile.copyTo(newFile, true)
                    } catch (e: Exception) {
                        Log.e("TAG", "saveImages: ${e.message}")
                    }

                    if (myFile == selectedItems.last()) {
                        when (type) {
                            "image" -> _saveImageTask.value = true
                            "videos" -> _saveVideoTask.value = true
                            "pdf" -> _savePdfTask.value = true
                            "doc" -> _saveDocTask.value = true
                            "xls" -> _saveXlsTask.value = true
                        }
                    }
                }
            }
        }
    }

    fun deleteFiles(selectedItems: ArrayList<File>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for (myFile in selectedItems) {
                    try {
                        _progressMessage.value =
                            "Save ${selectedItems.indexOf(myFile) + 1} of ${selectedItems.size}"
                        myFile.delete()
                    } catch (e: Exception) {
                        Log.e("TAG", "saveImages: ${e.message}")
                    }

                    if (myFile == selectedItems.last())
                        _deleteTask.value = true
                }
            }
        }
    }

    private val mediaRepo = MediaRepo()
    val imagesList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false,
            prefetchDistance = 100,
            initialLoadSize = 100
        ),
        pagingSourceFactory = { mediaRepo.imageSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    val videosList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { mediaRepo.videoSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    val pdfList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { mediaRepo.pdfSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    val docList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {mediaRepo.docsSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    val xlsList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {mediaRepo.xlsSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    val pptList: Flow<PagingData<File>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {mediaRepo.pptSource(getApplication()) }
    ).flow
        .cachedIn(viewModelScope)

    fun clearProgress() {
        _deleteTask.value = false
        _saveImageTask.value = false
        _saveVideoTask.value = false
        _savePdfTask.value = false
        _saveDocTask.value = false
        _saveXlsTask.value = false
        _progressMessage.value = ""
    }
}