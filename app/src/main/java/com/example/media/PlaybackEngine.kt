package com.example.media

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.PortfolioItem
import com.example.ui.theme.StageAmber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlayerUiState(
    val currentTrack: PortfolioItem? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null
)

class PersistentAudioController(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main.immediate)
    private var progressJob: Job? = null

    private var player: ExoPlayer? = null

    private val _state = MutableStateFlow(AudioPlayerUiState())
    val state: StateFlow<AudioPlayerUiState> = _state.asStateFlow()

    private fun getOrCreatePlayer(): ExoPlayer {
        player?.let { return it }
        return ExoPlayer.Builder(appContext).build().also { exo ->
            exo.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _state.update {
                        it.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            durationMs = exo.duration.coerceAtLeast(0L)
                        )
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        exo.seekTo(0L)
                        exo.pause()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            errorMessage = "Audio stream unavailable"
                        )
                    }
                }
            })
            player = exo
        }
    }

    fun playTrack(track: PortfolioItem) {
        val exo = getOrCreatePlayer()
        if (_state.value.currentTrack?.id == track.id && _state.value.errorMessage == null) {
            togglePlayPause()
            return
        }
        _state.update {
            it.copy(
                currentTrack = track,
                isBuffering = true,
                positionMs = 0L,
                durationMs = 0L,
                errorMessage = null
            )
        }
        exo.setMediaItem(MediaItem.fromUri(Uri.parse(track.sourceUrl)))
        exo.prepare()
        exo.play()
        startProgressLoop()
    }

    fun togglePlayPause() {
        val exo = player ?: return
        if (exo.isPlaying) {
            exo.pause()
        } else {
            exo.play()
        }
    }

    fun seekToFraction(fraction: Float) {
        val exo = player ?: return
        val duration = exo.duration.takeIf { it > 0L } ?: return
        val target = (duration * fraction.coerceIn(0f, 1f)).toLong()
        exo.seekTo(target)
        _state.update { it.copy(positionMs = target) }
    }

    fun dismiss() {
        progressJob?.cancel()
        player?.stop()
        _state.value = AudioPlayerUiState()
    }

    fun release() {
        progressJob?.cancel()
        player?.release()
        player = null
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val exo = player
                if (exo != null && _state.value.currentTrack != null) {
                    _state.update {
                        it.copy(
                            positionMs = exo.currentPosition.coerceAtLeast(0L),
                            durationMs = exo.duration.coerceAtLeast(it.durationMs)
                        )
                    }
                }
                delay(350L)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun StageVideoSurface(
    videoUrl: String,
    isMuted: Boolean,
    isPlaying: Boolean,
    zoomCrop: Boolean = true,
    onProgressUpdate: ((positionMs: Long, durationMs: Long) -> Unit)? = null,
    onPlayerReady: ((ExoPlayer) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBuffering by remember(videoUrl) { mutableStateOf(true) }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
        }
    }

    LaunchedEffect(exoPlayer) {
        onPlayerReady?.invoke(exoPlayer)
    }

    LaunchedEffect(isMuted, exoPlayer) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isPlaying, exoPlayer) {
        exoPlayer.playWhenReady = isPlaying
    }

    LaunchedEffect(exoPlayer, onProgressUpdate) {
        if (onProgressUpdate != null) {
            while (true) {
                onProgressUpdate(
                    exoPlayer.currentPosition.coerceAtLeast(0L),
                    exoPlayer.duration.coerceAtLeast(0L)
                )
                delay(250L)
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = if (zoomCrop) {
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    } else {
                        AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = exoPlayer
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = if (zoomCrop) {
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (isBuffering) {
            CircularProgressIndicator(
                color = StageAmber,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
