package com.example.music

import android.content.Context
import android.media.MediaPlayer

object AudioPlayerManager {
    var mediaPlayer: MediaPlayer? = null
    var currentList: List<Sheelah> = emptyList()
    var currentIndex: Int = -1

    // استمع للتغيرات في الشاشات (لتحديث شكل زر التشغيل/الإيقاف تلقائياً)
    var onPlaybackStateChanged: ((Boolean) -> Unit)? = null
    var onTrackChanged: ((Sheelah) -> Unit)? = null

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying ?: false

    val currentSheelah: Sheelah?
        get() = if (currentIndex in currentList.indices) currentList[currentIndex] else null

    fun playSheelah(context: Context, list: List<Sheelah>, index: Int) {
        currentList = list
        currentIndex = index
        val sheelah = currentSheelah ?: return

        // تنظيف المشغل القديم إذا كان يعمل
        mediaPlayer?.release()

        // تشغيل الملف الصوتي الجديد من مجلد raw
        mediaPlayer = MediaPlayer.create(context, sheelah.resourceId).apply {
            start()
        }

        onTrackChanged?.invoke(sheelah)
        onPlaybackStateChanged?.invoke(true)
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            onPlaybackStateChanged?.invoke(false)
        } else {
            player.start()
            onPlaybackStateChanged?.invoke(true)
        }
    }

    fun next(context: Context) {
        if (currentList.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentList.size
        playSheelah(context, currentList, currentIndex)
    }

    fun previous(context: Context) {
        if (currentList.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) currentList.size - 1 else currentIndex - 1
        playSheelah(context, currentList, currentIndex)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentIndex = -1
        onPlaybackStateChanged?.invoke(false)
    }
}