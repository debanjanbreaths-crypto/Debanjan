package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AvailabilityStatus
import com.example.data.model.MusicianProfile
import com.example.data.model.MusicianReview
import com.example.data.model.PortfolioItem
import com.example.data.model.PortfolioMediaType
import com.example.data.model.formatBdtAmount
import com.example.ui.components.ArtistAvatar
import com.example.ui.components.AvailabilityPill
import com.example.ui.components.InstagramBadgePill
import com.example.ui.components.MediaThumbnailImage
import com.example.ui.components.RickshawPaintRibbon
import com.example.ui.theme.InstagramGradientBrush
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageAmberContainer
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageOnAmber
import com.example.ui.theme.StageOutline
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicianProfileScreen(
    musicianId: String,
    currentUserId: String,
    musiciansState: UiState<List<MusicianProfile>>,
    portfolioState: UiState<List<PortfolioItem>>,
    reviews: List<MusicianReview>,
    onSelectProfile: (String) -> Unit,
    onBack: () -> Unit,
    onOpenVideoTheater: (PortfolioItem) -> Unit,
    onPlayAudioDemo: (PortfolioItem) -> Unit,
    onBookNow: (MusicianProfile) -> Unit,
    onSubmitReview: (musicianId: String, rating: Long, comment: String) -> Unit
) {
    BackHandler { onBack() }

    LaunchedEffect(musicianId) {
        onSelectProfile(musicianId)
    }

    val musicians = (musiciansState as? UiState.Success)?.data.orEmpty()
    val musician = musicians.firstOrNull { it.effectiveUserId == musicianId }
    val allItems = (portfolioState as? UiState.Success)?.data.orEmpty()
        .filter { it.musicianId == musicianId }
    val videosAndReels = remember(allItems) { allItems.filter { it.isVideoOrReel } }
    val audioDemos = remember(allItems) { allItems.filter { !it.isVideoOrReel } }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showReviewSheet by remember { mutableStateOf(false) }

    if (musician == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(StageCharcoal),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Musician profile not found", color = StageOffWhite)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onBack) { Text("Back", color = StageAmber) }
            }
        }
        return
    }

    val isOwnProfile = musician.effectiveUserId == currentUserId

    LazyColumn(
        contentPadding = PaddingValues(bottom = 110.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        // Header Cover + Profile Info
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                MediaThumbnailImage(
                    url = musician.coverUrl,
                    contentDescription = "${musician.name} cover banner",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    StageCharcoal.copy(alpha = 0.25f),
                                    StageCharcoal.copy(alpha = 0.72f),
                                    StageCharcoal
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(StageCharcoal.copy(alpha = 0.75f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StageOffWhite
                    )
                }

                AvailabilityPill(
                    status = AvailabilityStatus.fromRaw(musician.availabilityStatus),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    ArtistAvatar(
                        name = musician.name,
                        avatarUrl = musician.avatarUrl,
                        size = 76.dp,
                        verified = musician.verified
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = musician.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = StageOffWhite
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = musician.handle,
                                style = MaterialTheme.typography.labelMedium,
                                color = StageAmber
                            )
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = StageTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = musician.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = StageTextSecondary
                            )
                        }
                    }
                }
            }
            RickshawPaintRibbon(height = 4.dp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Badges, Bio & Action CTAs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (musician.instagramConnected && musician.instagramHandle.isNotBlank()) {
                        InstagramBadgePill(
                            handle = musician.instagramHandle,
                            followerCount = musician.instagramFollowers
                        )
                    }
                    musician.specialties.forEach { spec ->
                        Surface(color = StageAmberContainer, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                text = spec,
                                style = MaterialTheme.typography.labelSmall,
                                color = StageAmber,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    musician.genres.forEach { genre ->
                        Surface(color = StageSurfaceElevated, shape = RoundedCornerShape(999.dp)) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = StageOffWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Text(
                    text = musician.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StageOffWhite
                )

                if (isOwnProfile) {
                    Surface(
                        color = StageSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This is your public ক্ষ্যাপ profile. Edit rates, Instagram sync, and reels in the Studio tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StageAmber,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { onBookNow(musician) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StageAmber,
                                contentColor = StageOnAmber
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("profile_book_now_button")
                        ) {
                            Text("Book Now · ${musician.primaryStartingRateLabel}", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onBookNow(musician) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = StageAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Message", color = StageOffWhite)
                        }
                    }
                }
            }
        }

        // 4 Profile Tabs
        item {
            Spacer(modifier = Modifier.height(14.dp))
            val tabs = listOf("Videos & Reels", "Audio Demos", "Rates", "Gear & Reviews")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = StageSurface,
                contentColor = StageAmber,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = StageAmber
                    )
                }
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTab == idx) StageAmber else StageTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (videosAndReels.isEmpty()) {
                            Text(
                                text = "No videos or Instagram Reels uploaded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StageTextSecondary
                            )
                        } else {
                            videosAndReels.chunked(2).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { item ->
                                        PortfolioVideoTile(
                                            item = item,
                                            onClick = { onOpenVideoTheater(item) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (audioDemos.isEmpty()) {
                            Text(
                                text = "No audio demos posted yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StageTextSecondary
                            )
                        } else {
                            audioDemos.forEach { audio ->
                                AudioDemoRowCard(
                                    item = audio,
                                    onPlay = { onPlayAudioDemo(audio) }
                                )
                            }
                        }
                    }
                    2 -> {
                        RateCardRow("Hourly Studio / Rehearsal", musician.hourlyRate, "/hr")
                        RateCardRow("Live Performance / Gig", musician.perGigRate, "/gig")
                        RateCardRow("Remote Session / Per Track", musician.perTrackRate, "/track")
                    }
                    3 -> {
                        Surface(
                            color = StageSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, StageOutline, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Gear & Studio Rig", style = MaterialTheme.typography.titleMedium, color = StageAmber)
                                Text(
                                    text = musician.gearList.ifBlank { "Standard backline & studio setup" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StageOffWhite
                                )
                                Text(
                                    text = "Instruments: ${musician.instruments.joinToString(", ").ifBlank { "Multi-instrumentalist" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StageTextSecondary
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Verified Client Reviews", style = MaterialTheme.typography.titleMedium, color = StageOffWhite)
                            if (!isOwnProfile) {
                                OutlinedButton(onClick = { showReviewSheet = true }) {
                                    Text("Leave Review", color = StageAmber)
                                }
                            }
                        }

                        if (reviews.isEmpty()) {
                            Text("No reviews yet.", color = StageTextSecondary, style = MaterialTheme.typography.bodySmall)
                        } else {
                            reviews.forEach { rev ->
                                Surface(
                                    color = StageSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, StageOutline, RoundedCornerShape(14.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(rev.authorName, style = MaterialTheme.typography.titleSmall, color = StageOffWhite)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = StageAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("${rev.rating}.0", style = MaterialTheme.typography.labelMedium, color = StageAmber)
                                            }
                                        }
                                        Text(rev.comment, style = MaterialTheme.typography.bodyMedium, color = StageTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReviewSheet) {
        WriteReviewSheet(
            musicianName = musician.name,
            onDismiss = { showReviewSheet = false },
            onSubmit = { rating, comment ->
                onSubmitReview(musician.effectiveUserId, rating, comment)
                showReviewSheet = false
            }
        )
    }
}

@Composable
private fun PortfolioVideoTile(
    item: PortfolioItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaEnum = PortfolioMediaType.fromRaw(item.mediaType)
    Box(
        modifier = modifier
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(16.dp))
            .background(StageSurface)
            .border(1.dp, StageOutline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        MediaThumbnailImage(
            url = item.thumbnailUrl,
            contentDescription = item.title,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f))
                    )
                )
        )
        if (mediaEnum == PortfolioMediaType.INSTAGRAM_REEL) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(InstagramGradientBrush)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("IG REEL", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            color = StageCharcoal.copy(alpha = 0.8f),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.Center)
                .size(42.dp)
                .border(1.dp, StageAmber, CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play video", tint = StageAmber)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                color = StageOffWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.duration} · ${item.genreTag}",
                style = MaterialTheme.typography.labelSmall,
                color = StageAmber
            )
        }
    }
}

@Composable
private fun AudioDemoRowCard(
    item: PortfolioItem,
    onPlay: () -> Unit
) {
    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StageOutline, RoundedCornerShape(14.dp))
            .clickable(onClick = onPlay)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(StageAmber),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play audio demo", tint = StageOnAmber)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, color = StageOffWhite)
                Text("${item.genreTag} · ${item.duration}", style = MaterialTheme.typography.labelSmall, color = StageTextSecondary)
            }
            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = StageAmber)
        }
    }
}

@Composable
private fun RateCardRow(label: String, amount: Long, suffix: String) {
    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StageOutline, RoundedCornerShape(16.dp))
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = StageOffWhite)
            Text(
                text = if (amount > 0L) "${formatBdtAmount(amount)}$suffix" else "Custom Quote",
                style = MaterialTheme.typography.titleMedium,
                color = StageAmber,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteReviewSheet(
    musicianName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Long, comment: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rating by remember { mutableLongStateOf(5L) }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StageSurface,
        contentColor = StageOffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Review $musicianName", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1L..5L).forEach { star ->
                    IconButton(onClick = { rating = star }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "$star stars",
                            tint = if (star <= rating) StageAmber else StageOutline,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Share how the session or gig went") },
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (comment.isNotBlank()) onSubmit(rating, comment.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Submit Review", fontWeight = FontWeight.Bold)
            }
        }
    }
}
