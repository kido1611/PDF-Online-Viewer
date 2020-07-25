package id.kido1611.downloader

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object Downloader {
    private const val BUFFER_LEN = 1 * 1024
    private const val NOTIFY_PERIOD = 150 * 1024

    private var isRunning = false

    fun downloadFile(
        context: Context,
        url: String,
        path: File = context.filesDir
    ): Flow<DownloadResult> {
        return downloadFile(context, url, null, path)
    }

    fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
        path: File = context.filesDir
    ): Flow<DownloadResult> {
        val file = File(path, fileName)
        return downloadFile(context, url, file, path)
    }

    private fun downloadFile(
        context: Context,
        url: String,
        file: File?,
        path: File = context.filesDir
    ): Flow<DownloadResult> {
        isRunning = true
        return flow {
            if (file != null && file.exists() && file.length() > 0L) {
                emit(DownloadResult.Success(file))
            } else {
                try {

                    var savedFile = file

                    val urlObject = URL(url)
                    val urlConnection = urlObject.openConnection() as HttpURLConnection
                    val totalSize = urlConnection.contentLength

                    if (savedFile == null) {
                        val contentDisposition = urlConnection.getHeaderField("Content-Disposition")

                        savedFile = File(
                            path,
                            if (contentDisposition.isNullOrBlank()) {
                                System.currentTimeMillis().toString()
                            } else {
                                contentDisposition.split("=")[1]
                            }
                        )
                    }

                    if (savedFile.length() > 0) {
                        emit(DownloadResult.Success(savedFile))
                        urlConnection.disconnect()
                    } else {
                        var downloadedSize = 0
                        var counter = 0
                        val buffer = ByteArray(BUFFER_LEN)

                        val downloadStream = BufferedInputStream(urlConnection.inputStream)

                        var bufferLength = 0
                        val outputStream = FileOutputStream(savedFile)

                        while (downloadStream.read(buffer)
                                .also { bufferLength = it } > 0 && isRunning
                        ) {
                            outputStream.write(buffer, 0, bufferLength)
                            downloadedSize += bufferLength
                            counter += bufferLength

                            if (counter > NOTIFY_PERIOD) {
                                val progress = downloadedSize * 100 / totalSize
                                emit(DownloadResult.Progress(progress))
                                counter = 0
                            }
                        }

                        urlConnection.disconnect()
                        outputStream.close()

                        if (isRunning) {
                            emit(DownloadResult.Success(savedFile))
                        }
                    }
                } catch (e: MalformedURLException) {
                    emit(DownloadResult.Error("Url not valid", e))
                } catch (e: IOException) {
                    emit(DownloadResult.Error("Failed", e))
                }
            }
        }
    }

    fun cancel() {
        isRunning = false
    }
}