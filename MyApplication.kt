package com.example.music

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.*

class MyApplication : Application() {

    @Volatile
    var youtubeDlReady: Boolean = false

    // سجل آخر خطأ حدث أثناء init (لأغراض عرض أو debug)
    @Volatile
    var lastInitError: String? = null

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // init سريع: نريد الجاهزية فور انتهاء init، لا نجعل التحديث يمنع المستخدم
        appScope.launch {
            try {
                Log.i("MyApplication", "Starting YoutubeDL and FFmpeg init...")
                YoutubeDL.getInstance().init(applicationContext)
                FFmpeg.getInstance().init(applicationContext)

                // لو وصلت إلى هنا، اعتبر init ناجحاً (جاهز للعمليات الأساسية)
                youtubeDlReady = true
                Log.i("MyApplication", "YoutubeDL & FFmpeg init succeeded — ready = true")
            } catch (e: Exception) {
                lastInitError = e.message ?: e.toString()
                youtubeDlReady = false
                Log.e("MyApplication", "Init failed: ${lastInitError}", e)
                return@launch
            }

            // ابقِ التحديث في مهمة منفصلة حتى لو استغرق وقتاً؛ لا تمنع الجاهزية.
            launch {
                try {
                    Log.i("MyApplication", "Starting YoutubeDL update in background...")
                    // نفذ التحديث مع timeout معقول (مثلاً 30s). إن تجاوز الوقت نحاول الإلغاء.
                    withTimeout(30_000L) {
                        YoutubeDL.getInstance().updateYoutubeDL(applicationContext, YoutubeDL.UpdateChannel.STABLE)
                    }
                    Log.i("MyApplication", "YoutubeDL update completed")
                } catch (e: TimeoutCancellationException) {
                    Log.w("MyApplication", "YoutubeDL update timed out (continuing with existing binary).", e)
                } catch (e: Exception) {
                    Log.w("MyApplication", "YoutubeDL update failed (continuing with existing binary): ${e.message}", e)
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        appScope.cancel()
    }
}