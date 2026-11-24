package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.SoundStageRepository
import com.example.data.firebase.unwrapFirestoreMessage
import com.example.data.model.AvailabilityStatus
import com.example.data.model.Booking
import com.example.data.model.BookingMessage
import com.example.data.model.BookingStatus
import com.example.data.model.FeedPost
import com.example.data.model.GigApplication
import com.example.data.model.GigListing
import com.example.data.model.MusicianProfile
import com.example.data.model.MusicianReview
import com.example.data.model.PortfolioItem
import com.example.data.model.PortfolioMediaType
import com.example.data.model.PostComment
import com.example.data.model.PostMediaType
import com.example.data.model.PostType
import com.example.data.model.RateType
import com.example.data.model.SocialConnection
import com.example.data.model.SocialUrlValidator
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.seed.SeedCatalog
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class DiscoverFilterState(
    val searchQuery: String = "",
    val selectedSpecialty: String? = null,
    val selectedGenre: String? = null,
    val minRate: Float = 0f,
    val maxRate: Float = 60000f,
    val locationQuery: String = "",
    val remoteOnly: Boolean = false,
    val hasVideoDemosOnly: Boolean = false,
    val availability: AvailabilityStatus? = null
)

class SoundStageViewModel(
    private val repository: SoundStageRepository,
    val currentUserId: String
) : ViewModel() {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _filters = MutableStateFlow(DiscoverFilterState())
    val filters: StateFlow<DiscoverFilterState> = _filters.asStateFlow()

    private val _activeBookingId = MutableStateFlow<String?>(null)
    val activeBookingId: StateFlow<String?> = _activeBookingId.asStateFlow()

    private val _activeCommentsPostId = MutableStateFlow<String?>(null)
    private val _activeProfileId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null && currentUser.uid == currentUserId) {
                repository.ensureUserInitialized(currentUser)
            }
        }
    }

    private fun <T> Flow<T>.toUiStateFlow(
        tag: String,
        fallbackError: String
    ): StateFlow<UiState<T>> = map<T, UiState<T>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(tag, fallbackError, root)
            emit(UiState.Error(root.unwrapFirestoreMessage(fallbackError)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val userAccount: StateFlow<UiState<UserAccount?>> =
        repository.observeUserAccount(currentUserId)
            .toUiStateFlow("SoundStageVM", "Failed to load user account")

    val socialConnections: StateFlow<UiState<List<SocialConnection>>> =
        repository.observeSocialConnections(currentUserId)
            .toUiStateFlow("SoundStageVM", "Failed to load social connections")

    val allMusicians: StateFlow<UiState<List<MusicianProfile>>> =
        repository.observeMusicians()
            .toUiStateFlow("SoundStageVM", "Failed to load musicians")

    val filteredMusicians: StateFlow<UiState<List<MusicianProfile>>> =
        combine(allMusicians, _filters) { state, filter ->
            when (state) {
                is UiState.Loading -> UiState.Loading
                is UiState.Error -> state
                is UiState.Success -> {
                    val q = filter.searchQuery.trim().lowercase()
                    val locQ = filter.locationQuery.trim().lowercase()
                    val filtered = state.data.filter { m ->
                        val matchesSearch = q.isEmpty() ||
                            m.name.lowercase().contains(q) ||
                            m.handle.lowercase().contains(q) ||
                            m.bio.lowercase().contains(q) ||
                            m.specialties.any { it.lowercase().contains(q) } ||
                            m.genres.any { it.lowercase().contains(q) }
                        val matchesSpecialty = filter.selectedSpecialty == null ||
                            m.specialties.any { it.equals(filter.selectedSpecialty, ignoreCase = true) }
                        val matchesGenre = filter.selectedGenre == null ||
                            m.genres.any { it.equals(filter.selectedGenre, ignoreCase = true) }
                        val startingRate = when {
                            m.hourlyRate > 0 -> m.hourlyRate.toFloat()
                            m.perTrackRate > 0 -> m.perTrackRate.toFloat()
                            m.perGigRate > 0 -> m.perGigRate.toFloat()
                            else -> 0f
                        }
                        val matchesRate = startingRate >= filter.minRate && startingRate <= filter.maxRate
                        val matchesLocation = locQ.isEmpty() || m.location.lowercase().contains(locQ)
                        val matchesRemote = !filter.remoteOnly || m.remoteAvailable
                        val matchesVideos = !filter.hasVideoDemosOnly || m.hasVideoDemos
                        val matchesAvailability = filter.availability == null ||
                            AvailabilityStatus.fromRaw(m.availabilityStatus) == filter.availability

                        matchesSearch && matchesSpecialty && matchesGenre && matchesRate &&
                            matchesLocation && matchesRemote && matchesVideos && matchesAvailability
                    }
                    UiState.Success(filtered)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val portfolioItems: StateFlow<UiState<List<PortfolioItem>>> =
        repository.observePortfolioItems()
            .toUiStateFlow("SoundStageVM", "Failed to load portfolio items")

    val posts: StateFlow<UiState<List<FeedPost>>> =
        repository.observePosts()
            .toUiStateFlow("SoundStageVM", "Failed to load Stage feed")

    val likedPostIds: StateFlow<Set<String>> =
        repository.observeLikedPostIds(currentUserId)
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptySet())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptySet()
            )

    val gigs: StateFlow<UiState<List<GigListing>>> =
        repository.observeGigs()
            .toUiStateFlow("SoundStageVM", "Failed to load gigs")

    val myApplications: StateFlow<List<GigApplication>> =
        repository.observeMyGigApplications()
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    val bookings: StateFlow<UiState<List<Booking>>> =
        repository.observeBookings()
            .toUiStateFlow("SoundStageVM", "Failed to load bookings")

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeBookingMessages: StateFlow<UiState<List<BookingMessage>>> =
        _activeBookingId.flatMapLatest { bookingId ->
            if (bookingId.isNullOrBlank()) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeBookingMessages(bookingId)
                    .map<List<BookingMessage>, UiState<List<BookingMessage>>> { UiState.Success(it) }
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        emit(UiState.Error((firestoreError ?: error).unwrapFirestoreMessage("Failed to load messages")))
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(emptyList())
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePostComments: StateFlow<UiState<List<PostComment>>> =
        _activeCommentsPostId.flatMapLatest { postId ->
            if (postId.isNullOrBlank()) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observePostComments(postId)
                    .map<List<PostComment>, UiState<List<PostComment>>> { UiState.Success(it) }
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        emit(UiState.Error((firestoreError ?: error).unwrapFirestoreMessage("Failed to load comments")))
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Success(emptyList())
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMusicianReviews: StateFlow<List<MusicianReview>> =
        _activeProfileId.flatMapLatest { musicianId ->
            if (musicianId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeReviewsForMusician(musicianId)
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        emit(SeedCatalog.reviews.filter { it.musicianId == musicianId })
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    // --- FILTER UPDATES ---

    fun updateFilters(transform: (DiscoverFilterState) -> DiscoverFilterState) {
        _filters.update(transform)
    }

    fun resetFilters() {
        _filters.value = DiscoverFilterState()
    }

    fun selectBookingThread(bookingId: String?) {
        _activeBookingId.value = bookingId
    }

    fun selectCommentsPost(postId: String?) {
        _activeCommentsPostId.value = postId
    }

    fun selectMusicianProfile(musicianId: String) {
        _activeProfileId.value = musicianId
    }

    // --- ACTIONS ---

    private fun currentActorIdentity(): Pair<String, String> {
        val acct = (userAccount.value as? UiState.Success)?.data
        val name = acct?.name?.ifBlank { null }
            ?: Firebase.auth.currentUser?.displayName?.ifBlank { null }
            ?: "ক্ষ্যাপ User"
        val handle = acct?.handle?.ifBlank { null } ?: "@artist"
        return name to handle
    }

    fun toggleLikePost(post: FeedPost) {
        val liked = post.id in likedPostIds.value
        viewModelScope.launch {
            repository.toggleLikePost(post, liked).onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Could not update like"))
            }
        }
    }

    fun addPostComment(post: FeedPost, text: String) {
        val (name, handle) = currentActorIdentity()
        viewModelScope.launch {
            repository.addPostComment(post, name, handle, text).onSuccess {
                _events.tryEmit("Comment posted")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Could not post comment"))
            }
        }
    }

    fun createPost(
        content: String,
        postType: PostType,
        rawMediaUrl: String,
        pickedMediaUri: String?
    ) {
        val (name, handle) = currentActorIdentity()
        val acct = (userAccount.value as? UiState.Success)?.data
        val effectiveUrl = pickedMediaUri?.takeIf { it.isNotBlank() } ?: rawMediaUrl.trim()

        var mediaType = PostMediaType.NONE
        var resolvedMediaUrl = ""
        var resolvedThumb = ""

        if (effectiveUrl.isNotEmpty()) {
            if (pickedMediaUri != null && pickedMediaUri == effectiveUrl) {
                mediaType = PostMediaType.VIDEO
                resolvedMediaUrl = effectiveUrl
                resolvedThumb = "res://img_stage_hero"
            } else {
                val parsed = SocialUrlValidator.parseAndValidate(effectiveUrl)
                if (parsed == null) {
                    _events.tryEmit("Invalid media link. Paste a valid Instagram Reel, YouTube, or MP4/MP3 URL.")
                    return
                }
                mediaType = when (parsed.mediaType) {
                    PortfolioMediaType.INSTAGRAM_REEL -> PostMediaType.REEL_EMBED
                    PortfolioMediaType.YOUTUBE_VIDEO -> PostMediaType.VIDEO
                    PortfolioMediaType.NATIVE_VIDEO -> PostMediaType.VIDEO
                    PortfolioMediaType.AUDIO -> PostMediaType.AUDIO
                }
                resolvedMediaUrl = parsed.playableStreamUrl
                resolvedThumb = parsed.thumbnailUrl
            }
        }

        viewModelScope.launch {
            repository.createPost(
                authorName = name,
                authorHandle = handle,
                authorAvatarUrl = acct?.avatarUrl ?: "",
                authorVerified = acct?.verified ?: false,
                content = content,
                mediaType = mediaType,
                mediaUrl = resolvedMediaUrl,
                thumbnailUrl = resolvedThumb,
                postType = postType
            ).onSuccess {
                _events.tryEmit("Posted to ক্ষ্যাপ")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to publish post"))
            }
        }
    }

    fun updateRoleAndProfile(
        role: UserRole,
        name: String,
        handle: String,
        location: String,
        bio: String
    ) {
        viewModelScope.launch {
            repository.updateUserRoleAndBio(role, name, handle, location, bio)
                .onSuccess { _events.tryEmit("Profile saved") }
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to save profile")) }
        }
    }

    fun updateMusicianStudioDetails(
        specialties: List<String>,
        genres: List<String>,
        hourlyRate: Long,
        perGigRate: Long,
        perTrackRate: Long,
        availabilityStatus: AvailabilityStatus,
        remoteAvailable: Boolean,
        gearList: String
    ) {
        viewModelScope.launch {
            repository.updateMusicianStudioDetails(
                specialties = specialties,
                genres = genres,
                hourlyRate = hourlyRate,
                perGigRate = perGigRate,
                perTrackRate = perTrackRate,
                availabilityStatus = availabilityStatus,
                remoteAvailable = remoteAvailable,
                gearList = gearList
            ).onSuccess {
                _events.tryEmit("Rates & studio details updated")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to update studio details"))
            }
        }
    }

    fun connectInstagram(handle: String, followerCount: Long, sandboxMode: Boolean = true) {
        viewModelScope.launch {
            repository.connectInstagramAccount(handle, followerCount, sandboxMode)
                .onSuccess { _events.tryEmit("Instagram connected: $handle") }
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to connect Instagram")) }
        }
    }

    fun disconnectInstagram() {
        viewModelScope.launch {
            repository.disconnectInstagramAccount()
                .onSuccess { _events.tryEmit("Instagram disconnected") }
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to disconnect Instagram")) }
        }
    }

    fun importInstagramReels(reels: List<PortfolioItem>, alsoPostToFeed: Boolean) {
        val (name, handle) = currentActorIdentity()
        val acct = (userAccount.value as? UiState.Success)?.data
        viewModelScope.launch {
            var imported = 0
            reels.forEach { reel ->
                val res = repository.addPortfolioItem(
                    musicianName = name,
                    musicianHandle = handle,
                    title = reel.title,
                    mediaType = PortfolioMediaType.INSTAGRAM_REEL,
                    sourceUrl = reel.sourceUrl,
                    thumbnailUrl = reel.thumbnailUrl,
                    aspectRatio = reel.aspectRatio,
                    genreTag = reel.genreTag,
                    duration = reel.duration
                )
                if (res.isSuccess) {
                    imported++
                    if (alsoPostToFeed) {
                        repository.createPost(
                            authorName = name,
                            authorHandle = handle,
                            authorAvatarUrl = acct?.avatarUrl ?: "",
                            authorVerified = acct?.verified ?: false,
                            content = "Imported from Instagram: ${reel.title}",
                            mediaType = PostMediaType.REEL_EMBED,
                            mediaUrl = reel.sourceUrl,
                            thumbnailUrl = reel.thumbnailUrl,
                            postType = PostType.JAM_CLIP
                        )
                    }
                }
            }
            if (imported > 0) {
                _events.tryEmit("Imported $imported Instagram Reel(s) to your portfolio")
            } else {
                _events.tryEmit("Unable to import Instagram Reels")
            }
        }
    }

    fun addSocialLinkToPortfolio(rawUrl: String, customTitle: String, genreTag: String) {
        val parsed = SocialUrlValidator.parseAndValidate(rawUrl)
        if (parsed == null) {
            _events.tryEmit("Invalid URL. Enter a valid Instagram Reel/Post or YouTube video URL.")
            return
        }
        val (name, handle) = currentActorIdentity()
        val finalTitle = customTitle.trim().ifEmpty { parsed.suggestedTitle }
        viewModelScope.launch {
            repository.addPortfolioItem(
                musicianName = name,
                musicianHandle = handle,
                title = finalTitle,
                mediaType = parsed.mediaType,
                sourceUrl = parsed.playableStreamUrl,
                thumbnailUrl = parsed.thumbnailUrl,
                aspectRatio = parsed.aspectRatio,
                genreTag = genreTag.ifBlank { "Live" },
                duration = "0:32"
            ).onSuccess {
                _events.tryEmit("Added ${parsed.mediaType.label} to portfolio")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to save portfolio item"))
            }
        }
    }

    fun addNativePickedMedia(title: String, uriString: String, isAudio: Boolean, genreTag: String) {
        val (name, handle) = currentActorIdentity()
        viewModelScope.launch {
            repository.addPortfolioItem(
                musicianName = name,
                musicianHandle = handle,
                title = title.ifBlank { if (isAudio) "Studio Audio Demo" else "Performance Clip" },
                mediaType = if (isAudio) PortfolioMediaType.AUDIO else PortfolioMediaType.NATIVE_VIDEO,
                sourceUrl = uriString,
                thumbnailUrl = if (isAudio) "res://img_vocal_booth" else "res://img_stage_hero",
                aspectRatio = if (isAudio) "1:1" else "9:16",
                genreTag = genreTag.ifBlank { "Studio" },
                duration = "0:35"
            ).onSuccess {
                _events.tryEmit("Media added to your portfolio")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to add media"))
            }
        }
    }

    fun createGig(
        title: String,
        description: String,
        specialties: List<String>,
        genre: String,
        location: String,
        remote: Boolean,
        eventDate: String,
        budgetMin: Long,
        budgetMax: Long
    ) {
        val (name, _) = currentActorIdentity()
        viewModelScope.launch {
            repository.createGig(
                clientName = name,
                title = title,
                description = description,
                requiredSpecialties = specialties,
                genre = genre,
                location = location,
                remote = remote,
                eventDate = eventDate,
                budgetMin = budgetMin,
                budgetMax = budgetMax
            ).onSuccess {
                _events.tryEmit("Gig posted to the board")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to post gig"))
            }
        }
    }

    fun applyToGigOneClick(gig: GigListing, customNote: String = "") {
        val (name, handle) = currentActorIdentity()
        val myProfile = (allMusicians.value as? UiState.Success)?.data
            ?.firstOrNull { it.effectiveUserId == currentUserId }
        val myTopReel = (portfolioItems.value as? UiState.Success)?.data
            ?.firstOrNull { it.musicianId == currentUserId && it.isVideoOrReel }
            ?: SeedCatalog.portfolioVideos.firstOrNull()

        val rateType = if (gig.remote) RateType.PER_TRACK else RateType.PER_GIG
        val proposedRate = when (rateType) {
            RateType.PER_TRACK -> (myProfile?.perTrackRate ?: 10000L).coerceAtLeast(1000L)
            RateType.PER_GIG -> (myProfile?.perGigRate ?: 18000L).coerceAtLeast(2000L)
            RateType.HOURLY -> (myProfile?.hourlyRate ?: 3500L).coerceAtLeast(500L)
        }
        val note = customNote.ifBlank {
            "Applied with 1-click profile & top video reel (${myTopReel?.title ?: "Live Reel"})."
        }

        viewModelScope.launch {
            repository.applyToGig(
                gig = gig,
                musicianName = name,
                musicianHandle = handle,
                attachedReel = myTopReel,
                proposedRate = proposedRate,
                rateType = rateType,
                note = note
            ).onSuccess {
                _events.tryEmit("Applied to \"${gig.title}\" with reel & rate card")
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Could not submit application"))
            }
        }
    }

    fun sendBookingOffer(
        musician: MusicianProfile,
        proposedRate: Long,
        rateType: RateType,
        eventDate: String,
        scopeNotes: String,
        onCreated: (String) -> Unit = {}
    ) {
        if (musician.effectiveUserId == currentUserId) {
            _events.tryEmit("You cannot send a booking offer to your own profile.")
            return
        }
        val (name, _) = currentActorIdentity()
        viewModelScope.launch {
            repository.createBookingOffer(
                clientName = name,
                musician = musician,
                proposedRate = proposedRate,
                rateType = rateType,
                eventDate = eventDate,
                scopeNotes = scopeNotes
            ).onSuccess { bookingId ->
                _activeBookingId.value = bookingId
                _events.tryEmit("Booking offer sent to ${musician.name}")
                onCreated(bookingId)
            }.onFailure {
                _events.tryEmit(it.unwrapFirestoreMessage("Failed to send booking offer"))
            }
        }
    }

    fun sendMessage(booking: Booking, text: String) {
        val (name, _) = currentActorIdentity()
        viewModelScope.launch {
            repository.sendBookingMessage(booking, name, text)
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to send message")) }
        }
    }

    fun updateBookingStatus(booking: Booking, newStatus: BookingStatus) {
        val (name, _) = currentActorIdentity()
        viewModelScope.launch {
            repository.updateBookingStatus(booking, newStatus, name)
                .onSuccess { _events.tryEmit("Booking updated: ${newStatus.label}") }
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to update booking")) }
        }
    }

    fun submitReview(musicianId: String, rating: Long, comment: String) {
        val (name, _) = currentActorIdentity()
        viewModelScope.launch {
            repository.submitReview(musicianId, name, rating, comment)
                .onSuccess { _events.tryEmit("Review published") }
                .onFailure { _events.tryEmit(it.unwrapFirestoreMessage("Failed to submit review")) }
        }
    }

    fun showToastMessage(msg: String) {
        _events.tryEmit(msg)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
