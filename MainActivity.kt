package com.example.music

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val btnOpenMenu: View = findViewById(R.id.btnOpenMenu)

        // داخل MainActivity.onCreate(...)
        android.util.Log.i("AppCheck", "Application class = ${application.javaClass.name}")

        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val artistList = listOf(
            Artist("عبدالله آل فروان", R.drawable.icon_artist_alfarwan),
            Artist("عبدالله آل مخلص", R.drawable.icon_artist_abdullah),
            Artist("غريب آل مخلص", R.drawable.icon_artist_ghreeb)
        )
        val rvArtists: RecyclerView = findViewById(R.id.rvArtists)
        rvArtists.adapter = ArtistAdapter(artistList) { artist ->
            val intent = Intent(this, SheelahListActivity::class.java)
            intent.putExtra("ARTIST_NAME", artist.name)
            startActivity(intent)
        }

        val btnMenuFav: CardView = findViewById(R.id.btnMenuFav)
        btnMenuFav.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, SheelahListActivity::class.java)
            intent.putExtra("IS_FAVORITES_MODE", true)
            startActivity(intent)
        }
        val btnMenuYtDownload: CardView = findViewById(R.id.btnMenuYtDownload)
        btnMenuYtDownload.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, YouTubeDownloaderActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupMiniPlayer() {
        val miniPlayerLayout: View = findViewById(R.id.miniPlayerLayout)
        val tvMiniTitle: TextView = miniPlayerLayout.findViewById(R.id.tvMiniTitle)
        val tvMiniArtist: TextView = miniPlayerLayout.findViewById(R.id.tvMiniArtist)
        val ivArtistImage: ImageView = miniPlayerLayout.findViewById(R.id.ivArtistImage)
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