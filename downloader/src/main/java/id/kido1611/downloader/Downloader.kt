package id.kido1611.downloader

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.android.Android
import io.ktor.client.request.url
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

object Downloader {
    private val httpClient = HttpClient(Android)

    fun downloadFile(context: Context, url: String) : Flow<DownloadResult>{
        return downloadFile(context, url, null)
    }

    fun downloadFile(context: Context, url: String, fileName: String) : Flow<DownloadResult>{
        val file = File(context.filesDir, fileName)
        return downloadFile(context, url, file)
    }

    fun downloadFile(context: Context, url: String, file: File?) : Flow<DownloadResult>{
        return flow {
            if(file != null && file.exists() && file.length() > 0L){
                emit(DownloadResult.Success(file))
            }
            else{
                try {
                    val response = httpClient.call {
                        url(url)
                        method = HttpMethod.Get
                    }.response

                    val data = ByteArray(response.contentLength()!!.toInt())
                    var offset = 0

                    val contentDisposition = ContentDisposition.parse(response.headers[HttpHeaders.ContentDisposition].toString())

                    var savedFile = file
                    if(savedFile == null){
                        savedFile = File(context.filesDir, contentDisposition.parameter(ContentDisposition.Parameters.FileName) ?: System.currentTimeMillis().toString())
                    }

                    if(savedFile.exists() && savedFile.length() > 0){
                        emit(DownloadResult.Success(savedFile))
                        httpClient.close()
                    }
                    else{
                        do{
                            val currentRead = response.content.readAvailable(data, offset, data.size)
                            offset += currentRead
                            val progress = (offset * 100f / data.size).roundToInt()
                            emit(DownloadResult.Progress(progress))
                        } while (currentRead > 0)
                        response.close()

                        if(response.status.isSuccess()){
                            withContext(Dispatchers.IO){
                                savedFile.outputStream().write(data)
                            }
                            emit(DownloadResult.Success(savedFile))
                        }
                        else{
                            emit(DownloadResult.Error("File not downloaded"))
                        }
                    }
                }
                catch (e: TimeoutCancellationException){
                    emit(DownloadResult.Error("Connection timeout", e))
                }
                catch (t: Throwable){
                    emit(DownloadResult.Error("Failed to connect, ${t.message}"))
                }
            }
        }
    }
}