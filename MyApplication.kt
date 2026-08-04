package com.example.music

import android.app.Application

class MyApplication : Application() {

    // نترك هذه القيمة كـ true دائماً، لكي لا تتعطل أي شاشة تبحث عنها
    @Volatile
    var youtubeDlReady: Boolean = true

    override fun onCreate() {
        super.onCreate()

        // المحرك الجديد "java-youtube-downloader" لا يحتاج لأي تهيئة هنا!
        // فهو محرك يعمل محلياً ومباشرة عند ضغط زر التحميل.
    }
}