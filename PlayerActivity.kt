package com.example.music

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// 👇 القالب المطور الشامل للبيانات
data class Sheelah(
    val title: String,
    val singer: String,
    val resourceId: Int,
    val imageResId: Int,
    val duration: String
)

class PlayerActivity : AppCompatActivity() {

    // 👈 تم حذف mediaPlayer المحلي لأنه أصبح في المحرك المركزي AudioPlayerManager
    private var currentMode = 0
    private val sheelahList = MusicLibrary.allSheelahs

    private lateinit var tvTitle: TextView
    private lateinit var tvSinger: TextView
    private lateinit var ivBackground: ImageView

    private lateinit var btnFavorite: TextView
    private lateinit var btnFavTop: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnRepeat: ImageButton

    private lateinit var seekBar: SeekBar
    private lateinit var tvElapsedTime: TextView
    private lateinit var tvTotalDuration: TextView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        tvTitle = findViewById(R.id.tvTitle)
        tvSinger = findViewById(R.id.tvSinger)
        ivBackground = findViewById(R.id.ivBackground)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnRepeat = findViewById(R.id.btnRepeat)
        btnFavTop = findViewById(R.id.btnFavTop)

        val btnNext: ImageButton = findViewById(R.id.btnNext)
        val btnPrevious: ImageButton = findViewById(R.id.btnPrevious)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        seekBar = findViewById(R.id.seekBar)
        tvElapsedTime = findViewById(R.id.tvElapsedTime)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)

        // 👈 3. ربط استماع المحرك المركزي لكي تتحدث الشاشة تلقائياً
        AudioPlayerManager.onTrackChanged = { updatePlayerUI() }
        AudioPlayerManager.onPlaybackStateChanged = { isPlaying ->
            if (isPlaying) {
                btnPlayPause.setImageResource(R.drawable.ic_pause_modern)
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play_modern)
            }
        }

        // 👈 1. استقبال الشيلة المطلوبة وتشغيلها عبر المحرك المركزي
        val requestedIndex = intent.getIntExtra("SHEELAH_INDEX", 0)

        // إذا لم تكن الشيلة المطلوبة هي نفس التي تعمل حالياً، قم بتشغيلها
        if (AudioPlayerManager.currentIndex != requestedIndex || !AudioPlayerManager.isPlaying) {
            AudioPlayerManager.playSheelah(this, sheelahList, requestedIndex)
        }

        // 👈 2. استدعاء تحديث الشاشة لتطابق الشيلة الحالية
        updatePlayerUI()
        updateSeekBar()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    AudioPlayerManager.mediaPlayer?.seekTo(progress)
                    tvElapsedTime.text = createTimeLabel(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnRepeat.setOnClickListener {
            currentMode = (currentMode + 1) % 3
            when (currentMode) {
                0 -> {
                    btnRepeat.setImageResource(R.drawable.ic_mode_sequential)
                    Toast.makeText(this, "➡️  التشغيل التسلسلي", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    btnRepeat.setImageResource(R.drawable.ic_mode_shuffle)
                    Toast.makeText(this, "🔀 التشغيل العشوائي", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    btnRepeat.setImageResource(R.drawable.ic_mode_repeat_one)
                    Toast.makeText(this, "🔂 تكرار الشيلة الحالية", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 👈 4. أزرار التحكم أصبحت تتصل بالمحرك المركزي مباشرة
        btnPlayPause.setOnClickListener {
            AudioPlayerManager.togglePlayPause()
        }

        btnNext.setOnClickListener {
            playNextSheelah(isAuto = false)
        }

        btnPrevious.setOnClickListener {
            playPreviousSheelah()
        }

        btnFavorite.setOnClickListener { toggleFavorite(AudioPlayerManager.currentIndex) }
        btnFavTop.setOnClickListener { toggleFavorite(AudioPlayerManager.currentIndex) }
    }

    // 👈 دالة جديدة موحدة لتحديث كل الشاشة (الاسم، الصورة، المدة، المفضلة)
    private fun updatePlayerUI() {
        val currentSheelah = AudioPlayerManager.currentSheelah ?: return
        val index = AudioPlayerManager.currentIndex

        tvTitle.text = currentSheelah.title
        tvSinger.text = currentSheelah.singer
        ivBackground.setImageResource(currentSheelah.imageResId)

        if (AudioPlayerManager.isPlaying) {
            btnPlayPause.setImageResource(R.drawable.ic_pause_modern)
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play_modern)
        }

        val totalTime = AudioPlayerManager.mediaPlayer?.duration ?: 0
        seekBar.max = totalTime
        tvTotalDuration.text = createTimeLabel(totalTime)

        // ربط التكرار التلقائي عند انتهاء الشيلة
        AudioPlayerManager.mediaPlayer?.setOnCompletionListener {
            playNextSheelah(isAuto = true)
        }

        updateFavoriteButton(index)
    }

    private fun playNextSheelah(isAuto: Boolean) {
        when {
            isAuto && currentMode == 2 -> {
                AudioPlayerManager.mediaPlayer?.seekTo(0)
                AudioPlayerManager.mediaPlayer?.start()
                return
            }
            currentMode == 1 -> {
                val randomIndex = sheelahList.indices.random()
                AudioPlayerManager.playSheelah(this, sheelahList, randomIndex)
            }
            else -> {
                AudioPlayerManager.next(this)
            }
        }
        updatePlayerUI()
    }

    private fun playPreviousSheelah() {
        AudioPlayerManager.previous(this)
        updatePlayerUI()
    }

    private fun updateSeekBar() {
        runnable = Runnable {
            AudioPlayerManager.mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    val currentPos = player.currentPosition
                    seekBar.progress = currentPos
                    tvElapsedTime.text = createTimeLabel(currentPos)
                }
            }
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun createTimeLabel(timeInMs: Int): String {
        val totalSeconds = timeInMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun toggleFavorite(index: Int) {
        if (index == -1) return
        val prefs = getSharedPreferences("MusicAppPrefs", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorites", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        val indexStr = index.toString()

        if (favorites.contains(indexStr)) {
            favorites.remove(indexStr)
        } else {
            favorites.add(indexStr)
        }

        prefs.edit().putStringSet("favorites", favorites).apply()
        updateFavoriteButton(index)
    }

    private fun updateFavoriteButton(index: Int) {
        if (index == -1) return
        val prefs = getSharedPreferences("MusicAppPrefs", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorites", emptySet()) ?: emptySet()

        if (favorites.contains(index.toString())) {
            btnFavorite.text = "❤️"
            btnFavTop.setImageResource(R.drawable.ic_fav_filled)
            btnFavTop.setTint(Color.parseColor("#FF1E66"))
        } else {
            btnFavorite.text = "🤍"
            btnFavTop.setImageResource(R.drawable.ic_fav_border)
            btnFavTop.setTint(Color.WHITE)
        }
    }

    private fun ImageButton.setTint(color: Int) {
        this.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        // 👈 لاحظ: لم نقم بحذف أو إيقاف mediaPlayer هنا! لذلك سيستمر الصوت بالعمل في الخلفية! 🚀
    }
}