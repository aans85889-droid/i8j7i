package com.example.music

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YouTubeDownloaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_downloader)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val etUrlInput: EditText = findViewById(R.id.etUrlInput)
        val btnFetch: CardView = findViewById(R.id.btnFetch)
        val loadingLayout: LinearLayout = findViewById(R.id.loadingLayout)
        val rvDownloadOptions: RecyclerView = findViewById(R.id.rvDownloadOptions)

        btnBack.setOnClickListener { finish() }

        btnFetch.setOnClickListener {
            val url = etUrlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "الرجاء إدخال الرابط أولاً!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingLayout.visibility = View.VISIBLE
            rvDownloadOptions.visibility = View.GONE
            btnFetch.isEnabled = false

            // استخراج Video ID بدقة أكبر
            val videoId = if (url.contains("v=")) {
                url.substringAfter("v=").substringBefore("&")
            } else if (url.contains("youtu.be/")) {
                url.substringAfter("youtu.be/").substringBefore("?")
            } else {
                url
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val downloader = YoutubeDownloader()
                    val request = RequestVideoInfo(videoId)
                    val response = downloader.getVideoInfo(request)
                    val video = response.data()

                    withContext(Dispatchers.Main) {
                        loadingLayout.visibility = View.GONE
                        btnFetch.isEnabled = true

                        if (video != null) {
                            val audioFormats = video.audioFormats()
                            val title = video.details().title()

                            Toast.makeText(
                                this@YouTubeDownloaderActivity,
                                "🎉 تم الجلب: $title\nعدد الصيغ: ${audioFormats.size}",
                                Toast.LENGTH_LONG
                            ).show()

                            rvDownloadOptions.visibility = View.VISIBLE
                        } else {
                            // 👈 هنا السحر الهندسي: صيد سبب الخطأ الفعلي من يوتيوب!
                            val errorMsg = response.error()?.message ?: "حظر مجهول من يوتيوب"
                            Log.e("YoutubeDownloader", "Error Details: $errorMsg", response.error())
                            Toast.makeText(this@YouTubeDownloaderActivity, "❌ سبب الخطأ: $errorMsg", Toast.LENGTH_LONG).show()
                        }
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loadingLayout.visibility = View.GONE
                        btnFetch.isEnabled = true
                        Toast.makeText(this@YouTubeDownloaderActivity, "❌ خطأ عام: ${e.message}", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}