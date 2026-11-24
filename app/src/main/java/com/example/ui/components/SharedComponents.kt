package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AvailabilityStatus
import com.example.data.model.MusicianProfile
import com.example.data.model.PortfolioItem
import com.example.data.model.RateType
import com.example.media.AudioPlayerUiState
import com.example.media.StageVideoSurface
import com.example.ui.theme.InstagramGradientBrush
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageAmberContainer
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageEmerald
import com.example.ui.theme.StageEmeraldContainer
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageOnAmber
import com.example.ui.theme.StageOutline
import com.example.ui.theme.StageRose
import com.example.ui.theme.StageRoseContainer
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextMuted
import com.example.ui.theme.StageTextSecondary
import java.util.Locale

fun resolveDrawableResource(url: String): Int? = when (url) {
    "res://img_stage_hero" -> R.drawable.img_dhaka_stage_hero
    "res://img_studio_guitar" -> R.drawable.img_dhaka_studio_guitar
    "res://img_vocal_booth" -> R.drawable.img_dhaka_vocal_booth
    "res://img_auth_backdrop" -> R.drawable.img_dhaka_auth_backdrop
    else -> null
}

@Composable
fun MediaThumbnailImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val localRes = resolveDrawableResource(url)
    when {
        localRes != null -> {
            Image(
                painter = painterResource(id = localRes),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        url.startsWith("http://") || url.startsWith("https://") -> {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = contentScale,
                placeholder = painterResource(id = R.drawable.img_dhaka_stage_hero),
                error = painterResource(id = R.drawable.img_dhaka_stage_hero),
                modifier = modifier
            )
        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.img_dhaka_stage_hero),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    }
}

@Composable
fun ArtistAvatar(
    name: String,
    avatarUrl: String,
    size: Dp = 44.dp,
    verified: Boolean = false
) {
    Box(modifier = Modifier.size(size)) {
        val localRes = resolveDrawableResource(avatarUrl)
        if (localRes != null || avatarUrl.startsWith("http")) {
            MediaThumbnailImage(
                url = avatarUrl,
                contentDescription = "$name avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.5.dp, StageAmber.copy(alpha = 0.65f), CircleShape)
            )
        } else {
            val initials = name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { "S" }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(StageAmberContainer)
                    .border(1.5.dp, StageAmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleSmall,
                    color = StageAmber,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (verified) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified musician",
                tint = StageAmber,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(StageCharcoal)
            )
        }
    }
}

@Composable
fun InstagramBadgePill(
    handle: String,
    followerCount: Long,
    modifier: Modifier = Modifier
) {
    val formattedFollowers = when {
        followerCount >= 1_000_000L -> String.format(Locale.US, "%.1fM", followerCount / 1_000_000f)
        followerCount >= 1_000L -> String.format(Locale.US, "%.1fK", followerCount / 1_000f)
        else -> followerCount.toString()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(InstagramGradientBrush)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.PhotoCamera,
            contentDescription = "Instagram connected",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "$handle · $formattedFollowers",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AvailabilityPill(status: AvailabilityStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        AvailabilityStatus.AVAILABLE -> StageEmeraldContainer to StageEmerald
        AvailabilityStatus.LIMITED -> StageAmberContainer to StageAmber
        AvailabilityStatus.BOOKED -> StageRoseContainer to StageRose
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun formatFollowerCount(count: Long): String = when {
    count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000f)
    count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000f)
    else -> count.toString()
}

fun formatMillis(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSeconds / 60L
    val secs = totalSeconds % 60L
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

@Composable
fun PersistentMiniAudioBar(
    audioState: AudioPlayerUiState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onDismiss: () -> Unit,
    onOpenArtistProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = audioState.currentTrack ?: return
    val progress = if (audioState.durationMs > 0L) {
        (audioState.positionMs.toFloat() / audioState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = StageSurfaceElevated,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, StageOutline, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                color = StageAmber,
                trackColor = StageOutline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenArtistProfile(track.musicianId) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StageAmberContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = StageAmber
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = StageOffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.musicianName} · ${formatMillis(audioState.positionMs)} / ${
                            if (audioState.durationMs > 0L) formatMillis(audioState.durationMs) else track.duration
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = StageTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.testTag("mini_audio_play_pause")
                ) {
                    Icon(
                        imageVector = if (audioState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (audioState.isPlaying) "Pause audio demo" else "Play audio demo",
                        tint = StageAmber
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("mini_audio_close")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close mini player",
                        tint = StageTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun VideoTheaterDialog(
    item: PortfolioItem,
    musician: MusicianProfile?,
    isOwnProfile: Boolean,
    onDismiss: () -> Unit,
    onBookArtist: (MusicianProfile) -> Unit,
    onViewProfile: (String) -> Unit
) {
    var isPlaying by remember(item.id) { mutableStateOf(true) }
    var isMuted by remember(item.id) { mutableStateOf(false) }
    var isFullscreen by remember(item.id) { mutableStateOf(false) }
    var positionMs by remember(item.id) { mutableLongStateOf(0L) }
    var durationMs by remember(item.id) { mutableLongStateOf(0L) }
    var exoPlayerRef by remember(item.id) { mutableStateOf<ExoPlayer?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val videoModifier = if (isFullscreen) {
                Modifier.fillMaxSize()
            } else {
                val ratio = if (item.isVertical) 9f / 16f else 16f / 9f
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .align(Alignment.Center)
            }

            StageVideoSurface(
                videoUrl = item.sourceUrl,
                isMuted = isMuted,
                isPlaying = isPlaying,
                zoomCrop = isFullscreen || item.isVertical,
                onProgressUpdate = { pos, dur ->
                    positionMs = pos
                    durationMs = dur
                },
                onPlayerReady = { exoPlayerRef = it },
                modifier = videoModifier
            )

            // Top bar overlay
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onDismiss()
                            onViewProfile(item.musicianId)
                        }
                ) {
                    ArtistAvatar(
                        name = musician?.name ?: item.musicianName,
                        avatarUrl = musician?.avatarUrl ?: item.thumbnailUrl,
                        size = 40.dp,
                        verified = musician?.verified ?: false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = musician?.name ?: item.musicianName,
                            style = MaterialTheme.typography.titleSmall,
                            color = StageOffWhite
                        )
                        Text(
                            text = "${item.musicianHandle} · ${item.genreTag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StageAmber
                        )
                    }
                }
                Row {
                    IconButton(onClick = { isMuted = !isMuted }) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute video" else "Mute video",
                            tint = StageOffWhite
                        )
                    }
                    IconButton(onClick = { isFullscreen = !isFullscreen }) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit fullscreen" else "Fullscreen",
                            tint = StageOffWhite
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("theater_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close player",
                            tint = StageOffWhite
                        )
                    }
                }
            }

            // Bottom theater controls + Book Artist CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = StageOffWhite
                )
                if (musician != null && musician.instagramConnected) {
                    InstagramBadgePill(
                        handle = musician.instagramHandle,
                        followerCount = musician.instagramFollowers
                    )
                }

                // Scrubbing timeline
                val sliderFraction = if (durationMs > 0L) {
                    (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.testTag("theater_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = StageAmber
                        )
                    }
                    Slider(
                        value = sliderFraction,
                        onValueChange = { frac ->
                            val dur = exoPlayerRef?.duration ?: 0L
                            if (dur > 0L) {
                                val target = (dur * frac).toLong()
                                exoPlayerRef?.seekTo(target)
                                positionMs = target
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = StageAmber,
                            activeTrackColor = StageAmber,
                            inactiveTrackColor = StageOutline
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${formatMillis(positionMs)} / ${
                            if (durationMs > 0L) formatMillis(durationMs) else item.duration
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = StageTextSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (musician != null) {
                        Text(
                            text = "From ${musician.primaryStartingRateLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = StageAmber,
                            modifier = Modifier.weight(1f)
                        )
                        if (!isOwnProfile) {
                            Button(
                                onClick = { onBookArtist(musician) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StageAmber,
                                    contentColor = StageOnAmber
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("theater_book_artist_button")
                            ) {
                                Text(
                                    text = "Book Artist",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SendBookingOfferSheet(
    musician: MusicianProfile,
    onDismiss: () -> Unit,
    onSubmitOffer: (proposedRate: Long, rateType: RateType, eventDate: String, scopeNotes: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRateType by remember(musician.effectiveUserId) { mutableStateOf(RateType.PER_GIG) }
    val defaultAmount = when (selectedRateType) {
        RateType.HOURLY -> musician.hourlyRate.takeIf { it > 0 } ?: 3500L
        RateType.PER_GIG -> musician.perGigRate.takeIf { it > 0 } ?: 18000L
        RateType.PER_TRACK -> musician.perTrackRate.takeIf { it > 0 } ?: 10000L
    }
    var rateText by remember(musician.effectiveUserId, selectedRateType) {
        mutableStateOf(defaultAmount.toString())
    }
    var eventDate by remember { mutableStateOf("2026-12-20") }
    var scopeNotes by remember {
        mutableStateOf("Dhaka live / studio session — please confirm availability and backline specs.")
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StageSurface,
        contentColor = StageOffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RickshawPaintRibbon(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                height = 5.dp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArtistAvatar(
                    name = musician.name,
                    avatarUrl = musician.avatarUrl,
                    size = 46.dp,
                    verified = musician.verified
                )
                Column {
                    Text(
                        text = "Send Booking Offer",
                        style = MaterialTheme.typography.titleLarge,
                        color = StageOffWhite
                    )
                    Text(
                        text = "To ${musician.name} (${musician.handle})",
                        style = MaterialTheme.typography.bodySmall,
                        color = StageAmber
                    )
                }
            }

            Text(
                text = "Rate Structure (BDT)",
                style = MaterialTheme.typography.labelLarge,
                color = StageOffWhite
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RateType.entries.forEach { rt ->
                    FilterChip(
                        selected = selectedRateType == rt,
                        onClick = { selectedRateType = rt },
                        label = { Text(rt.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StageAmber,
                            selectedLabelColor = StageOnAmber,
                            containerColor = StageSurfaceElevated,
                            labelColor = StageOffWhite
                        )
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Proposed Rate (BDT)") },
                    prefix = { Text("৳") },
                    suffix = { Text(selectedRateType.unitSuffix) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StageAmber,
                        unfocusedBorderColor = StageOutline,
                        focusedTextColor = StageOffWhite,
                        unfocusedTextColor = StageOffWhite
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Session / Event Date") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = StageAmber
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StageAmber,
                        unfocusedBorderColor = StageOutline,
                        focusedTextColor = StageOffWhite,
                        unfocusedTextColor = StageOffWhite
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = scopeNotes,
                onValueChange = { scopeNotes = it },
                label = { Text("Scope, Setlist & Deliverables") },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = StageRose
                )
            }

            Button(
                onClick = {
                    val amount = rateText.toLongOrNull() ?: 0L
                    when {
                        amount <= 0L -> validationError = "Enter a valid rate above ৳0."
                        eventDate.isBlank() -> validationError = "Please enter an event or delivery date."
                        scopeNotes.isBlank() -> validationError = "Please include scope details."
                        else -> {
                            onSubmitOffer(amount, selectedRateType, eventDate.trim(), scopeNotes.trim())
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StageAmber,
                    contentColor = StageOnAmber
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_booking_offer_button")
            ) {
                Text(
                    text = "Send Offer & Open Thread",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
