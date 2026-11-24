package com.example.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.example.data.firebase.SoundStageRepository
import com.example.data.model.MusicianProfile
import com.example.data.model.PortfolioItem
import com.example.media.PersistentAudioController
import com.example.ui.components.PersistentMiniAudioBar
import com.example.ui.components.RickshawPaintRibbon
import com.example.ui.components.SendBookingOfferSheet
import com.example.ui.components.VideoTheaterDialog
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.GigsScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.MusicianProfileScreen
import com.example.ui.screens.StageScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.JaminiTerracotta
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageAmberContainer
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageOffWhite
import com.example.ui.theme.StageSurface
import com.example.ui.theme.StageTextSecondary
import com.example.ui.viewmodel.SoundStageViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.serialization.Serializable

@Serializable data object StageRoute
@Serializable data object DiscoverRoute
@Serializable data object GigsRoute
@Serializable data object InboxRoute
@Serializable data object StudioRoute
@Serializable data class MusicianProfileRoute(val musicianId: String)

private data class TopLevelDestination(
    val route: Any,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTagName: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = StageRoute,
        labelRes = R.string.nav_stage,
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        testTagName = "nav_stage"
    ),
    TopLevelDestination(
        route = DiscoverRoute,
        labelRes = R.string.nav_discover,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTagName = "nav_discover"
    ),
    TopLevelDestination(
        route = GigsRoute,
        labelRes = R.string.nav_gigs,
        selectedIcon = Icons.Filled.Work,
        unselectedIcon = Icons.Outlined.WorkOutline,
        testTagName = "nav_gigs"
    ),
    TopLevelDestination(
        route = InboxRoute,
        labelRes = R.string.nav_inbox,
        selectedIcon = Icons.Filled.Mail,
        unselectedIcon = Icons.Outlined.Mail,
        testTagName = "nav_inbox"
    ),
    TopLevelDestination(
        route = StudioRoute,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTagName = "nav_studio"
    )
)

@Composable
fun SoundStageApp(
    currentUserId: String,
    windowSizeClass: WindowSizeClass
) {
    val context = LocalContext.current
    val viewModel: SoundStageViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                SoundStageViewModel(
                    repository = SoundStageRepository(context.applicationContext),
                    currentUserId = currentUserId
                )
            }
        }
    )

    val audioController = remember(currentUserId) {
        PersistentAudioController(context.applicationContext)
    }
    DisposableEffect(audioController) {
        onDispose {
            audioController.release()
        }
    }

    val userAccountState by viewModel.userAccount.collectAsStateWithLifecycle()
    val socialConnectionsState by viewModel.socialConnections.collectAsStateWithLifecycle()
    val musiciansState by viewModel.allMusicians.collectAsStateWithLifecycle()
    val filteredMusiciansState by viewModel.filteredMusicians.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val portfolioState by viewModel.portfolioItems.collectAsStateWithLifecycle()
    val postsState by viewModel.posts.collectAsStateWithLifecycle()
    val likedPostIds by viewModel.likedPostIds.collectAsStateWithLifecycle()
    val commentsState by viewModel.activePostComments.collectAsStateWithLifecycle()
    val gigsState by viewModel.gigs.collectAsStateWithLifecycle()
    val myApplications by viewModel.myApplications.collectAsStateWithLifecycle()
    val bookingsState by viewModel.bookings.collectAsStateWithLifecycle()
    val activeBookingId by viewModel.activeBookingId.collectAsStateWithLifecycle()
    val activeBookingMessagesState by viewModel.activeBookingMessages.collectAsStateWithLifecycle()
    val activeMusicianReviews by viewModel.activeMusicianReviews.collectAsStateWithLifecycle()
    val audioState by audioController.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isProfileDetail = currentDestination?.hasRoute(MusicianProfileRoute::class) == true

    var theaterVideoItem by remember { mutableStateOf<PortfolioItem?>(null) }
    var bookingSheetMusician by remember { mutableStateOf<MusicianProfile?>(null) }

    val navigateTopLevel: (Any) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val openProfile: (String) -> Unit = { musicianId ->
        if (musicianId.isNotBlank()) {
            navController.navigate(MusicianProfileRoute(musicianId))
        }
    }

    val openBookingOffer: (MusicianProfile) -> Unit = { musician ->
        if (musician.effectiveUserId == currentUserId) {
            viewModel.showToastMessage("You cannot send a booking offer to your own profile.")
        } else {
            bookingSheetMusician = musician
        }
    }

    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val isWideInbox = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    Scaffold(
        containerColor = StageCharcoal,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!useNavigationRail) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PersistentMiniAudioBar(
                        audioState = audioState,
                        onTogglePlayPause = { audioController.togglePlayPause() },
                        onSeek = { frac -> audioController.seekToFraction(frac) },
                        onDismiss = { audioController.dismiss() },
                        onOpenArtistProfile = openProfile
                    )
                    if (!isProfileDetail) {
                        RickshawPaintRibbon(height = 3.dp)
                        NavigationBar(
                            containerColor = StageSurface,
                            contentColor = StageOffWhite,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            topLevelDestinations.forEach { dest ->
                                val selected = currentDestination?.hierarchy?.any {
                                    it.hasRoute(dest.route::class)
                                } == true
                                val label = stringResource(dest.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = StageAmber,
                                        selectedTextColor = StageAmber,
                                        indicatorColor = StageAmberContainer,
                                        unselectedIconColor = StageTextSecondary,
                                        unselectedTextColor = StageTextSecondary
                                    ),
                                    modifier = Modifier.testTag(dest.testTagName)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StageCharcoal)
        ) {
            if (useNavigationRail) {
                NavigationRail(
                    containerColor = StageSurface,
                    contentColor = StageOffWhite,
                    header = {
                        Surface(
                            color = StageAmberContainer,
                            shape = CircleShape,
                            modifier = Modifier
                                .padding(top = 12.dp, bottom = 16.dp)
                                .size(48.dp)
                                .border(2.dp, JaminiTerracotta, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.khyap_icon),
                                contentDescription = "ক্ষ্যাপ",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                ) {
                    topLevelDestinations.forEach { dest ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(dest.route::class)
                        } == true
                        val label = stringResource(dest.labelRes)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = StageAmber,
                                selectedTextColor = StageAmber,
                                indicatorColor = StageAmberContainer,
                                unselectedIconColor = StageTextSecondary,
                                unselectedTextColor = StageTextSecondary
                            ),
                            modifier = Modifier.testTag(dest.testTagName)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = StageRoute,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable<StageRoute> {
                            StageScreen(
                                currentUserId = currentUserId,
                                postsState = postsState,
                                musiciansState = musiciansState,
                                portfolioState = portfolioState,
                                likedPostIds = likedPostIds,
                                commentsState = commentsState,
                                onToggleLike = { viewModel.toggleLikePost(it) },
                                onOpenComments = { viewModel.selectCommentsPost(it.id) },
                                onSubmitComment = { post, text -> viewModel.addPostComment(post, text) },
                                onDismissComments = { viewModel.selectCommentsPost(null) },
                                onCreatePost = { content, postType, url, picked ->
                                    viewModel.createPost(content, postType, url, picked)
                                },
                                onOpenVideoTheater = { theaterVideoItem = it },
                                onPlayAudioDemo = { audioController.playTrack(it) },
                                onOpenProfile = openProfile,
                                onBookArtist = openBookingOffer
                            )
                        }

                        composable<DiscoverRoute> {
                            DiscoverScreen(
                                currentUserId = currentUserId,
                                musiciansState = filteredMusiciansState,
                                portfolioState = portfolioState,
                                filters = filters,
                                onUpdateFilters = { viewModel.updateFilters(it) },
                                onResetFilters = { viewModel.resetFilters() },
                                onOpenProfile = openProfile,
                                onOpenVideoTheater = { theaterVideoItem = it },
                                onBookArtist = openBookingOffer
                            )
                        }

                        composable<GigsRoute> {
                            GigsScreen(
                                currentUserId = currentUserId,
                                gigsState = gigsState,
                                myApplications = myApplications,
                                onOneClickApply = { viewModel.applyToGigOneClick(it) },
                                onCreateGig = { title, desc, specs, genre, loc, remote, date, minB, maxB ->
                                    viewModel.createGig(
                                        title = title,
                                        description = desc,
                                        specialties = specs,
                                        genre = genre,
                                        location = loc,
                                        remote = remote,
                                        eventDate = date,
                                        budgetMin = minB,
                                        budgetMax = maxB
                                    )
                                }
                            )
                        }

                        composable<InboxRoute> {
                            InboxScreen(
                                isWideScreen = isWideInbox,
                                currentUserId = currentUserId,
                                bookingsState = bookingsState,
                                activeBookingId = activeBookingId,
                                messagesState = activeBookingMessagesState,
                                musiciansState = musiciansState,
                                onSelectBooking = { viewModel.selectBookingThread(it) },
                                onSendMessage = { booking, text -> viewModel.sendMessage(booking, text) },
                                onUpdateStatus = { booking, status ->
                                    viewModel.updateBookingStatus(booking, status)
                                },
                                onOpenNewOfferForArtist = openBookingOffer
                            )
                        }

                        composable<StudioRoute> {
                            StudioScreen(
                                currentUserId = currentUserId,
                                userAccountState = userAccountState,
                                socialConnectionsState = socialConnectionsState,
                                musiciansState = musiciansState,
                                onSaveAccount = { role, name, handle, loc, bio ->
                                    viewModel.updateRoleAndProfile(role, name, handle, loc, bio)
                                },
                                onSaveStudioDetails = { specs, genres, hr, gig, track, avail, remote, gear ->
                                    viewModel.updateMusicianStudioDetails(
                                        specialties = specs,
                                        genres = genres,
                                        hourlyRate = hr,
                                        perGigRate = gig,
                                        perTrackRate = track,
                                        availabilityStatus = avail,
                                        remoteAvailable = remote,
                                        gearList = gear
                                    )
                                },
                                onConnectInstagram = { handle, followers, sandbox ->
                                    viewModel.connectInstagram(handle, followers, sandbox)
                                },
                                onDisconnectInstagram = { viewModel.disconnectInstagram() },
                                onImportInstagramReels = { reels, postToFeed ->
                                    viewModel.importInstagramReels(reels, postToFeed)
                                },
                                onAddSocialLink = { url, title, genre ->
                                    viewModel.addSocialLinkToPortfolio(url, title, genre)
                                },
                                onAddNativeVideo = { title, uri, genre ->
                                    viewModel.addNativePickedMedia(title, uri, isAudio = false, genreTag = genre)
                                },
                                onOpenPublicProfile = openProfile
                            )
                        }

                        composable<MusicianProfileRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<MusicianProfileRoute>()
                            MusicianProfileScreen(
                                musicianId = route.musicianId,
                                currentUserId = currentUserId,
                                musiciansState = musiciansState,
                                portfolioState = portfolioState,
                                reviews = activeMusicianReviews,
                                onSelectProfile = { viewModel.selectMusicianProfile(it) },
                                onBack = { navController.popBackStack() },
                                onOpenVideoTheater = { theaterVideoItem = it },
                                onPlayAudioDemo = { audioController.playTrack(it) },
                                onBookNow = openBookingOffer,
                                onSubmitReview = { musicianId, rating, comment ->
                                    viewModel.submitReview(musicianId, rating, comment)
                                }
                            )
                        }
                    }
                }

                if (useNavigationRail) {
                    PersistentMiniAudioBar(
                        audioState = audioState,
                        onTogglePlayPause = { audioController.togglePlayPause() },
                        onSeek = { frac -> audioController.seekToFraction(frac) },
                        onDismiss = { audioController.dismiss() },
                        onOpenArtistProfile = openProfile
                    )
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }

    theaterVideoItem?.let { item ->
        val musicians = (musiciansState as? UiState.Success)?.data.orEmpty()
        val musician = musicians.firstOrNull { it.effectiveUserId == item.musicianId }
        VideoTheaterDialog(
            item = item,
            musician = musician,
            isOwnProfile = item.musicianId == currentUserId,
            onDismiss = { theaterVideoItem = null },
            onBookArtist = { target ->
                theaterVideoItem = null
                openBookingOffer(target)
            },
            onViewProfile = { id ->
                theaterVideoItem = null
                openProfile(id)
            }
        )
    }

    bookingSheetMusician?.let { musician ->
        SendBookingOfferSheet(
            musician = musician,
            onDismiss = { bookingSheetMusician = null },
            onSubmitOffer = { rate, rateType, date, notes ->
                viewModel.sendBookingOffer(
                    musician = musician,
                    proposedRate = rate,
                    rateType = rateType,
                    eventDate = date,
                    scopeNotes = notes,
                    onCreated = {
                        navigateTopLevel(InboxRoute)
                    }
                )
            }
        )
    }
}
