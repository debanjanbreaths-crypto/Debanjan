package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.GigApplication
import com.example.data.model.GigListing
import com.example.data.model.formatBdtAmount
import com.example.data.seed.SeedCatalog
import com.example.ui.components.DhakaHeritagePill
import com.example.ui.components.JaminiRoyLotusHeaderBackdrop
import com.example.ui.components.RickshawPaintRibbon
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageAmberContainer
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageEmerald
import com.example.ui.theme.StageEmeraldContainer
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageOnAmber
import com.example.ui.theme.StageOutline
import com.example.ui.theme.StageRose
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.UiState

@Composable
fun GigsScreen(
    currentUserId: String,
    gigsState: UiState<List<GigListing>>,
    myApplications: List<GigApplication>,
    onOneClickApply: (GigListing) -> Unit,
    onCreateGig: (
        title: String,
        description: String,
        specialties: List<String>,
        genre: String,
        location: String,
        remote: Boolean,
        eventDate: String,
        budgetMin: Long,
        budgetMax: Long
    ) -> Unit
) {
    var remoteFilterOnly by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var showCreateGigSheet by remember { mutableStateOf(false) }

    val appliedGigIds = remember(myApplications) {
        myApplications.map { it.gigId }.toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                                    text = "Gig Board",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = StageOffWhite
                                )
                                DhakaHeritagePill()
                            }
                            Text(
                                text = "Dhaka live shows & studio tracking in ৳ BDT — 1-click apply with your top reel",
                                style = MaterialTheme.typography.bodySmall,
                                color = StageTextSecondary
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = remoteFilterOnly,
                                        onClick = { remoteFilterOnly = !remoteFilterOnly },
                                        label = { Text("Remote Only") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = StageAmber,
                                            selectedLabelColor = StageOnAmber,
                                            containerColor = StageSurfaceElevated,
                                            labelColor = StageOffWhite
                                        )
                                    )
                                }
                                items(SeedCatalog.allGenres) { genre ->
                                    FilterChip(
                                        selected = selectedGenre == genre,
                                        onClick = {
                                            selectedGenre = if (selectedGenre == genre) null else genre
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
                        }
                    }
                    RickshawPaintRibbon(height = 4.dp)
                }
            }

            when (gigsState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StageAmber)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(gigsState.message, color = StageRose, modifier = Modifier.padding(24.dp))
                    }
                }
                is UiState.Success -> {
                    val filtered = gigsState.data.filter { gig ->
                        (!remoteFilterOnly || gig.remote) &&
                            (selectedGenre == null || gig.genre.equals(selectedGenre, ignoreCase = true))
                    }
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No matching open gigs.", color = StageTextSecondary)
                        }
                    } else {
                        LazyColumn(
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
                            items(filtered, key = { it.id }) { gig ->
                                GigCard(
                                    gig = gig,
                                    isOwnGig = gig.clientId == currentUserId,
                                    hasApplied = gig.id in appliedGigIds,
                                    onApply = { onOneClickApply(gig) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateGigSheet = true },
            containerColor = StageAmber,
            contentColor = StageOnAmber,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
                .testTag("post_gig_fab")
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Post open gig")
        }
    }

    if (showCreateGigSheet) {
        CreateGigSheet(
            onDismiss = { showCreateGigSheet = false },
            onSubmit = { title, desc, specs, genre, loc, remote, date, minB, maxB ->
                onCreateGig(title, desc, specs, genre, loc, remote, date, minB, maxB)
                showCreateGigSheet = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GigCard(
    gig: GigListing,
    isOwnGig: Boolean,
    hasApplied: Boolean,
    onApply: () -> Unit
) {
    Surface(
        color = StageSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
            .border(1.5.dp, StageOutline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gig.clientName,
                        style = MaterialTheme.typography.labelMedium,
                        color = StageAmber
                    )
                    Text(
                        text = gig.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = StageOffWhite
                    )
                }
                Surface(
                    color = StageAmberContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "${formatBdtAmount(gig.budgetMin)} – ${formatBdtAmount(gig.budgetMax)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = StageAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Text(
                text = gig.description,
                style = MaterialTheme.typography.bodyMedium,
                color = StageTextSecondary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = StageAmber, modifier = Modifier.size(15.dp))
                    Text(
                        text = if (gig.remote) "Remote Session" else gig.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = StageOffWhite
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = StageAmber, modifier = Modifier.size(15.dp))
                    Text(
                        text = gig.eventDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = StageOffWhite
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                gig.requiredSpecialties.forEach { spec ->
                    Surface(color = StageSurfaceElevated, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = spec,
                            style = MaterialTheme.typography.labelSmall,
                            color = StageOffWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Surface(color = StageSurfaceElevated, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = gig.genre,
                        style = MaterialTheme.typography.labelSmall,
                        color = StageAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            when {
                isOwnGig -> {
                    Surface(color = StageSurfaceElevated, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Your Posted Gig · Open",
                            style = MaterialTheme.typography.labelMedium,
                            color = StageAmber,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                hasApplied -> {
                    Surface(
                        color = StageEmeraldContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StageEmerald)
                            Text(
                                text = "Applied · Top Video Reel & Rate Card Attached",
                                style = MaterialTheme.typography.labelMedium,
                                color = StageEmerald
                            )
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onApply,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StageAmber,
                            contentColor = StageOnAmber
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_gig_${gig.id}")
                    ) {
                        Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("1-Click Apply (Attach Reel & Rates)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateGigSheet(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        specialties: List<String>,
        genre: String,
        location: String,
        remote: Boolean,
        eventDate: String,
        budgetMin: Long,
        budgetMax: Long
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSpecs by remember { mutableStateOf(setOf("Session Guitarist")) }
    var selectedGenre by remember { mutableStateOf("Bangla Rock") }
    var location by remember { mutableStateOf("Banani, Dhaka") }
    var remote by remember { mutableStateOf(false) }
    var eventDate by remember { mutableStateOf("2026-12-22") }
    var budgetMin by remember { mutableStateOf("15000") }
    var budgetMax by remember { mutableStateOf("35000") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RickshawPaintRibbon(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
                height = 5.dp
            )
            Text("Post Dhaka Gig / Session (BDT)", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Gig Title") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Scope, Set Length & Deliverables") },
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StageAmber,
                    unfocusedBorderColor = StageOutline,
                    focusedTextColor = StageOffWhite,
                    unfocusedTextColor = StageOffWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Required Specialties", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SeedCatalog.allSpecialties.forEach { spec ->
                    FilterChip(
                        selected = spec in selectedSpecs,
                        onClick = {
                            selectedSpecs = if (spec in selectedSpecs && selectedSpecs.size > 1) {
                                selectedSpecs - spec
                            } else {
                                selectedSpecs + spec
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

            Text("Primary Genre", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SeedCatalog.allGenres.forEach { g ->
                    FilterChip(
                        selected = selectedGenre == g,
                        onClick = { selectedGenre = g },
                        label = { Text(g) },
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remote Studio Session", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = remote,
                    onCheckedChange = { remote = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = StageOnAmber, checkedTrackColor = StageAmber)
                )
            }

            if (!remote) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("City / Venue") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StageAmber,
                        unfocusedBorderColor = StageOutline,
                        focusedTextColor = StageOffWhite,
                        unfocusedTextColor = StageOffWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = budgetMin,
                    onValueChange = { budgetMin = it.filter { c -> c.isDigit() } },
                    label = { Text("Min ৳") },
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
                    value = budgetMax,
                    onValueChange = { budgetMax = it.filter { c -> c.isDigit() } },
                    label = { Text("Max ৳") },
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
                    label = { Text("Date") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StageAmber,
                        unfocusedBorderColor = StageOutline,
                        focusedTextColor = StageOffWhite,
                        unfocusedTextColor = StageOffWhite
                    ),
                    modifier = Modifier.weight(1.2f)
                )
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = StageRose, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val minB = budgetMin.toLongOrNull() ?: 0L
                    val maxB = budgetMax.toLongOrNull() ?: 0L
                    when {
                        title.trim().length < 3 -> errorMsg = "Enter a gig title (min 3 chars)."
                        description.isBlank() -> errorMsg = "Enter a description."
                        maxB < minB -> errorMsg = "Max budget must be >= min budget."
                        else -> onSubmit(
                            title.trim(),
                            description.trim(),
                            selectedSpecs.toList(),
                            selectedGenre,
                            location,
                            remote,
                            eventDate,
                            minB,
                            maxB
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Publish Gig", fontWeight = FontWeight.Bold)
            }
        }
    }
}
