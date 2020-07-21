package id.kido1611.example.pdfonlineviewer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import id.kido1611.downloader.DownloadResult
import id.kido1611.downloader.Downloader
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        edUrl.setText("http://maven.apache.org/maven-1.x/maven.pdf")
        progressBar.max = 100

        btnDownload.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Downloader.downloadFile(
                    requireContext(), edUrl.text.toString()
                )
                    .collect {
                        withContext(Dispatchers.Main) {
                            when (it) {
                                is DownloadResult.Success -> {
                                    Toast
                                        .makeText(context, "Download success", Toast.LENGTH_LONG)
                                        .show()
                                    showPdf(it.output)
                                }
                                is DownloadResult.Error -> {
                                    Log.e("Download", "Error: ${it.message}")
                                }
                                is DownloadResult.Progress -> {
                                    progressBar.progress = it.progress
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun showPdf(file: File) {
        pdfView.setBackgroundColor(Color.LTGRAY)
        pdfView.fromFile(file)
            .enableSwipe(true) // allows to block changing pages using swipe
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .scrollHandle(DefaultScrollHandle(context))
            .spacing(16)
            .pageFitPolicy(FitPolicy.BOTH)
            .load()
    }
}