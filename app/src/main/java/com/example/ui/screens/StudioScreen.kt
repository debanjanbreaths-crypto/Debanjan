package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.model.AvailabilityStatus
import com.example.data.model.MusicianProfile
import com.example.data.model.PortfolioItem
import com.example.data.model.SocialConnection
import com.example.data.model.SocialProvider
import com.example.data.model.SocialUrlValidator
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.seed.SeedCatalog
import com.example.ui.auth.signOutUser
import com.example.ui.components.ArtistAvatar
import com.example.ui.components.InstagramBadgePill
import com.example.ui.components.JaminiRoyLotusHeaderBackdrop
import com.example.ui.components.RickshawPaintRibbon
import com.example.ui.theme.InstagramGradientBrush
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageEmerald
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageOnAmber
import com.example.ui.theme.StageOutline
import com.example.ui.theme.StageRose
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    currentUserId: String,
    userAccountState: UiState<UserAccount?>,
    socialConnectionsState: UiState<List<SocialConnection>>,
    musiciansState: UiState<List<MusicianProfile>>,
    onSaveAccount: (role: UserRole, name: String, handle: String, location: String, bio: String) -> Unit,
    onSaveStudioDetails: (
        specialties: List<String>,
        genres: List<String>,
        hourlyRate: Long,
        perGigRate: Long,
        perTrackRate: Long,
        availability: AvailabilityStatus,
        remoteAvailable: Boolean,
        gearList: String
    ) -> Unit,
    onConnectInstagram: (handle: String, followers: Long, sandboxMode: Boolean) -> Unit,
    onDisconnectInstagram: () -> Unit,
    onImportInstagramReels: (List<PortfolioItem>, alsoPostToFeed: Boolean) -> Unit,
    onAddSocialLink: (url: String, title: String, genre: String) -> Unit,
    onAddNativeVideo: (title: String, uri: String, genre: String) -> Unit,
    onOpenPublicProfile: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val account = (userAccountState as? UiState.Success)?.data
    val myProfile = (musiciansState as? UiState.Success)?.data
        ?.firstOrNull { it.effectiveUserId == currentUserId }
    val igConnection = (socialConnectionsState as? UiState.Success)?.data
        ?.firstOrNull { SocialProvider.fromRaw(it.provider) == SocialProvider.INSTAGRAM && it.connected }

    var name by remember(account?.name) { mutableStateOf(account?.name ?: "Dhaka Artist") }
    var handle by remember(account?.handle) { mutableStateOf(account?.handle ?: "@artist") }
    var location by remember(account?.location) { mutableStateOf(account?.location ?: "Dhaka, Bangladesh") }
    var bio by remember(account?.bio) { mutableStateOf(account?.bio ?: "") }
    var role by remember(account?.role) { mutableStateOf(UserRole.fromRaw(account?.role)) }

    var selectedSpecs by remember(myProfile?.specialties) {
        mutableStateOf((myProfile?.specialties ?: listOf("Session Guitarist")).toSet())
    }
    var selectedGenres by remember(myProfile?.genres) {
        mutableStateOf((myProfile?.genres ?: listOf("Bangla Rock", "Pop")).toSet())
    }
    var hourlyRate by remember(myProfile?.hourlyRate) { mutableStateOf((myProfile?.hourlyRate ?: 3500L).toString()) }
    var perGigRate by remember(myProfile?.perGigRate) { mutableStateOf((myProfile?.perGigRate ?: 18000L).toString()) }
    var perTrackRate by remember(myProfile?.perTrackRate) { mutableStateOf((myProfile?.perTrackRate ?: 10000L).toString()) }
    var availability by remember(myProfile?.availabilityStatus) {
        mutableStateOf(AvailabilityStatus.fromRaw(myProfile?.availabilityStatus))
    }
    var remoteAvailable by remember(myProfile?.remoteAvailable) {
        mutableStateOf(myProfile?.remoteAvailable ?: true)
    }
    var gearList by remember(myProfile?.gearList) { mutableStateOf(myProfile?.gearList ?: "") }

    // Social URL importer
    var pasteSocialUrl by remember { mutableStateOf("") }
    var pasteTitle by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }

    var showInstagramConnectSheet by remember { mutableStateOf(false) }
    var showImportReelsSheet by remember { mutableStateOf(false) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onAddNativeVideo("Native Performance Clip", uri.toString(), selectedGenres.firstOrNull() ?: "Live")
        }
    }

    LaunchedEffect(myProfile?.userId) {
        if (myProfile != null) {
            if (selectedSpecs.isEmpty()) selectedSpecs = myProfile.specialties.toSet()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        // Profile Header Card
        item {
            Surface(
                color = StageSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .border(1.5.dp, StageOutline, RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    JaminiRoyLotusHeaderBackdrop(modifier = Modifier.matchParentSize())
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RickshawPaintRibbon(height = 5.dp)
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ArtistAvatar(
                            name = name,
                            avatarUrl = account?.avatarUrl ?: "",
                            size = 58.dp,
                            verified = account?.verified ?: false
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleLarge, color = StageOffWhite)
                            Text("$handle · ${role.label}", style = MaterialTheme.typography.labelSmall, color = StageAmber)
                        }
                        OutlinedButton(onClick = { onOpenPublicProfile(currentUserId) }) {
                            Text("Preview", color = StageAmber)
                        }
                    }

                    // Role Switcher
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        UserRole.entries.forEach { r ->
                            FilterChip(
                                selected = role == r,
                                onClick = { role = r },
                                label = { Text(r.label) },
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
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = handle,
                            onValueChange = { handle = it },
                            label = { Text("@Handle") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("City") },
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        minLines = 2,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveAccount(role, name, handle, location, bio) },
                        colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Identity & Role", fontWeight = FontWeight.Bold)
                    }
                        }
                    }
                }
            }
        }

        // Instagram OAuth / Sandbox Connection & Reel Importer
        item {
            Surface(
                color = StageSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .border(1.dp, StageOutline, RoundedCornerShape(20.dp))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Instagram & Video Engine", style = MaterialTheme.typography.titleMedium, color = StageOffWhite)
                            Text(
                                text = "Sync Reels via Instagram OAuth (Sandbox ready) or paste public Reel/YouTube links",
                                style = MaterialTheme.typography.bodySmall,
                                color = StageTextSecondary
                            )
                        }
                    }

                    if (igConnection != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            InstagramBadgePill(
                                handle = igConnection.externalHandle,
                                followerCount = igConnection.followerCount
                            )
                            TextButton(onClick = onDisconnectInstagram) {
                                Text("Disconnect", color = StageRose)
                            }
                        }
                        Button(
                            onClick = { showImportReelsSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_instagram_reels_button")
                        ) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("One-Click Import Instagram Reels", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(InstagramGradientBrush)
                                .clickable { showInstagramConnectSheet = true }
                                .padding(vertical = 14.dp)
                                .testTag("connect_instagram_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White)
                                Text(
                                    text = "Connect Instagram",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Paste Instagram Reel / Post / YouTube URL
                    OutlinedTextField(
                        value = pasteSocialUrl,
                        onValueChange = {
                            pasteSocialUrl = it
                            urlError = null
                        },
                        label = { Text("Paste Instagram Reel/Post or YouTube URL") },
                        leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, tint = StageAmber) },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("social_url_input")
                    )
                    OutlinedTextField(
                        value = pasteTitle,
                        onValueChange = { pasteTitle = it },
                        label = { Text("Clip Title (optional)") },
                        singleLine = true,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (urlError != null) {
                        Text(urlError!!, color = StageRose, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val parsed = SocialUrlValidator.parseAndValidate(pasteSocialUrl)
                                if (parsed == null) {
                                    urlError = "Invalid link. Must be a valid Instagram Reel/Post, YouTube, or MP4/MP3 URL."
                                } else {
                                    onAddSocialLink(
                                        pasteSocialUrl.trim(),
                                        pasteTitle.trim(),
                                        selectedGenres.firstOrNull() ?: "Live"
                                    )
                                    pasteSocialUrl = ""
                                    pasteTitle = ""
                                    urlError = null
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("save_social_url_button")
                        ) {
                            Text("Add URL Embed", color = StageAmber)
                        }
                        OutlinedButton(
                            onClick = {
                                videoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = StageOffWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Clip", color = StageOffWhite)
                        }
                    }
                }
            }
        }

        // Musician Rate Card, Specialties & Availability
        item {
            Surface(
                color = StageSurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .border(1.dp, StageOutline, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Rate Card (৳ BDT) & Specialties", style = MaterialTheme.typography.titleMedium, color = StageOffWhite)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourlyRate,
                            onValueChange = { hourlyRate = it.filter { c -> c.isDigit() } },
                            label = { Text("Hourly ৳") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = perGigRate,
                            onValueChange = { perGigRate = it.filter { c -> c.isDigit() } },
                            label = { Text("Per Gig ৳") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = perTrackRate,
                            onValueChange = { perTrackRate = it.filter { c -> c.isDigit() } },
                            label = { Text("Per Track ৳") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = studioTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Availability", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AvailabilityStatus.entries.forEach { st ->
                            FilterChip(
                                selected = availability == st,
                                onClick = { availability = st },
                                label = { Text(st.label) },
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
                        Text("Open for Remote Session Work", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = remoteAvailable,
                            onCheckedChange = { remoteAvailable = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StageOnAmber, checkedTrackColor = StageAmber)
                        )
                    }

                    Text("Specialties", style = MaterialTheme.typography.labelLarge)
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

                    Text("Genres", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SeedCatalog.allGenres.forEach { g ->
                            FilterChip(
                                selected = g in selectedGenres,
                                onClick = {
                                    selectedGenres = if (g in selectedGenres && selectedGenres.size > 1) {
                                        selectedGenres - g
                                    } else {
                                        selectedGenres + g
                                    }
                                },
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

                    OutlinedTextField(
                        value = gearList,
                        onValueChange = { gearList = it },
                        label = { Text("Gear & Studio Rig") },
                        minLines = 2,
                        colors = studioTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveStudioDetails(
                                selectedSpecs.toList(),
                                selectedGenres.toList(),
                                hourlyRate.toLongOrNull() ?: 3500L,
                                perGigRate.toLongOrNull() ?: 18000L,
                                perTrackRate.toLongOrNull() ?: 10000L,
                                availability,
                                remoteAvailable,
                                gearList
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Rates & Studio Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sign Out
        item {
            OutlinedButton(
                onClick = { signOutUser(credentialManager, scope) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .height(50.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = StageRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = StageRose, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showInstagramConnectSheet) {
        InstagramConnectSheet(
            defaultHandle = handle,
            onDismiss = { showInstagramConnectSheet = false },
            onConnect = { igHandle, followers ->
                onConnectInstagram(igHandle, followers, true)
                showInstagramConnectSheet = false
            }
        )
    }

    if (showImportReelsSheet) {
        ImportInstagramReelsSheet(
            onDismiss = { showImportReelsSheet = false },
            onImport = { reels, postToFeed ->
                onImportInstagramReels(reels, postToFeed)
                showImportReelsSheet = false
            }
        )
    }
}

@Composable
private fun studioTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = StageAmber,
    unfocusedBorderColor = StageOutline,
    focusedTextColor = StageOffWhite,
    unfocusedTextColor = StageOffWhite
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstagramConnectSheet(
    defaultHandle: String,
    onDismiss: () -> Unit,
    onConnect: (handle: String, followers: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var igHandle by remember { mutableStateOf(defaultHandle) }
    var followers by remember { mutableStateOf("14800") }

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
            Text("Connect Instagram (Graph API / Sandbox)", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Instagram App ID is not configured in BuildConfig — Sandbox OAuth mode connects your verified creator badge and enables 1-click Reel import.",
                style = MaterialTheme.typography.bodySmall,
                color = StageTextSecondary
            )
            OutlinedTextField(
                value = igHandle,
                onValueChange = { igHandle = it },
                label = { Text("Instagram @handle") },
                singleLine = true,
                colors = studioTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = followers,
                onValueChange = { followers = it.filter { c -> c.isDigit() } },
                label = { Text("Synced Follower Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = studioTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (igHandle.isNotBlank()) {
                        onConnect(igHandle.trim(), followers.toLongOrNull() ?: 0L)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Authorize & Sync Instagram", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportInstagramReelsSheet(
    onDismiss: () -> Unit,
    onImport: (List<PortfolioItem>, alsoPostToFeed: Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val available = SeedCatalog.sandboxInstagramImportReels
    var selectedIds by remember { mutableStateOf(available.map { it.id }.toSet()) }
    var alsoPostToFeed by remember { mutableStateOf(true) }

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
            Text("Import Reels from Instagram", style = MaterialTheme.typography.titleLarge)
            available.forEach { reel ->
                val checked = reel.id in selectedIds
                Surface(
                    color = StageSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedIds = if (checked) selectedIds - reel.id else selectedIds + reel.id
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = {
                                selectedIds = if (checked) selectedIds - reel.id else selectedIds + reel.id
                            },
                            colors = CheckboxDefaults.colors(checkedColor = StageAmber, checkmarkColor = StageOnAmber)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reel.title, style = MaterialTheme.typography.titleSmall, color = StageOffWhite)
                            Text("${reel.duration} · ${reel.genreTag}", style = MaterialTheme.typography.labelSmall, color = StageEmerald)
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Also share imported Reels to ক্ষ্যাপ", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = alsoPostToFeed,
                    onCheckedChange = { alsoPostToFeed = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = StageOnAmber, checkedTrackColor = StageAmber)
                )
            }

            Button(
                onClick = {
                    val chosen = available.filter { it.id in selectedIds }
                    if (chosen.isNotEmpty()) onImport(chosen, alsoPostToFeed)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Import ${selectedIds.size} Reel(s)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
