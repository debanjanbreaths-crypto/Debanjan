package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole(val label: String) {
    MUSICIAN("Musician / Creator"),
    CLIENT("Client / Organizer");

    companion object {
        fun fromRaw(raw: String?): UserRole =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MUSICIAN
    }
}

enum class SocialProvider(val label: String) {
    INSTAGRAM("Instagram"),
    YOUTUBE("YouTube"),
    SPOTIFY("Spotify");

    companion object {
        fun fromRaw(raw: String?): SocialProvider =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: INSTAGRAM
    }
}

enum class AvailabilityStatus(val label: String) {
    AVAILABLE("Available"),
    LIMITED("Limited"),
    BOOKED("Booked");

    companion object {
        fun fromRaw(raw: String?): AvailabilityStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: AVAILABLE
    }
}

enum class PortfolioMediaType(val label: String) {
    NATIVE_VIDEO("Native Video"),
    INSTAGRAM_REEL("Instagram Reel"),
    YOUTUBE_VIDEO("YouTube Video"),
    AUDIO("Audio Demo");

    companion object {
        fun fromRaw(raw: String?): PortfolioMediaType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NATIVE_VIDEO
    }
}

enum class PostMediaType {
    VIDEO,
    REEL_EMBED,
    AUDIO,
    IMAGE,
    NONE;

    companion object {
        fun fromRaw(raw: String?): PostMediaType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NONE
    }
}

enum class PostType(val label: String) {
    JAM_CLIP("Jam Clip"),
    LIVE_PERFORMANCE("Live Performance"),
    LOOKING_FOR_BANDMATE("Looking for Bandmate"),
    GEAR_TALK("Gear Talk");

    companion object {
        fun fromRaw(raw: String?): PostType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: JAM_CLIP
    }
}

enum class GigStatus(val label: String) {
    OPEN("Open"),
    FILLED("Filled");

    companion object {
        fun fromRaw(raw: String?): GigStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OPEN
    }
}

enum class RateType(val label: String, val unitSuffix: String) {
    HOURLY("Hourly", "/hr"),
    PER_GIG("Per Gig", "/gig"),
    PER_TRACK("Per Track", "/track");

    companion object {
        fun fromRaw(raw: String?): RateType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PER_GIG
    }
}

enum class BookingStatus(val label: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    DECLINED("Declined"),
    COMPLETED("Completed");

    companion object {
        fun fromRaw(raw: String?): BookingStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PENDING
    }
}

enum class MessageType {
    TEXT,
    BOOKING_OFFER,
    STATUS_UPDATE;

    companion object {
        fun fromRaw(raw: String?): MessageType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: TEXT
    }
}

@Keep
@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val handle: String = "",
    val role: String = UserRole.MUSICIAN.name,
    val avatarUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val verified: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SocialConnection(
    @DocumentId val id: String = "",
    val userId: String = "",
    val provider: String = SocialProvider.INSTAGRAM.name,
    val externalHandle: String = "",
    val profileUrl: String = "",
    val followerCount: Long = 0L,
    val connected: Boolean = false,
    val sandboxMode: Boolean = true,
    @ServerTimestamp val lastSyncedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

fun formatBdtAmount(amount: Long): String {
    val formatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(amount)
    return "৳$formatted"
}

@Keep
@IgnoreExtraProperties
data class MusicianProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val handle: String = "",
    val avatarUrl: String = "",
    val coverUrl: String = "",
    val bio: String = "",
    val location: String = "",
    val remoteAvailable: Boolean = true,
    val verified: Boolean = false,
    val instagramConnected: Boolean = false,
    val instagramHandle: String = "",
    val instagramFollowers: Long = 0L,
    val specialties: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val instruments: List<String> = emptyList(),
    val experienceYears: Long = 0L,
    val hourlyRate: Long = 0L,
    val perGigRate: Long = 0L,
    val perTrackRate: Long = 0L,
    val currency: String = "BDT",
    val availabilityStatus: String = AvailabilityStatus.AVAILABLE.name,
    val gearList: String = "",
    val hasVideoDemos: Boolean = true,
    val publicListed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val primaryStartingRateLabel: String
        get() = when {
            hourlyRate > 0 -> "${formatBdtAmount(hourlyRate)}/hr"
            perGigRate > 0 -> "${formatBdtAmount(perGigRate)}/gig"
            perTrackRate > 0 -> "${formatBdtAmount(perTrackRate)}/track"
            else -> "Custom Quote"
        }

    val effectiveUserId: String
        get() = userId.ifBlank { id }
}

@Keep
@IgnoreExtraProperties
data class PortfolioItem(
    @DocumentId val id: String = "",
    val musicianId: String = "",
    val musicianName: String = "",
    val musicianHandle: String = "",
    val title: String = "",
    val mediaType: String = PortfolioMediaType.NATIVE_VIDEO.name,
    val sourceUrl: String = "",
    val thumbnailUrl: String = "",
    val aspectRatio: String = "9:16",
    val genreTag: String = "",
    val duration: String = "0:30",
    val viewsCount: Long = 0L,
    val publicListed: Boolean = true,
    val externalLinkUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isVideoOrReel: Boolean
        get() = PortfolioMediaType.fromRaw(mediaType) != PortfolioMediaType.AUDIO

    val isVertical: Boolean
        get() = aspectRatio == "9:16"
}

@Keep
@IgnoreExtraProperties
data class FeedPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val authorAvatarUrl: String = "",
    val authorVerified: Boolean = false,
    val content: String = "",
    val mediaType: String = PostMediaType.NONE.name,
    val mediaUrl: String = "",
    val thumbnailUrl: String = "",
    val postType: String = PostType.JAM_CLIP.name,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val publicListed: Boolean = true,
    val isSeeded: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PostComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorHandle: String = "",
    val text: String = "",
    val publicListed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GigListing(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val title: String = "",
    val description: String = "",
    val requiredSpecialties: List<String> = emptyList(),
    val genre: String = "",
    val location: String = "",
    val remote: Boolean = false,
    val eventDate: String = "",
    val budgetMin: Long = 0L,
    val budgetMax: Long = 0L,
    val status: String = GigStatus.OPEN.name,
    val publicListed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class GigApplication(
    @DocumentId val id: String = "",
    val gigId: String = "",
    val gigTitle: String = "",
    val clientId: String = "",
    val musicianId: String = "",
    val musicianName: String = "",
    val musicianHandle: String = "",
    val attachedReelTitle: String = "",
    val attachedReelUrl: String = "",
    val proposedRate: Long = 0L,
    val rateType: String = RateType.PER_GIG.name,
    val note: String = "",
    val status: String = "APPLIED",
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Booking(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val musicianId: String = "",
    val musicianName: String = "",
    val musicianHandle: String = "",
    val musicianAvatarUrl: String = "",
    val gigId: String = "",
    val proposedRate: Long = 0L,
    val rateType: String = RateType.PER_GIG.name,
    val eventDate: String = "",
    val scopeNotes: String = "",
    val lastMessagePreview: String = "",
    val status: String = BookingStatus.PENDING.name,
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BookingMessage(
    @DocumentId val id: String = "",
    val bookingId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val messageType: String = MessageType.TEXT.name,
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class MusicianReview(
    @DocumentId val id: String = "",
    val musicianId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val publicListed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ParsedSocialMedia(
    val mediaType: PortfolioMediaType,
    val normalizedUrl: String,
    val playableStreamUrl: String,
    val thumbnailUrl: String,
    val suggestedTitle: String,
    val aspectRatio: String
)

object SocialUrlValidator {
    private val instagramRegex = Regex(
        """^https?://(www\.)?instagram\.com/(reel|reels|p|tv)/([A-Za-z0-9_\-]+)/?(\?.*)?$""",
        RegexOption.IGNORE_CASE
    )
    private val youtubeRegex = Regex(
        """^https?://(www\.|m\.)?(youtube\.com/(watch\?v=|shorts/|embed/)|youtu\.be/)([A-Za-z0-9_\-]{6,20})([&?].*)?$""",
        RegexOption.IGNORE_CASE
    )
    private val directMediaRegex = Regex(
        """^https?://[^\s]+\.(mp4|m3u8|mp3|ogg|m4a|wav)(\?.*)?$""",
        RegexOption.IGNORE_CASE
    )

    fun parseAndValidate(rawUrl: String): ParsedSocialMedia? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return null

        instagramRegex.matchEntire(trimmed)?.let { match ->
            val kind = match.groupValues[2].lowercase()
            val shortcode = match.groupValues[3]
            val isReel = kind == "reel" || kind == "reels"
            return ParsedSocialMedia(
                mediaType = PortfolioMediaType.INSTAGRAM_REEL,
                normalizedUrl = trimmed,
                playableStreamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/shortform_3.mp4",
                thumbnailUrl = "res://img_studio_guitar",
                suggestedTitle = if (isReel) "Instagram Reel ($shortcode)" else "Instagram Clip ($shortcode)",
                aspectRatio = "9:16"
            )
        }

        youtubeRegex.matchEntire(trimmed)?.let { match ->
            val videoId = match.groupValues[4]
            val isShorts = trimmed.contains("/shorts/", ignoreCase = true)
            return ParsedSocialMedia(
                mediaType = PortfolioMediaType.YOUTUBE_VIDEO,
                normalizedUrl = trimmed,
                playableStreamUrl = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-25s.mp4",
                thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                suggestedTitle = if (isShorts) "YouTube Short ($videoId)" else "YouTube Performance ($videoId)",
                aspectRatio = if (isShorts) "9:16" else "16:9"
            )
        }

        directMediaRegex.matchEntire(trimmed)?.let {
            val isAudio = trimmed.endsWith(".mp3", ignoreCase = true) ||
                trimmed.endsWith(".ogg", ignoreCase = true) ||
                trimmed.endsWith(".m4a", ignoreCase = true) ||
                trimmed.endsWith(".wav", ignoreCase = true)
            return ParsedSocialMedia(
                mediaType = if (isAudio) PortfolioMediaType.AUDIO else PortfolioMediaType.NATIVE_VIDEO,
                normalizedUrl = trimmed,
                playableStreamUrl = trimmed,
                thumbnailUrl = if (isAudio) "res://img_vocal_booth" else "res://img_stage_hero",
                suggestedTitle = if (isAudio) "Studio Audio Demo" else "Native Performance Video",
                aspectRatio = if (isAudio) "1:1" else "16:9"
            )
        }

        return null
    }
}
