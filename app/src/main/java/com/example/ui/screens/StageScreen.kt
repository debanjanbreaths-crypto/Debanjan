package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.FeedPost
import com.example.data.model.MusicianProfile
import com.example.data.model.PortfolioItem
import com.example.data.model.PortfolioMediaType
import com.example.data.model.PostComment
import com.example.data.model.PostMediaType
import com.example.data.model.PostType
import com.example.data.model.SocialUrlValidator
import com.example.media.StageVideoSurface
import com.example.ui.components.ArtistAvatar
import com.example.ui.components.DhakaHeritagePill
import com.example.ui.components.JaminiRoyLotusHeaderBackdrop
import com.example.ui.components.MediaThumbnailImage
import com.example.ui.components.RickshawPaintRibbon
import com.example.ui.theme.InstagramGradientBrush
import com.example.ui.theme.JaminiTerracotta
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageAmberContainer
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageOnAmber
import com.example.ui.theme.StageOutline
import com.example.ui.theme.StageRose
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.UiState

@Composable
fun StageScreen(
    currentUserId: String,
    postsState: UiState<List<FeedPost>>,
    musiciansState: UiState<List<MusicianProfile>>,
    portfolioState: UiState<List<PortfolioItem>>,
    likedPostIds: Set<String>,
    commentsState: UiState<List<PostComment>>,
    onToggleLike: (FeedPost) -> Unit,
    onOpenComments: (FeedPost) -> Unit,
    onSubmitComment: (FeedPost, String) -> Unit,
    onDismissComments: () -> Unit,
    onCreatePost: (content: String, postType: PostType, mediaUrl: String, pickedUri: String?) -> Unit,
    onOpenVideoTheater: (PortfolioItem) -> Unit,
    onPlayAudioDemo: (PortfolioItem) -> Unit,
    onOpenProfile: (String) -> Unit,
    onBookArtist: (MusicianProfile) -> Unit
) {
    var reelsMode by remember { mutableStateOf(false) }
    var showCreatePostSheet by remember { mutableStateOf(false) }
    var activeCommentsPost by remember { mutableStateOf<FeedPost?>(null) }

    val musicians = (musiciansState as? UiState.Success)?.data.orEmpty()
    val videoItems = remember(portfolioState) {
        ((portfolioState as? UiState.Success)?.data.orEmpty()).filter { it.isVideoOrReel }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Stage Header + All Posts / Video Reels Toggle
            Surface(
                color = StageSurface.copy(alpha = 0.96f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        JaminiRoyLotusHeaderBackdrop(modifier = Modifier.matchParentSize())
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, JaminiTerracotta, CircleShape)
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = painterResource(id = R.drawable.khyap_icon),
                                            contentDescription = "ক্ষ্যাপ logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "ক্ষ্যাপ",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = StageOffWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Dhaka live jams, reels & studio sessions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StageTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier.testTag("stage_mode_toggle")
                                ) {
                                    SegmentedButton(
                                        selected = !reelsMode,
                                        onClick = { reelsMode = false },
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = StageAmber,
                                            activeContentColor = StageOnAmber,
                                            inactiveContainerColor = StageSurfaceElevated,
                                            inactiveContentColor = StageOffWhite
                                        )
                                    ) {
                                        Text("All Posts", style = MaterialTheme.typography.labelSmall)
                                    }
                                    SegmentedButton(
                                        selected = reelsMode,
                                        onClick = { reelsMode = true },
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                        colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = StageAmber,
                                            activeContentColor = StageOnAmber,
                                            inactiveContainerColor = StageSurfaceElevated,
                                            inactiveContentColor = StageOffWhite
                                        )
                                    ) {
                                        Text("Video Reels", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                    RickshawPaintRibbon(height = 4.dp)
                }
            }

            if (reelsMode) {
                StageVerticalReelsFeed(
                    currentUserId = currentUserId,
                    reels = videoItems,
                    musicians = musicians,
                    onOpenProfile = onOpenProfile,
                    onBookArtist = onBookArtist,
                    onExpandTheater = onOpenVideoTheater
                )
            } else {
                when (postsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = StageAmber)
                        }
                    }
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = postsState.message,
                                color = StageRose,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                    is UiState.Success -> {
                        val listState = rememberLazyListState()
                        val firstVisibleIndex by remember {
                            derivedStateOf { listState.firstVisibleItemIndex }
                        }

                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "dhaka_stage_banner") {
                                DhakaShowcaseBanner()
                            }
                            itemsIndexed(
                                items = postsState.data,
                                key = { _, post -> post.id }
                            ) { index, post ->
                                val isLiked = post.id in likedPostIds
                                val isAutoPreviewActive = (index + 1 == firstVisibleIndex ||
                                    (firstVisibleIndex == 0 && index == 0)) &&
                                    (PostMediaType.fromRaw(post.mediaType) == PostMediaType.VIDEO ||
                                        PostMediaType.fromRaw(post.mediaType) == PostMediaType.REEL_EMBED)
                                StagePostCard(
                                    post = post,
                                    isLiked = isLiked,
                                    isAutoPreviewActive = isAutoPreviewActive,
                                    onLike = { onToggleLike(post) },
                                    onComment = {
                                        activeCommentsPost = post
                                        onOpenComments(post)
                                    },
                                    onOpenAuthor = { onOpenProfile(post.authorId) },
                                    onPlayVideo = {
                                        onOpenVideoTheater(
                                            PortfolioItem(
                                                id = "post_media_${post.id}",
                                                musicianId = post.authorId,
                                                musicianName = post.authorName,
                                                musicianHandle = post.authorHandle,
                                                title = post.content.take(64),
                                                mediaType = if (post.mediaType == PostMediaType.REEL_EMBED.name) {
                                                    PortfolioMediaType.INSTAGRAM_REEL.name
                                                } else {
                                                    PortfolioMediaType.NATIVE_VIDEO.name
                                                },
                                                sourceUrl = post.mediaUrl,
                                                thumbnailUrl = post.thumbnailUrl,
                                                aspectRatio = "9:16",
                                                genreTag = PostType.fromRaw(post.postType).label,
                                                duration = "0:32"
                                            )
                                        )
                                    },
                                    onPlayAudio = {
                                        onPlayAudioDemo(
                                            PortfolioItem(
                                                id = "post_audio_${post.id}",
                                                musicianId = post.authorId,
                                                musicianName = post.authorName,
                                                musicianHandle = post.authorHandle,
                                                title = "${post.authorName} — Stage Audio Clip",
                                                mediaType = PortfolioMediaType.AUDIO.name,
                                                sourceUrl = post.mediaUrl,
                                                thumbnailUrl = post.thumbnailUrl,
                                                aspectRatio = "1:1",
                                                genreTag = PostType.fromRaw(post.postType).label,
                                                duration = "1:45"
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!reelsMode) {
            FloatingActionButton(
                onClick = { showCreatePostSheet = true },
                containerColor = StageAmber,
                contentColor = StageOnAmber,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
                    .testTag("create_post_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create post")
            }
        }
    }

    if (showCreatePostSheet) {
        CreatePostSheet(
            onDismiss = { showCreatePostSheet = false },
            onSubmit = { content, type, url, picked ->
                onCreatePost(content, type, url, picked)
                showCreatePostSheet = false
            }
        )
    }

    activeCommentsPost?.let { post ->
        PostCommentsSheet(
            post = post,
            commentsState = commentsState,
            onDismiss = {
                activeCommentsPost = null
                onDismissComments()
            },
            onSendComment = { text -> onSubmitComment(post, text) }
        )
    }
}

@Composable
private fun DhakaShowcaseBanner() {
    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .border(1.5.dp, JaminiTerracotta.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.img_dhaka_stage_hero),
                    contentDescription = "Dhaka Jamini Roy stage artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    StageCharcoal.copy(alpha = 0.25f),
                                    StageCharcoal.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                DhakaHeritagePill(
                    text = "DHAKA SOUND COLLECTIVE · ৳ BDT",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "ক্ষ্যাপ — Dhaka Live & Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = StageOffWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Banani, Dhanmondi, Uttara & Gulshan musicians — ready for shows and studio tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = StageTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            RickshawPaintRibbon(height = 5.dp)
        }
    }
}

@Composable
private fun StagePostCard(
    post: FeedPost,
    isLiked: Boolean,
    isAutoPreviewActive: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onOpenAuthor: () -> Unit,
    onPlayVideo: () -> Unit,
    onPlayAudio: () -> Unit
) {
    val context = LocalContext.current
    val mediaType = PostMediaType.fromRaw(post.mediaType)
    val postType = PostType.fromRaw(post.postType)

    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .border(1.5.dp, StageOutline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenAuthor)
                ) {
                    ArtistAvatar(
                        name = post.authorName,
                        avatarUrl = post.authorAvatarUrl,
                        size = 44.dp,
                        verified = post.authorVerified
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            style = MaterialTheme.typography.titleSmall,
                            color = StageOffWhite
                        )
                        Text(
                            text = post.authorHandle,
                            style = MaterialTheme.typography.labelSmall,
                            color = StageTextSecondary
                        )
                    }
                }
                Surface(
                    color = StageAmberContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = postType.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = StageAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = StageOffWhite
            )

            when (mediaType) {
                PostMediaType.VIDEO, PostMediaType.REEL_EMBED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black)
                            .clickable(onClick = onPlayVideo)
                    ) {
                        if (isAutoPreviewActive && post.mediaUrl.isNotBlank()) {
                            StageVideoSurface(
                                videoUrl = post.mediaUrl,
                                isMuted = true,
                                isPlaying = true,
                                zoomCrop = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            MediaThumbnailImage(
                                url = post.thumbnailUrl,
                                contentDescription = "Video thumbnail",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                    )
                                )
                        )
                        if (mediaType == PostMediaType.REEL_EMBED) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(InstagramGradientBrush)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "REEL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            color = StageCharcoal.copy(alpha = 0.8f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(52.dp)
                                .border(1.5.dp, StageAmber, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Open theater player",
                                    tint = StageAmber,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isAutoPreviewActive) "Muted preview · Tap to expand" else "Tap to play",
                            style = MaterialTheme.typography.labelSmall,
                            color = StageOffWhite,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        )
                    }
                }
                PostMediaType.AUDIO -> {
                    Surface(
                        color = StageSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StageOutline, RoundedCornerShape(14.dp))
                            .clickable(onClick = onPlayAudio)
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
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play audio demo",
                                    tint = StageOnAmber
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Audio Demo Track",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = StageOffWhite
                                )
                                Text(
                                    text = "Tap to stream in mini player",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StageTextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = StageAmber
                            )
                        }
                    }
                }
                PostMediaType.IMAGE -> {
                    MediaThumbnailImage(
                        url = post.thumbnailUrl.ifBlank { post.mediaUrl },
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                PostMediaType.NONE -> Unit
            }

            // Action bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like post",
                            tint = if (isLiked) StageRose else StageTextSecondary
                        )
                    }
                    Text(
                        text = "${post.likesCount + if (isLiked && post.isSeeded) 1 else 0}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StageTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onComment) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = StageTextSecondary
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StageTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${post.authorName} on ক্ষ্যাপ: ${post.content}"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share ক্ষ্যাপ clip"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share post",
                            tint = StageTextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenAuthor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "View Profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = StageAmber
                    )
                }
            }
        }
    }
}

@Composable
private fun StageVerticalReelsFeed(
    currentUserId: String,
    reels: List<PortfolioItem>,
    musicians: List<MusicianProfile>,
    onOpenProfile: (String) -> Unit,
    onBookArtist: (MusicianProfile) -> Unit,
    onExpandTheater: (PortfolioItem) -> Unit
) {
    if (reels.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No video reels available yet.", color = StageTextSecondary)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })
    var isMuted by remember { mutableStateOf(false) }

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val reel = reels[page]
        val musician = musicians.firstOrNull { it.effectiveUserId == reel.musicianId }
        val isOwn = reel.musicianId == currentUserId

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onExpandTheater(reel) }
        ) {
            StageVideoSurface(
                videoUrl = reel.sourceUrl,
                isMuted = isMuted,
                isPlaying = pagerState.currentPage == page,
                zoomCrop = true,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(StageCharcoal.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute reel" else "Mute reel",
                    tint = StageOffWhite
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenProfile(reel.musicianId) }
                ) {
                    ArtistAvatar(
                        name = musician?.name ?: reel.musicianName,
                        avatarUrl = musician?.avatarUrl ?: reel.thumbnailUrl,
                        size = 44.dp,
                        verified = musician?.verified ?: false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = musician?.name ?: reel.musicianName,
                            style = MaterialTheme.typography.titleMedium,
                            color = StageOffWhite
                        )
                        Text(
                            text = "${reel.musicianHandle} · ${reel.genreTag}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StageAmber
                        )
                    }
                }
                Text(
                    text = reel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = StageOffWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (musician != null && !isOwn) {
                        Button(
                            onClick = { onBookArtist(musician) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StageAmber,
                                contentColor = StageOnAmber
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Book ${musician.primaryStartingRateLabel}", fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { onOpenProfile(reel.musicianId) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Profile", color = StageOffWhite)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreatePostSheet(
    onDismiss: () -> Unit,
    onSubmit: (content: String, postType: PostType, mediaUrl: String, pickedUri: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PostType.JAM_CLIP) }
    var mediaUrl by remember { mutableStateOf("") }
    var pickedVideoUri by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedVideoUri = uri.toString()
            errorMsg = null
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Post to ক্ষ্যাপ", style = MaterialTheme.typography.titleLarge)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StageAmber,
                            selectedLabelColor = StageOnAmber,
                            containerColor = StageSurfaceElevated,
                            labelColor = StageOffWhite
                        )
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Share a jam, live update, or call for collaborators") },
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

            OutlinedTextField(
                value = mediaUrl,
                onValueChange = {
                    mediaUrl = it
                    errorMsg = null
                },
                label = { Text("Instagram Reel, YouTube, or MP4/MP3 URL (optional)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    videoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = StageAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (pickedVideoUri != null) "Video Clip Attached" else "Attach Video from Device (Photo Picker)",
                    color = StageOffWhite
                )
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = StageRose, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (content.isBlank()) {
                        errorMsg = "Write something before posting."
                        return@Button
                    }
                    if (pickedVideoUri == null && mediaUrl.isNotBlank() &&
                        SocialUrlValidator.parseAndValidate(mediaUrl) == null
                    ) {
                        errorMsg = "Enter a valid Instagram Reel/Post, YouTube, or direct MP4/MP3 URL."
                        return@Button
                    }
                    onSubmit(content.trim(), selectedType, mediaUrl.trim(), pickedVideoUri)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StageAmber,
                    contentColor = StageOnAmber
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Publish to Stage", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostCommentsSheet(
    post: FeedPost,
    commentsState: UiState<List<PostComment>>,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentDraft by remember { mutableStateOf("") }

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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Comments on ${post.authorName}'s post",
                style = MaterialTheme.typography.titleMedium
            )

            when (commentsState) {
                is UiState.Loading -> CircularProgressIndicator(color = StageAmber)
                is UiState.Error -> Text(commentsState.message, color = StageRose)
                is UiState.Success -> {
                    if (commentsState.data.isEmpty()) {
                        Text(
                            text = "No comments yet. Start the conversation!",
                            style = MaterialTheme.typography.bodySmall,
                            color = StageTextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            commentsState.data.take(10).forEach { comment ->
                                Surface(
                                    color = StageSurfaceElevated,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${comment.authorName} · ${comment.authorHandle}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StageAmber
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = comment.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = StageOffWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    placeholder = { Text("Add a comment...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StageAmber,
                        unfocusedBorderColor = StageOutline,
                        focusedTextColor = StageOffWhite,
                        unfocusedTextColor = StageOffWhite
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (commentDraft.isNotBlank()) {
                            onSendComment(commentDraft.trim())
                            commentDraft = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send comment",
                        tint = StageAmber
                    )
                }
            }
        }
    }
}
