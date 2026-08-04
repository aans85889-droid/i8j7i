package com.example.music

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SheelahListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheelah_list)

        val rvSheelahs: RecyclerView = findViewById(R.id.rvSheelahs)
        val tvTitle: TextView = findViewById(R.id.tvTitle)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val btnOpenMenu: View = findViewById(R.id.btnOpenMenu)

        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val btnMenuFav: CardView = findViewById(R.id.btnMenuFav)
        btnMenuFav.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, SheelahListActivity::class.java)
            intent.putExtra("IS_FAVORITES_MODE", true)
            startActivity(intent)
            finish()
        }
        val btnMenuYtDownload: CardView = findViewById(R.id.btnMenuYtDownload)
        btnMenuYtDownload.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, YouTubeDownloaderActivity::class.java)
            startActivity(intent)
        }

        val isFavoritesMode = intent.getBooleanExtra("IS_FAVORITES_MODE", false)
        val artistName = intent.getStringExtra("ARTIST_NAME")

        val allSheelahs = MusicLibrary.allSheelahs
        val prefs = getSharedPreferences("MusicAppPrefs", Context.MODE_PRIVATE)
        val favSet = prefs.getStringSet("favorites", emptySet()) ?: emptySet()

        val displayList = mutableListOf<Sheelah>()
        val originalIndices = mutableListOf<Int>()

        for (i in allSheelahs.indices) {
            val sheelah = allSheelahs[i]
            val matchesFavorite = !isFavoritesMode || favSet.contains(i.toString())
            val matchesArtist = artistName == null || sheelah.singer == artistName

            if (matchesFavorite && matchesArtist) {
                displayList.add(sheelah)
                originalIndices.add(i)
            }
        }

        if (isFavoritesMode) {
            tvTitle.text = "❤️ المفضلة"
            if (displayList.isEmpty()) {
                Toast.makeText(this, "لا توجد شيلات في المفضلة بعد!", Toast.LENGTH_LONG).show()
            }
        } else if (artistName != null) {
            tvTitle.text = "artistName"
            if (displayList.isEmpty()) {
                Toast.makeText(this, "لا توجد شيلات لهذا الفنان بعد!", Toast.LENGTH_LONG).show()
            }
        }

        val adapter = SheelahAdapter(displayList) { position ->
            val originalIndex = originalIndices[position]
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("SHEELAH_INDEX", originalIndex)
            startActivity(intent)
        }
        rvSheelahs.adapter = adapter
    }

    private fun setupMiniPlayer() {
        val miniPlayerLayout: View = findViewById(R.id.miniPlayerLayout)
        val tvMiniTitle: TextView = miniPlayerLayout.findViewById(R.id.tvMiniTitle)
        val tvMiniArtist: TextView = miniPlayerLayout.findViewById(R.id.tvMiniArtist)
        val ivArtistImage: ImageView = miniPlayerLayout.findViewById(R.id.ivArtistImage) // 👈 ربط الصورة
        val btnMiniPlayPause: ImageView = miniPlayerLayout.findViewById(R.id.btnMiniPlayPause)
        val btnMiniNext: ImageView = miniPlayerLayout.findViewById(R.id.btnMiniNext)
        val btnMiniPrev: ImageView = miniPlayerLayout.findViewById(R.id.btnMiniPrev)
        val btnMiniClose: ImageView = miniPlayerLayout.findViewById(R.id.btnMiniClose)
        val miniProgressBar: ProgressBar = miniPlayerLayout.findViewById(R.id.miniProgressBar)

        fun updateMiniUI() {
            val current = AudioPlayerManager.currentSheelah
            if (current != null) {
                miniPlayerLayout.visibility = View.VISIBLE
                tvMiniTitle.text = current.title
                tvMiniArtist.text = current.singer
                ivArtistImage.setImageResource(current.imageResId)

                if (AudioPlayerManager.isPlaying) {
                    btnMiniPlayPause.setImageResource(R.drawable.ic_pause_modern)
                } else {
                    btnMiniPlayPause.setImageResource(R.drawable.ic_play_modern)
                }

                AudioPlayerManager.mediaPlayer?.let { mp ->
                    miniProgressBar.max = mp.duration
                    miniProgressBar.progress = mp.currentPosition
                }
            } else {
                miniPlayerLayout.visibility = View.GONE
            }
        }

        updateMiniUI()

        btnMiniPlayPause.setOnClickListener {
            AudioPlayerManager.togglePlayPause()
            updateMiniUI()
        }

        btnMiniNext.setOnClickListener {
            AudioPlayerManager.next(this)
            updateMiniUI()
        }

        btnMiniPrev.setOnClickListener {
            AudioPlayerManager.previous(this)
            updateMiniUI()
        }

        btnMiniClose.setOnClickListener {
            AudioPlayerManager.stop()
            miniPlayerLayout.visibility = View.GONE
        }

        miniPlayerLayout.setOnClickListener {
            if (AudioPlayerManager.currentIndex != -1) {
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putExtra("SHEELAH_INDEX", AudioPlayerManager.currentIndex)
                startActivity(intent)
            }
        }

        AudioPlayerManager.onPlaybackStateChanged = { isPlaying -> updateMiniUI() }
        AudioPlayerManager.onTrackChanged = { sheelah -> updateMiniUI() }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (AudioPlayerManager.isPlaying) {
                    AudioPlayerManager.mediaPlayer?.let { mp ->
                        miniProgressBar.progress = mp.currentPosition
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupMiniPlayer()
    }
}