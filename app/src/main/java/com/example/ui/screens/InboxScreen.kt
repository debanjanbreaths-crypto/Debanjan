package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Booking
import com.example.data.model.BookingMessage
import com.example.data.model.BookingStatus
import com.example.data.model.MessageType
import com.example.data.model.MusicianProfile
import com.example.data.model.RateType
import com.example.data.model.formatBdtAmount
import com.example.ui.components.ArtistAvatar
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
import com.example.ui.theme.StageRoseContainer
import com.example.ui.theme.StageSky
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageSurfaceElevated
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.UiState

@Composable
fun InboxScreen(
    isWideScreen: Boolean,
    currentUserId: String,
    bookingsState: UiState<List<Booking>>,
    activeBookingId: String?,
    messagesState: UiState<List<BookingMessage>>,
    musiciansState: UiState<List<MusicianProfile>>,
    onSelectBooking: (String?) -> Unit,
    onSendMessage: (Booking, String) -> Unit,
    onUpdateStatus: (Booking, BookingStatus) -> Unit,
    onOpenNewOfferForArtist: (MusicianProfile) -> Unit
) {
    var showArtistPicker by remember { mutableStateOf(false) }

    val bookings = (bookingsState as? UiState.Success)?.data.orEmpty()
    val selectedBooking = bookings.firstOrNull { it.id == activeBookingId }
        ?: if (isWideScreen) bookings.firstOrNull() else null

    if (!isWideScreen && selectedBooking != null) {
        BackHandler { onSelectBooking(null) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StageCharcoal)
    ) {
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                BookingListPane(
                    bookingsState = bookingsState,
                    selectedId = selectedBooking?.id,
                    onSelect = { onSelectBooking(it.id) },
                    onNewOffer = { showArtistPicker = true },
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(StageOutline)
                )
                if (selectedBooking != null) {
                    BookingThreadPane(
                        currentUserId = currentUserId,
                        booking = selectedBooking,
                        messagesState = messagesState,
                        showBack = false,
                        onBack = {},
                        onSendMessage = { onSendMessage(selectedBooking, it) },
                        onUpdateStatus = { onUpdateStatus(selectedBooking, it) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    EmptyInboxPlaceholder(
                        onNewOffer = { showArtistPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            if (selectedBooking == null) {
                BookingListPane(
                    bookingsState = bookingsState,
                    selectedId = null,
                    onSelect = { onSelectBooking(it.id) },
                    onNewOffer = { showArtistPicker = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                BookingThreadPane(
                    currentUserId = currentUserId,
                    booking = selectedBooking,
                    messagesState = messagesState,
                    showBack = true,
                    onBack = { onSelectBooking(null) },
                    onSendMessage = { onSendMessage(selectedBooking, it) },
                    onUpdateStatus = { onUpdateStatus(selectedBooking, it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showArtistPicker) {
        val bookableArtists = (musiciansState as? UiState.Success)?.data.orEmpty()
            .filter { it.effectiveUserId != currentUserId }
        ArtistOfferPickerSheet(
            artists = bookableArtists,
            onDismiss = { showArtistPicker = false },
            onPickArtist = { artist ->
                showArtistPicker = false
                onOpenNewOfferForArtist(artist)
            }
        )
    }
}

@Composable
private fun BookingListPane(
    bookingsState: UiState<List<Booking>>,
    selectedId: String?,
    onSelect: (Booking) -> Unit,
    onNewOffer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(StageCharcoal)) {
        Surface(
            color = StageSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    JaminiRoyLotusHeaderBackdrop(modifier = Modifier.matchParentSize())
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text("Inbox & Bookings", style = MaterialTheme.typography.headlineMedium, color = StageOffWhite)
                            Text("Dhaka offers, sessions & contracts", style = MaterialTheme.typography.bodySmall, color = StageTextSecondary)
                        }
                        Button(
                            onClick = onNewOffer,
                            colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("inbox_new_offer_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Offer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                RickshawPaintRibbon(height = 4.dp)
            }
        }

        when (bookingsState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StageAmber)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(bookingsState.message, color = StageRose, modifier = Modifier.padding(24.dp))
                }
            }
            is UiState.Success -> {
                if (bookingsState.data.isEmpty()) {
                    EmptyInboxPlaceholder(onNewOffer = onNewOffer, modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bookingsState.data, key = { it.id }) { booking ->
                            val isSelected = booking.id == selectedId
                            val status = BookingStatus.fromRaw(booking.status)
                            Surface(
                                color = if (isSelected) StageSurfaceElevated else StageSurface,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isSelected) StageAmber else StageOutline,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onSelect(booking) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    ArtistAvatar(
                                        name = booking.musicianName,
                                        avatarUrl = booking.musicianAvatarUrl,
                                        size = 46.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = booking.musicianName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = StageOffWhite,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            BookingStatusPill(status)
                                        }
                                        Text(
                                            text = "${formatBdtAmount(booking.proposedRate)}${RateType.fromRaw(booking.rateType).unitSuffix} · ${booking.eventDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StageAmber
                                        )
                                        Text(
                                            text = booking.lastMessagePreview,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StageTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingThreadPane(
    currentUserId: String,
    booking: Booking,
    messagesState: UiState<List<BookingMessage>>,
    showBack: Boolean,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onUpdateStatus: (BookingStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }
    val status = BookingStatus.fromRaw(booking.status)
    val canRespondToOffer = status == BookingStatus.PENDING &&
        (booking.musicianId == currentUserId || booking.musicianId.startsWith("seed_"))

    Column(modifier = modifier.fillMaxSize().background(StageCharcoal)) {
        // Header + Offer Banner
        Surface(color = StageSurface, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to inbox", tint = StageOffWhite)
                        }
                    }
                    ArtistAvatar(
                        name = booking.musicianName,
                        avatarUrl = booking.musicianAvatarUrl,
                        size = 42.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(booking.musicianName, style = MaterialTheme.typography.titleMedium, color = StageOffWhite)
                        Text(
                            text = "${formatBdtAmount(booking.proposedRate)}${RateType.fromRaw(booking.rateType).unitSuffix} · ${booking.eventDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = StageAmber
                        )
                    }
                    BookingStatusPill(status)
                }

                // Offer Scope Summary + Accept/Decline/Complete Controls
                Surface(
                    color = StageSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Scope: ${booking.scopeNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StageOffWhite
                        )
                        if (canRespondToOffer) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onUpdateStatus(BookingStatus.ACCEPTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StageEmerald, contentColor = StageCharcoal),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("accept_booking_button")
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accept", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onUpdateStatus(BookingStatus.DECLINED) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("decline_booking_button")
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = StageRose, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Decline", color = StageRose)
                                }
                            }
                        } else if (status == BookingStatus.ACCEPTED) {
                            Button(
                                onClick = { onUpdateStatus(BookingStatus.COMPLETED) },
                                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Session Completed", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = StageOutline)

        // Messages List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (messagesState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(color = StageAmber, modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Text(
                        text = messagesState.message,
                        color = StageRose,
                        modifier = Modifier.align(Alignment.Center).padding(20.dp)
                    )
                }
                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messagesState.data, key = { it.id }) { msg ->
                            val isMine = msg.senderId == currentUserId
                            val msgType = MessageType.fromRaw(msg.messageType)
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = when {
                                    msgType == MessageType.STATUS_UPDATE -> Alignment.Center
                                    isMine -> Alignment.CenterEnd
                                    else -> Alignment.CenterStart
                                }
                            ) {
                                Surface(
                                    color = when {
                                        msgType == MessageType.STATUS_UPDATE -> StageSurfaceElevated
                                        msgType == MessageType.BOOKING_OFFER -> StageAmberContainer
                                        isMine -> StageAmber
                                        else -> StageSurface
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        if (msgType != MessageType.STATUS_UPDATE) {
                                            Text(
                                                text = msg.senderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isMine && msgType == MessageType.TEXT) StageOnAmber.copy(alpha = 0.7f) else StageAmber
                                            )
                                        }
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMine && msgType == MessageType.TEXT) StageOnAmber else StageOffWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Composer
        if (status == BookingStatus.PENDING || status == BookingStatus.ACCEPTED) {
            Surface(color = StageSurface, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text("Message ${booking.musicianName}...") },
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
                            if (draft.isNotBlank()) {
                                onSendMessage(draft.trim())
                                draft = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = StageAmber)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingStatusPill(status: BookingStatus) {
    val (bg, fg) = when (status) {
        BookingStatus.PENDING -> StageAmberContainer to StageAmber
        BookingStatus.ACCEPTED -> StageEmeraldContainer to StageEmerald
        BookingStatus.DECLINED -> StageRoseContainer to StageRose
        BookingStatus.COMPLETED -> StageSurfaceElevated to StageSky
    }
    Surface(color = bg, shape = RoundedCornerShape(999.dp)) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyInboxPlaceholder(
    onNewOffer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Forum,
                contentDescription = null,
                tint = StageAmber,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No booking threads yet",
                style = MaterialTheme.typography.titleMedium,
                color = StageOffWhite
            )
            Text(
                text = "Send a direct booking offer to any musician to negotiate dates, rates, and setlists.",
                style = MaterialTheme.typography.bodySmall,
                color = StageTextSecondary
            )
            Button(
                onClick = onNewOffer,
                colors = ButtonDefaults.buttonColors(containerColor = StageAmber, contentColor = StageOnAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send Booking Offer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistOfferPickerSheet(
    artists: List<MusicianProfile>,
    onDismiss: () -> Unit,
    onPickArtist: (MusicianProfile) -> Unit
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Choose an Artist to Book", style = MaterialTheme.typography.titleLarge)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(360.dp)
            ) {
                items(artists, key = { it.effectiveUserId }) { artist ->
                    Surface(
                        color = StageSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPickArtist(artist) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ArtistAvatar(
                                name = artist.name,
                                avatarUrl = artist.avatarUrl,
                                size = 42.dp,
                                verified = artist.verified
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(artist.name, style = MaterialTheme.typography.titleSmall, color = StageOffWhite)
                                Text(
                                    text = "${artist.specialties.firstOrNull() ?: "Musician"} · ${artist.primaryStartingRateLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StageAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
