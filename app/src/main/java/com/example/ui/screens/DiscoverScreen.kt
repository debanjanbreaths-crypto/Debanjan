package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AvailabilityStatus
import com.example.data.model.MusicianProfile
import com.example.data.model.PortfolioItem
import com.example.data.model.formatBdtAmount
import com.example.data.seed.SeedCatalog
import com.example.media.StageVideoSurface
import com.example.ui.components.ArtistAvatar
import com.example.ui.components.AvailabilityPill
import com.example.ui.components.DhakaHeritagePill
import com.example.ui.components.InstagramBadgePill
import com.example.ui.components.JaminiRoyLotusHeaderBackdrop
import com.example.ui.components.MediaThumbnailImage
import com.example.ui.components.RickshawPaintRibbon
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
import com.example.ui.viewmodel.DiscoverFilterState
import com.example.ui.viewmodel.UiState

@Composable
fun DiscoverScreen(
    currentUserId: String,
    musiciansState: UiState<List<MusicianProfile>>,
    portfolioState: UiState<List<PortfolioItem>>,
    filters: DiscoverFilterState,
    onUpdateFilters: ((DiscoverFilterState) -> DiscoverFilterState) -> Unit,
    onResetFilters: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenVideoTheater: (PortfolioItem) -> Unit,
    onBookArtist: (MusicianProfile) -> Unit
) {
    var showFiltersSheet by remember { mutableStateOf(false) }
    val portfolioItems = (portfolioState as? UiState.Success)?.data.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        // Search & Filter Bar
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
                            Text(
                                text = "Discover Talent",
                                style = MaterialTheme.typography.headlineMedium,
                                color = StageOffWhite
                            )
                            DhakaHeritagePill()
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = filters.searchQuery,
                                onValueChange = { q -> onUpdateFilters { it.copy(searchQuery = q) } },
                                placeholder = { Text("Search Dhaka artists, Bangla rock, sitar...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = StageAmber)
                        },
                        trailingIcon = {
                            if (filters.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onUpdateFilters { it.copy(searchQuery = "") } }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear search", tint = StageTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StageAmber,
                            unfocusedBorderColor = StageOutline,
                            focusedTextColor = StageOffWhite,
                            unfocusedTextColor = StageOffWhite,
                            focusedContainerColor = StageSurfaceElevated,
                            unfocusedContainerColor = StageSurfaceElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("discover_search_input")
                    )

                    Surface(
                        color = StageSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .size(54.dp)
                            .border(1.dp, StageAmber.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable { showFiltersSheet = true }
                            .testTag("discover_filter_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Open talent filters",
                                tint = StageAmber
                            )
                        }
                    }
                }

                // Quick Specialty Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filters.selectedSpecialty == null,
                            onClick = { onUpdateFilters { it.copy(selectedSpecialty = null) } },
                            label = { Text("All Talent") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StageAmber,
                                selectedLabelColor = StageOnAmber,
                                containerColor = StageSurfaceElevated,
                                labelColor = StageOffWhite
                            )
                        )
                    }
                    items(SeedCatalog.allSpecialties) { spec ->
                        FilterChip(
                            selected = filters.selectedSpecialty == spec,
                            onClick = {
                                onUpdateFilters {
                                    it.copy(selectedSpecialty = if (it.selectedSpecialty == spec) null else spec)
                                }
                            },
                            label = { Text(spec) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StageAmber,
                                selectedLabelColor = StageOnAmber,
                                containerColor = StageSurfaceElevated,
                                labelColor = StageOffWhite
                            )
                        )
                    }
                }
            }
                }
                RickshawPaintRibbon(height = 4.dp)
            }
        }

        when (musiciansState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StageAmber)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(musiciansState.message, color = StageRose, modifier = Modifier.padding(24.dp))
                }
            }
            is UiState.Success -> {
                if (musiciansState.data.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "No musicians match those filters.",
                                style = MaterialTheme.typography.titleMedium,
                                color = StageOffWhite
                            )
                            TextButton(onClick = onResetFilters) {
                                Text("Reset Filters", color = StageAmber)
                            }
                        }
                    }
                } else {
                    val gridState = rememberLazyGridState()
                    val firstVisible by remember { derivedStateOf { gridState.firstVisibleItemIndex } }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 310.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = musiciansState.data,
                            key = { _, m -> m.effectiveUserId }
                        ) { index, musician ->
                            val primaryVideo = remember(portfolioItems, musician.effectiveUserId) {
                                portfolioItems.firstOrNull {
                                    it.musicianId == musician.effectiveUserId && it.isVideoOrReel
                                }
                            }
                            MusicianDirectoryCard(
                                musician = musician,
                                primaryVideo = primaryVideo,
                                isAutoPreviewActive = index == firstVisible && primaryVideo != null,
                                isOwnProfile = musician.effectiveUserId == currentUserId,
                                onOpenProfile = { onOpenProfile(musician.effectiveUserId) },
                                onPlayVideo = { video -> onOpenVideoTheater(video) },
                                onBookNow = { onBookArtist(musician) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFiltersSheet) {
        DiscoverFiltersSheet(
            filters = filters,
            onDismiss = { showFiltersSheet = false },
            onUpdateFilters = onUpdateFilters,
            onResetFilters = onResetFilters
        )
    }
}

@Composable
private fun MusicianDirectoryCard(
    musician: MusicianProfile,
    primaryVideo: PortfolioItem?,
    isAutoPreviewActive: Boolean,
    isOwnProfile: Boolean,
    onOpenProfile: () -> Unit,
    onPlayVideo: (PortfolioItem) -> Unit,
    onBookNow: () -> Unit
) {
    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, StageOutline, RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenProfile)
            .testTag("musician_card_${musician.effectiveUserId}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Interactive Video / Cover Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .clickable {
                        if (primaryVideo != null) onPlayVideo(primaryVideo) else onOpenProfile()
                    }
            ) {
                if (isAutoPreviewActive && primaryVideo != null) {
                    StageVideoSurface(
                        videoUrl = primaryVideo.sourceUrl,
                        isMuted = true,
                        isPlaying = true,
                        zoomCrop = true,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    MediaThumbnailImage(
                        url = primaryVideo?.thumbnailUrl?.ifBlank { musician.coverUrl } ?: musician.coverUrl,
                        contentDescription = "${musician.name} showcase preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                AvailabilityPill(
                    status = AvailabilityStatus.fromRaw(musician.availabilityStatus),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                )

                Surface(
                    color = StageCharcoal.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .border(1.dp, StageAmber.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                ) {
                    Text(
                        text = musician.primaryStartingRateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = StageAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (primaryVideo != null) {
                    Surface(
                        color = StageCharcoal.copy(alpha = 0.78f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(46.dp)
                            .border(1.dp, StageAmber, CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Watch video demo",
                                tint = StageAmber
                            )
                        }
                    }
                }
            }

            RickshawPaintRibbon(height = 3.dp)

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ArtistAvatar(
                        name = musician.name,
                        avatarUrl = musician.avatarUrl,
                        size = 46.dp,
                        verified = musician.verified
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = musician.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = StageOffWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = StageTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = musician.location + if (musician.remoteAvailable) " · Remote OK" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = StageTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (musician.instagramConnected && musician.instagramHandle.isNotBlank()) {
                    InstagramBadgePill(
                        handle = musician.instagramHandle,
                        followerCount = musician.instagramFollowers
                    )
                }

                // Specialties + Genres tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (musician.specialties.take(2) + musician.genres.take(2)).distinct().take(3).forEach { tag ->
                        Surface(
                            color = StageSurfaceElevated,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = StageOffWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onOpenProfile,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Portfolio", color = StageOffWhite)
                    }
                    if (!isOwnProfile) {
                        Button(
                            onClick = onBookNow,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StageAmber,
                                contentColor = StageOnAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Book", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DiscoverFiltersSheet(
    filters: DiscoverFilterState,
    onDismiss: () -> Unit,
    onUpdateFilters: ((DiscoverFilterState) -> DiscoverFilterState) -> Unit,
    onResetFilters: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Filter Musicians", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onResetFilters) {
                    Text("Reset All", color = StageAmber)
                }
            }

            Text("Genre", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeedCatalog.allGenres.forEach { genre ->
                    FilterChip(
                        selected = filters.selectedGenre == genre,
                        onClick = {
                            onUpdateFilters {
                                it.copy(selectedGenre = if (it.selectedGenre == genre) null else genre)
                            }
                        },
                        label = { Text(genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StageAmber,
                            selectedLabelColor = StageOnAmber,
                            containerColor = StageSurfaceElevated,
                            labelColor = StageOffWhite
                        )
                    )
                }
            }

            Text(
                text = "Starting Rate Range: ${formatBdtAmount(filters.minRate.toLong())} – ${formatBdtAmount(filters.maxRate.toLong())}",
                style = MaterialTheme.typography.labelLarge
            )
            RangeSlider(
                value = filters.minRate..filters.maxRate,
                onValueChange = { range ->
                    onUpdateFilters {
                        it.copy(minRate = range.start, maxRate = range.endInclusive)
                    }
                },
                valueRange = 0f..60000f,
                colors = SliderDefaults.colors(
                    thumbColor = StageAmber,
                    activeTrackColor = StageAmber,
                    inactiveTrackColor = StageOutline
                )
            )

            OutlinedTextField(
                value = filters.locationQuery,
                onValueChange = { loc -> onUpdateFilters { it.copy(locationQuery = loc) } },
                label = { Text("Dhaka Area / City") },
                placeholder = { Text("e.g. Banani, Dhanmondi, Uttara") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remote Sessions Available", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = filters.remoteOnly,
                    onCheckedChange = { v -> onUpdateFilters { it.copy(remoteOnly = v) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = StageOnAmber, checkedTrackColor = StageAmber)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Has Video Demos", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = filters.hasVideoDemosOnly,
                    onCheckedChange = { v -> onUpdateFilters { it.copy(hasVideoDemosOnly = v) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = StageOnAmber, checkedTrackColor = StageAmber)
                )
            }

            Text("Availability Status", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AvailabilityStatus.entries.forEach { avail ->
                    FilterChip(
                        selected = filters.availability == avail,
                        onClick = {
                            onUpdateFilters {
                                it.copy(availability = if (it.availability == avail) null else avail)
                            }
                        },
                        label = { Text(avail.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StageAmber,
                            selectedLabelColor = StageOnAmber,
                            containerColor = StageSurfaceElevated,
                            labelColor = StageOffWhite
                        )
                    )
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StageAmber,
                    contentColor = StageOnAmber
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold)
            }
        }
    }
}
