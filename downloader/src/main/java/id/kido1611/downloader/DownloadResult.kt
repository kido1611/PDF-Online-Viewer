package id.kido1611.downloader

import java.io.File
import java.lang.Exception

sealed class DownloadResult {
    data class Success(val output: File) : DownloadResult()

    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    data class Progress(val progress: Int): DownloadResult()
}