package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : Activity() {
    private var player: ExoPlayer? = null
    private var statusView: TextView? = null
    private var audioTrackStatus = "Checking audio track…"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val streamUrl = intent.getStringExtra("stream_url")
        val streamTitle = intent.getStringExtra("stream_title") ?: "Allinonetv Player"

        if (streamUrl.isNullOrBlank() ||
            !(streamUrl.startsWith("https://", true) || streamUrl.startsWith("http://", true))) {
            showError("No valid stream was provided. Please return and try again.")
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }

        val heading = TextView(this).apply {
            text = streamTitle
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
            setShutterBackgroundColor(Color.BLACK)
        }
        root.addView(view, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        statusView = TextView(this).apply {
            text = "Connecting to channel…"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
        }
        root.addView(statusView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 44
        ))

        setContentView(root)

        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Allinonetv Android TV Player")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(30000)
            .setAllowCrossProtocolRedirects(true)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
                    .setDataSourceFactory(httpFactory)
            )
            .build()
            .also { exo ->
                view.player = exo
                exo.setAudioAttributes(audioAttributes, true)
                exo.setHandleAudioBecomingNoisy(true)
                exo.volume = 1.0f
                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        statusView?.text = when (playbackState) {
                            Player.STATE_IDLE -> "Player ready"
                            Player.STATE_BUFFERING -> "Connecting / buffering…"
                            Player.STATE_READY -> if (exo.playWhenReady) "Playing — $audioTrackStatus" else "Ready to play — $audioTrackStatus"
                            Player.STATE_ENDED -> "Stream ended"
                            else -> "Connecting to channel…"
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        // Keep the audio-track diagnostic visible instead of replacing it.
                        if (!isPlaying && exo.playbackState == Player.STATE_BUFFERING) {
                            statusView?.text = "Connecting / buffering…"
                        }
                    }

                    override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                        val audioGroups = tracks.groups.filter {
                            it.type == C.TRACK_TYPE_AUDIO
                        }
                        audioTrackStatus = when {
                            audioGroups.isEmpty() -> "no audio track detected"
                            audioGroups.any { it.isSupported } ->
                                "audio track supported"
                            else -> "audio format unsupported"
                        }
                        statusView?.text = if (exo.playbackState == Player.STATE_READY && exo.playWhenReady) {
                            "Playing — $audioTrackStatus"
                        } else {
                            audioTrackStatus
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        statusView?.text = "Playback error: ${error.errorCodeName}"
                    }
                })
                exo.setMediaItem(MediaItem.fromUri(streamUrl))
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
