package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : Activity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val streamUrl = intent.getStringExtra("stream_url")
        val title = intent.getStringExtra("stream_title") ?: "Allinonetv Player"
        if (streamUrl.isNullOrBlank() || !(streamUrl.startsWith("https://") || streamUrl.startsWith("http://"))) {
            showError("No valid stream was provided. Please return and try again.")
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }
        val heading = TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 8, 20, 8)
        }
        root.addView(heading, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 56
        ))
        val view = PlayerView(this).apply {
            useController = true
            keepScreenOn = true
        }
        root.addView(view, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)

        player = ExoPlayer.Builder(this).build().also { exo ->
            view.player = exo
            exo.setMediaItem(MediaItem.fromUri(streamUrl))
            exo.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    showError("Playback failed. The provider may not support this stream format, or the stream may be unavailable.")
                }
            })
            exo.prepare()
            exo.playWhenReady = true
        }
    }

    private fun showError(message: String) {
        val text = TextView(this).apply {
            this.text = message + "\n\nPress Back to return to Allinonetv."
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.rgb(8, 14, 30))
        }
        setContentView(text)
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
