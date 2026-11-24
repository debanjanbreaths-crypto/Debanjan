package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.model.AvailabilityStatus
import com.example.data.model.Booking
import com.example.data.model.BookingMessage
import com.example.data.model.BookingStatus
import com.example.data.model.FeedPost
import com.example.data.model.GigApplication
import com.example.data.model.GigListing
import com.example.data.model.GigStatus
import com.example.data.model.MessageType
import com.example.data.model.MusicianProfile
import com.example.data.model.MusicianReview
import com.example.data.model.PortfolioItem
import com.example.data.model.PortfolioMediaType
import com.example.data.model.PostComment
import com.example.data.model.PostMediaType
import com.example.data.model.PostType
import com.example.data.model.RateType
import com.example.data.model.SocialConnection
import com.example.data.model.SocialProvider
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.formatBdtAmount
import com.example.data.seed.SeedCatalog
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SoundStageRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    private fun <T> Flow<T>.wrapFirestoreFlow(
        uid: String,
        operationType: OperationType,
        path: String
    ): Flow<T> = catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == uid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
        throw error
    }

    // --- ACCOUNT INITIALIZATION & USER OBSERVATION ---

    suspend fun ensureUserInitialized(user: FirebaseUser): Result<Unit> {
        val uid = user.uid
        var currentPath = "users/$uid"
        var currentOp = OperationType.GET
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val displayName = (user.displayName ?: "ক্ষ্যাপ Artist").trim().take(80).ifEmpty { "ক্ষ্যাপ Artist" }
            val rawHandle = "@" + displayName.lowercase().replace(Regex("[^a-z0-9_]"), "").take(24).ifEmpty { "artist" }
            if (!userDoc.exists()) {
                currentOp = OperationType.CREATE
                val accountMap = mapOf(
                    "userId" to uid,
                    "name" to displayName,
                    "handle" to rawHandle,
                    "role" to UserRole.MUSICIAN.name,
                    "avatarUrl" to (user.photoUrl?.toString()?.take(500) ?: ""),
                    "bio" to "Musician & creator in Dhaka on ক্ষ্যাপ.",
                    "location" to "Dhaka, Bangladesh",
                    "verified" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(accountMap).await()
            }

            currentPath = "musician_profiles/$uid"
            currentOp = OperationType.GET
            val profileDoc = db.collection("musician_profiles").document(uid).get().await()
            if (!profileDoc.exists()) {
                currentOp = OperationType.CREATE
                val profileMap = mapOf(
                    "userId" to uid,
                    "name" to displayName,
                    "handle" to rawHandle,
                    "avatarUrl" to (user.photoUrl?.toString()?.take(500) ?: ""),
                    "coverUrl" to "res://img_stage_hero",
                    "bio" to "Available for Dhaka studio sessions, live shows, and remote tracking.",
                    "location" to "Dhaka, Bangladesh",
                    "remoteAvailable" to true,
                    "verified" to false,
                    "instagramConnected" to false,
                    "instagramHandle" to "",
                    "instagramFollowers" to 0L,
                    "specialties" to listOf("Session Guitarist"),
                    "genres" to listOf("Bangla Rock", "Pop"),
                    "instruments" to listOf("Guitar", "Vocals"),
                    "experienceYears" to 3L,
                    "hourlyRate" to 3500L,
                    "perGigRate" to 18000L,
                    "perTrackRate" to 10000L,
                    "currency" to "BDT",
                    "availabilityStatus" to AvailabilityStatus.AVAILABLE.name,
                    "gearList" to "Universal Audio Apollo, Fender Stratocaster, Logic Pro",
                    "hasVideoDemos" to false,
                    "publicListed" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                db.collection("musician_profiles").document(uid).set(profileMap).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, currentOp, currentPath)
            Result.failure(e)
        }
    }

    fun observeUserAccount(userId: String): Flow<UserAccount?> = flow {
        val uid = requireUserId()
        require(uid == userId) { "Refusing cross-user account observation" }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(UserAccount::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .wrapFirestoreFlow(uid, OperationType.GET, path)
        )
    }

    fun observeSocialConnections(userId: String): Flow<List<SocialConnection>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "users/$uid/social_connections"
        emitAll(
            db.collection("users").document(uid).collection("social_connections")
                .snapshots()
                .map { snap -> snap.toObjects(SocialConnection::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeLikedPostIds(userId: String): Flow<Set<String>> = flow {
        val uid = requireUserId()
        require(uid == userId)
        val path = "users/$uid/liked_posts"
        emitAll(
            db.collection("users").document(uid).collection("liked_posts")
                .snapshots()
                .map { snap -> snap.documents.map { it.id }.toSet() }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    // --- MARKETPLACE & FEED OBSERVERS (Merged with Curated SeedCatalog) ---

    fun observeMusicians(): Flow<List<MusicianProfile>> = flow {
        val uid = requireUserId()
        val path = "musician_profiles"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .snapshots()
                .map { snap ->
                    val live = snap.toObjects(MusicianProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    val liveIds = live.map { it.effectiveUserId }.toSet()
                    live + SeedCatalog.musicians.filter { it.effectiveUserId !in liveIds }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observePortfolioItems(): Flow<List<PortfolioItem>> = flow {
        val uid = requireUserId()
        val path = "portfolio_items"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .snapshots()
                .map { snap ->
                    val live = snap.toObjects(PortfolioItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    live + SeedCatalog.portfolioVideos + SeedCatalog.portfolioAudioDemos
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observePosts(): Flow<List<FeedPost>> = flow {
        val uid = requireUserId()
        val path = "posts"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .snapshots()
                .map { snap ->
                    val live = snap.toObjects(FeedPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    live + SeedCatalog.posts
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observePostComments(postId: String): Flow<List<PostComment>> = flow {
        val uid = requireUserId()
        val path = "post_comments"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .whereEqualTo("postId", postId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(PostComment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeGigs(): Flow<List<GigListing>> = flow {
        val uid = requireUserId()
        val path = "gigs"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .snapshots()
                .map { snap ->
                    val live = snap.toObjects(GigListing::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    live + SeedCatalog.gigs
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeMyGigApplications(): Flow<List<GigApplication>> = flow {
        val uid = requireUserId()
        val path = "gig_applications"
        emitAll(
            db.collection(path)
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(GigApplication::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeBookings(): Flow<List<Booking>> = flow {
        val uid = requireUserId()
        val path = "bookings"
        emitAll(
            db.collection(path)
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Booking::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeBookingMessages(bookingId: String): Flow<List<BookingMessage>> = flow {
        val uid = requireUserId()
        val path = "bookings/$bookingId/messages"
        emitAll(
            db.collection("bookings").document(bookingId).collection("messages")
                .whereArrayContains("participantUids", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(BookingMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    fun observeReviewsForMusician(musicianId: String): Flow<List<MusicianReview>> = flow {
        val uid = requireUserId()
        val path = "musician_reviews"
        emitAll(
            db.collection(path)
                .whereEqualTo("publicListed", true)
                .whereEqualTo("musicianId", musicianId)
                .snapshots()
                .map { snap ->
                    val live = snap.toObjects(MusicianReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    live + SeedCatalog.reviews.filter { it.musicianId == musicianId }
                }
                .wrapFirestoreFlow(uid, OperationType.LIST, path)
        )
    }

    // --- MUTATIONS ---

    suspend fun updateUserRoleAndBio(
        role: UserRole,
        name: String,
        handle: String,
        location: String,
        bio: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val userPath = "users/$uid"
        return try {
            val cleanName = name.trim().take(80).ifEmpty { "ক্ষ্যাপ Artist" }
            val cleanHandle = handle.trim().let { if (it.startsWith("@")) it else "@$it" }.take(40)
            require(cleanHandle.length >= 2) { "Handle must be at least 2 characters" }
            val cleanLocation = location.trim().take(100)
            val cleanBio = bio.trim().take(500)

            db.collection("users").document(uid).update(
                mapOf(
                    "role" to role.name,
                    "name" to cleanName,
                    "handle" to cleanHandle,
                    "location" to cleanLocation,
                    "bio" to cleanBio,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val profileRef = db.collection("musician_profiles").document(uid)
            if (profileRef.get().await().exists()) {
                profileRef.update(
                    mapOf(
                        "name" to cleanName,
                        "handle" to cleanHandle,
                        "location" to cleanLocation,
                        "bio" to cleanBio.take(600),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userPath)
            Result.failure(e)
        }
    }

    suspend fun updateMusicianStudioDetails(
        specialties: List<String>,
        genres: List<String>,
        hourlyRate: Long,
        perGigRate: Long,
        perTrackRate: Long,
        availabilityStatus: AvailabilityStatus,
        remoteAvailable: Boolean,
        gearList: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val path = "musician_profiles/$uid"
        return try {
            db.collection("musician_profiles").document(uid).update(
                mapOf(
                    "specialties" to specialties.take(10),
                    "genres" to genres.take(10),
                    "hourlyRate" to hourlyRate.coerceIn(0L, 50000L),
                    "perGigRate" to perGigRate.coerceIn(0L, 200000L),
                    "perTrackRate" to perTrackRate.coerceIn(0L, 100000L),
                    "availabilityStatus" to availabilityStatus.name,
                    "remoteAvailable" to remoteAvailable,
                    "gearList" to gearList.trim().take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun connectInstagramAccount(
        instagramHandle: String,
        followerCount: Long,
        sandboxMode: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val connPath = "users/$uid/social_connections/instagram"
        return try {
            val cleanHandle = instagramHandle.trim().let { if (it.startsWith("@")) it else "@$it" }.take(60)
            require(cleanHandle.length >= 2) { "Instagram handle is required" }
            val normalizedFollowers = followerCount.coerceIn(0L, 100000000L)
            val slug = cleanHandle.removePrefix("@")

            val docRef = db.collection("users").document(uid)
                .collection("social_connections").document("instagram")
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "externalHandle" to cleanHandle,
                        "profileUrl" to "https://instagram.com/$slug",
                        "followerCount" to normalizedFollowers,
                        "connected" to true,
                        "sandboxMode" to sandboxMode,
                        "lastSyncedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "provider" to SocialProvider.INSTAGRAM.name,
                        "externalHandle" to cleanHandle,
                        "profileUrl" to "https://instagram.com/$slug",
                        "followerCount" to normalizedFollowers,
                        "connected" to true,
                        "sandboxMode" to sandboxMode,
                        "lastSyncedAt" to FieldValue.serverTimestamp(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            db.collection("musician_profiles").document(uid).update(
                mapOf(
                    "instagramConnected" to true,
                    "instagramHandle" to cleanHandle,
                    "instagramFollowers" to normalizedFollowers,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, connPath)
            Result.failure(e)
        }
    }

    suspend fun disconnectInstagramAccount(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val connPath = "users/$uid/social_connections/instagram"
        return try {
            val docRef = db.collection("users").document(uid)
                .collection("social_connections").document("instagram")
            if (docRef.get().await().exists()) {
                docRef.update(
                    mapOf(
                        "connected" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            db.collection("musician_profiles").document(uid).update(
                mapOf(
                    "instagramConnected" to false,
                    "instagramHandle" to "",
                    "instagramFollowers" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, connPath)
            Result.failure(e)
        }
    }

    suspend fun addPortfolioItem(
        musicianName: String,
        musicianHandle: String,
        title: String,
        mediaType: PortfolioMediaType,
        sourceUrl: String,
        thumbnailUrl: String,
        aspectRatio: String,
        genreTag: String,
        duration: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("portfolio_items").document()
        return try {
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "Title cannot be empty" }
            require(sourceUrl.trim().isNotEmpty()) { "Source URL is required" }

            val payload = mapOf(
                "musicianId" to uid,
                "musicianName" to musicianName.trim().take(80).ifEmpty { "Artist" },
                "musicianHandle" to musicianHandle.trim().take(40).ifEmpty { "@artist" },
                "title" to cleanTitle,
                "mediaType" to mediaType.name,
                "sourceUrl" to sourceUrl.trim().take(800),
                "thumbnailUrl" to thumbnailUrl.trim().take(800),
                "aspectRatio" to if (aspectRatio in listOf("9:16", "16:9", "1:1")) aspectRatio else "9:16",
                "genreTag" to genreTag.trim().take(40).ifEmpty { "Live" },
                "duration" to duration.trim().take(20).ifEmpty { "0:30" },
                "viewsCount" to 1L,
                "publicListed" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()

            if (mediaType != PortfolioMediaType.AUDIO) {
                val profileRef = db.collection("musician_profiles").document(uid)
                if (profileRef.get().await().exists()) {
                    profileRef.update(
                        mapOf(
                            "hasVideoDemos" to true,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createPost(
        authorName: String,
        authorHandle: String,
        authorAvatarUrl: String,
        authorVerified: Boolean,
        content: String,
        mediaType: PostMediaType,
        mediaUrl: String,
        thumbnailUrl: String,
        postType: PostType
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val docRef = db.collection("posts").document()
        return try {
            val cleanContent = content.trim().take(1000)
            require(cleanContent.isNotEmpty()) { "Post content cannot be empty" }

            val payload = mapOf(
                "authorId" to uid,
                "authorName" to authorName.trim().take(80).ifEmpty { "Artist" },
                "authorHandle" to authorHandle.trim().take(40).ifEmpty { "@artist" },
                "authorAvatarUrl" to authorAvatarUrl.trim().take(500),
                "authorVerified" to authorVerified,
                "content" to cleanContent,
                "mediaType" to mediaType.name,
                "mediaUrl" to mediaUrl.trim().take(800),
                "thumbnailUrl" to thumbnailUrl.trim().take(800),
                "postType" to postType.name,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "publicListed" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleLikePost(post: FeedPost, currentlyLiked: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val likeDoc = db.collection("users").document(uid)
            .collection("liked_posts").document(post.id)
        return try {
            if (currentlyLiked) {
                likeDoc.delete().await()
            } else {
                likeDoc.set(
                    mapOf(
                        "userId" to uid,
                        "postId" to post.id,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            if (!post.isSeeded && !post.id.startsWith("seed_")) {
                val delta = if (currentlyLiked) -1L else 1L
                val postRef = db.collection("posts").document(post.id)
                if (postRef.get().await().exists()) {
                    postRef.update(
                        mapOf(
                            "likesCount" to FieldValue.increment(delta),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, likeDoc.path)
            Result.failure(e)
        }
    }

    suspend fun addPostComment(
        post: FeedPost,
        authorName: String,
        authorHandle: String,
        text: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val commentRef = db.collection("post_comments").document()
        return try {
            val cleanText = text.trim().take(400)
            require(cleanText.isNotEmpty()) { "Comment cannot be empty" }
            commentRef.set(
                mapOf(
                    "postId" to post.id,
                    "authorId" to uid,
                    "authorName" to authorName.trim().take(80).ifEmpty { "Artist" },
                    "authorHandle" to authorHandle.trim().take(40).ifEmpty { "@artist" },
                    "text" to cleanText,
                    "publicListed" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (!post.isSeeded && !post.id.startsWith("seed_")) {
                val postRef = db.collection("posts").document(post.id)
                if (postRef.get().await().exists()) {
                    postRef.update(
                        mapOf(
                            "commentsCount" to FieldValue.increment(1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun createGig(
        clientName: String,
        title: String,
        description: String,
        requiredSpecialties: List<String>,
        genre: String,
        location: String,
        remote: Boolean,
        eventDate: String,
        budgetMin: Long,
        budgetMax: Long
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val gigRef = db.collection("gigs").document()
        return try {
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.length >= 3) { "Gig title must be at least 3 characters" }
            val cleanDesc = description.trim().take(1500)
            require(cleanDesc.isNotEmpty()) { "Description is required" }
            val safeMin = budgetMin.coerceIn(0L, 1000000L)
            val safeMax = budgetMax.coerceIn(safeMin, 1000000L)

            gigRef.set(
                mapOf(
                    "clientId" to uid,
                    "clientName" to clientName.trim().take(80).ifEmpty { "Organizer" },
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "requiredSpecialties" to requiredSpecialties.take(10),
                    "genre" to genre.trim().take(50).ifEmpty { "All Genres" },
                    "location" to if (remote) "Remote" else location.trim().take(100).ifEmpty { "TBD" },
                    "remote" to remote,
                    "eventDate" to eventDate.trim().take(40).ifEmpty { "Flexible" },
                    "budgetMin" to safeMin,
                    "budgetMax" to safeMax,
                    "status" to GigStatus.OPEN.name,
                    "publicListed" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, gigRef.path)
            Result.failure(e)
        }
    }

    suspend fun applyToGig(
        gig: GigListing,
        musicianName: String,
        musicianHandle: String,
        attachedReel: PortfolioItem?,
        proposedRate: Long,
        rateType: RateType,
        note: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val appRef = db.collection("gig_applications").document("${gig.id}_$uid")
        return try {
            require(gig.clientId != uid) { "You cannot apply to your own gig listing." }
            appRef.set(
                mapOf(
                    "gigId" to gig.id,
                    "gigTitle" to gig.title.take(120),
                    "clientId" to gig.clientId,
                    "musicianId" to uid,
                    "musicianName" to musicianName.trim().take(80).ifEmpty { "Artist" },
                    "musicianHandle" to musicianHandle.trim().take(40).ifEmpty { "@artist" },
                    "attachedReelTitle" to (attachedReel?.title?.take(120) ?: "Artist Portfolio Reel"),
                    "attachedReelUrl" to (attachedReel?.sourceUrl?.take(800) ?: ""),
                    "proposedRate" to proposedRate.coerceIn(0L, 500000L),
                    "rateType" to rateType.name,
                    "note" to note.trim().take(500),
                    "status" to "APPLIED",
                    "participantUids" to listOf(uid, gig.clientId),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, appRef.path)
            Result.failure(e)
        }
    }

    suspend fun createBookingOffer(
        clientName: String,
        musician: MusicianProfile,
        gigId: String = "",
        proposedRate: Long,
        rateType: RateType,
        eventDate: String,
        scopeNotes: String
    ): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val targetMusicianId = musician.effectiveUserId
        val bookingRef = db.collection("bookings").document()
        return try {
            require(targetMusicianId != uid) { "You cannot send a booking offer to your own profile." }
            require(proposedRate >= 1L) { "Proposed rate must be at least ৳1" }
            val cleanScope = scopeNotes.trim().take(1000)
            require(cleanScope.isNotEmpty()) { "Please enter scope notes for this booking." }
            val cleanDate = eventDate.trim().take(40).ifEmpty { "TBD" }
            val rateFormatted = formatBdtAmount(proposedRate)
            val preview = "Offer: $rateFormatted${rateType.unitSuffix} · $cleanDate"
            val participants = listOf(uid, targetMusicianId)

            bookingRef.set(
                mapOf(
                    "clientId" to uid,
                    "clientName" to clientName.trim().take(80).ifEmpty { "Client" },
                    "musicianId" to targetMusicianId,
                    "musicianName" to musician.name.take(80),
                    "musicianHandle" to musician.handle.take(40),
                    "musicianAvatarUrl" to musician.avatarUrl.take(500),
                    "gigId" to gigId.take(128),
                    "proposedRate" to proposedRate.coerceIn(1L, 1000000L),
                    "rateType" to rateType.name,
                    "eventDate" to cleanDate,
                    "scopeNotes" to cleanScope,
                    "lastMessagePreview" to preview.take(300),
                    "status" to BookingStatus.PENDING.name,
                    "participantUids" to participants,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            bookingRef.collection("messages").document().set(
                mapOf(
                    "bookingId" to bookingRef.id,
                    "senderId" to uid,
                    "senderName" to clientName.trim().take(80).ifEmpty { "Client" },
                    "text" to "Booking Offer: $rateFormatted${rateType.unitSuffix} on $cleanDate — $cleanScope".take(1000),
                    "messageType" to MessageType.BOOKING_OFFER.name,
                    "participantUids" to participants,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(bookingRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, bookingRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendBookingMessage(
        booking: Booking,
        senderName: String,
        text: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val msgRef = db.collection("bookings").document(booking.id).collection("messages").document()
        return try {
            val cleanText = text.trim().take(1000)
            require(cleanText.isNotEmpty()) { "Message cannot be empty" }
            msgRef.set(
                mapOf(
                    "bookingId" to booking.id,
                    "senderId" to uid,
                    "senderName" to senderName.trim().take(80).ifEmpty { "User" },
                    "text" to cleanText,
                    "messageType" to MessageType.TEXT.name,
                    "participantUids" to booking.participantUids,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            db.collection("bookings").document(booking.id).update(
                mapOf(
                    "lastMessagePreview" to cleanText.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(
        booking: Booking,
        newStatus: BookingStatus,
        actorName: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val bookingRef = db.collection("bookings").document(booking.id)
        return try {
            val preview = when (newStatus) {
                BookingStatus.ACCEPTED -> "Offer accepted by ${booking.musicianName}"
                BookingStatus.DECLINED -> "Offer declined by ${booking.musicianName}"
                BookingStatus.COMPLETED -> "Booking marked completed"
                BookingStatus.PENDING -> "Pending review"
            }
            bookingRef.update(
                mapOf(
                    "status" to newStatus.name,
                    "lastMessagePreview" to preview.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            bookingRef.collection("messages").document().set(
                mapOf(
                    "bookingId" to booking.id,
                    "senderId" to uid,
                    "senderName" to actorName.trim().take(80).ifEmpty { "User" },
                    "text" to preview,
                    "messageType" to MessageType.STATUS_UPDATE.name,
                    "participantUids" to booking.participantUids,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, bookingRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitReview(
        musicianId: String,
        authorName: String,
        rating: Long,
        comment: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not signed in"))
        val reviewRef = db.collection("musician_reviews").document()
        return try {
            require(musicianId != uid) { "You cannot review your own profile." }
            val cleanComment = comment.trim().take(500)
            require(cleanComment.isNotEmpty()) { "Review comment is required" }
            reviewRef.set(
                mapOf(
                    "musicianId" to musicianId,
                    "authorId" to uid,
                    "authorName" to authorName.trim().take(80).ifEmpty { "Client" },
                    "rating" to rating.coerceIn(1L, 5L),
                    "comment" to cleanComment,
                    "publicListed" to true,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reviewRef.path)
            Result.failure(e)
        }
    }
}
