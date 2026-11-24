package com.example

import com.example.data.model.PortfolioMediaType
import com.example.data.model.SocialUrlValidator
import com.example.data.model.formatBdtAmount
import com.example.data.seed.SeedCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun socialUrlValidator_acceptsInstagramYouTubeAndDirectMedia() {
        val ig = SocialUrlValidator.parseAndValidate("https://www.instagram.com/reel/Cx9AbCdEf12/")
        assertNotNull(ig)
        assertEquals(PortfolioMediaType.INSTAGRAM_REEL, ig?.mediaType)
        assertEquals("9:16", ig?.aspectRatio)

        val yt = SocialUrlValidator.parseAndValidate("https://youtu.be/dQw4w9WgXcQ")
        assertNotNull(yt)
        assertEquals(PortfolioMediaType.YOUTUBE_VIDEO, yt?.mediaType)
        assertEquals("https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg", yt?.thumbnailUrl)

        val mp3 = SocialUrlValidator.parseAndValidate("https://example.com/tracks/demo.mp3")
        assertNotNull(mp3)
        assertEquals(PortfolioMediaType.AUDIO, mp3?.mediaType)

        assertNull(SocialUrlValidator.parseAndValidate("not-a-valid-url"))
        assertNull(SocialUrlValidator.parseAndValidate("https://example.com/page.html"))
    }

    @Test
    fun seedCatalog_hasExpectedMinimumRecordsPlayableMediaAndDhakaBdtLocalization() {
        assertTrue(SeedCatalog.musicians.size >= 8)
        assertTrue(SeedCatalog.portfolioVideos.size >= 10)
        assertTrue(SeedCatalog.portfolioAudioDemos.size >= 5)
        assertTrue(SeedCatalog.posts.size >= 6)
        assertTrue(SeedCatalog.gigs.size >= 5)
        assertTrue(SeedCatalog.portfolioVideos.all { it.sourceUrl.startsWith("https://") })
        assertTrue(SeedCatalog.portfolioAudioDemos.all { it.sourceUrl.startsWith("https://") })

        // Verify Dhaka localization and BDT (৳) currency without USD ($)
        assertTrue(SeedCatalog.musicians.all { it.currency == "BDT" })
        assertTrue(SeedCatalog.musicians.all { it.location.contains("Dhaka") })
        assertTrue(SeedCatalog.musicians.all { it.primaryStartingRateLabel.startsWith("৳") })
        assertTrue(SeedCatalog.musicians.none { it.primaryStartingRateLabel.contains("$") })
        assertEquals("৳18,000", formatBdtAmount(18000L))
        assertFalse(formatBdtAmount(3500L).contains("$"))
    }
}
