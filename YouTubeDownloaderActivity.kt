package com.example.music

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YouTubeDownloaderActivity : AppCompatActivity() {

    private val POLL_READY_MS = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_downloader)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val etUrlInput: EditText = findViewById(R.id.etUrlInput)
        val btnFetch: CardView = findViewById(R.id.btnFetch)
        val loadingLayout: LinearLayout = findViewById(R.id.loadingLayout)
        val rvDownloadOptions: RecyclerView = findViewById(R.id.rvDownloadOptions)

        btnBack.setOnClickListener { finish() }

        // تأكد أن المحرك جاهز قبل السماح بالبحث/الجلب
        val app = application as MyApplication
        if (!app.youtubeDlReady) {
            btnFetch.isEnabled = false
            Toast.makeText(this, "⚙️ يتم تهيئة محرك التحميل... الرجاء الانتظار", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(Dispatchers.IO) {
                while (!app.youtubeDlReady) {
                    delay(POLL_READY_MS)
                }
                withContext(Dispatchers.Main) {
                    btnFetch.isEnabled = true
                    Toast.makeText(this@YouTubeDownloaderActivity, "✅ محرك التحميل جاهز", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            btnFetch.isEnabled = true
        }

        // زر جلب الروابط والبيانات
        btnFetch.setOnClickListener {
            val url = etUrlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "الرجاء لصق رابط يوتيوب أولاً!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loadingLayout.visibility = View.VISIBLE
            rvDownloadOptions.visibility = View.GONE
            btnFetch.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val request = YoutubeDLRequest(url)
                    request.addOption("--dump-json")
                    // صحّحنا اسم الخيار: singular (بدون s)
                    request.addOption("--no-check-certificate")

                    val streamInfo = YoutubeDL.getInstance().getInfo(request)

                    withContext(Dispatchers.Main) {
                        loadingLayout.visibility = View.GONE
                        btnFetch.isEnabled = true

                        val videoTitle = streamInfo.title ?: "بدون عنوان"
                        val audioFormats = streamInfo.formats?.filter { format ->
                            format.vcodec == "none" && format.acodec != "none"
                        } ?: emptyList()

                        Toast.makeText(
                            this@YouTubeDownloaderActivity,
                            "🎉 نجح الجلب: $videoTitle\nعدد الصيغ الصوتية: ${audioFormats.size}",
                            Toast.LENGTH_LONG
                        ).show()

                        rvDownloadOptions.visibility = View.VISIBLE
                        // TODO: عرض الصيغ في RV باستخدام Adapter مناسب
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        loadingLayout.visibility = View.GONE
                        btnFetch.isEnabled = true

                        val realError = e.message ?: e.localizedMessage ?: "خطأ غير معروف"
                        Toast.makeText(
                            this@YouTubeDownloaderActivity,
                            "❌ سبب الفشل:\n$realError",
                            Toast.LENGTH_LONG
                        ).show()

                        // طباعة التفاصيل في Logcat
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}