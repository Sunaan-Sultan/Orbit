package com.orbit.starsystems

import com.orbit.starsystems.core.DeepLink
import com.orbit.starsystems.core.OrbitData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * A deep link that does not match the manifest's intent filter fails silently at both ends, so
 * the two link shapes are pinned here — including the one that must not be mistaken for the
 * other, since a reminder landing on the wrong screen is invisible until a user reports it.
 */
class DeepLinkTest {

    @Before
    fun loadCatalog() {
        OrbitData.load { path -> File("src/main/assets/$path").readText() }
    }

    @Test
    fun `links are built with the hosts the manifest declares`() {
        assertEquals("spacefacts://quiz", DeepLink.toDailyQuiz())
        assertEquals("spacefacts://fact/moon", DeepLink.toFact("moon"))
    }

    @Test
    fun `a quiz link is recognised`() {
        assertTrue(DeepLink.isQuizLink(DeepLink.SCHEME, DeepLink.HOST_QUIZ))
    }

    /** A fact link must never open the quiz, and vice versa. */
    @Test
    fun `the two link shapes do not overlap`() {
        assertFalse(DeepLink.isQuizLink(DeepLink.SCHEME, DeepLink.HOST_FACT))
        assertNull(DeepLink.factIdFrom(DeepLink.SCHEME, DeepLink.HOST_QUIZ, null))
    }

    @Test
    fun `another scheme is ignored`() {
        assertFalse(DeepLink.isQuizLink("https", DeepLink.HOST_QUIZ))
        assertFalse(DeepLink.isQuizLink(null, null))
    }

    @Test
    fun `a fact link resolves only to a fact that still ships`() {
        assertEquals("moon", DeepLink.factIdFrom(DeepLink.SCHEME, DeepLink.HOST_FACT, "moon"))
        assertNull(DeepLink.factIdFrom(DeepLink.SCHEME, DeepLink.HOST_FACT, "a_fact_we_removed"))
        assertNull(DeepLink.factIdFrom(DeepLink.SCHEME, DeepLink.HOST_FACT, ""))
    }
}
