package com.orbit.starsystems.core

import android.content.Intent
import android.net.Uri

/**
 * The `spacefacts://fact/<id>` links that point back into the catalog, built by the daily
 * reminder and by the caption on a shared fact image, and parsed by MainActivity.
 *
 * One place so the three never drift: a link that does not match the manifest's intent
 * filter fails silently, with nothing to see at either end.
 */
object DeepLink {

    const val SCHEME = "spacefacts"
    const val HOST_FACT = "fact"

    fun toFact(factId: String): String = "$SCHEME://$HOST_FACT/$factId"

    /** The fact named by [intent], if it is one of our links and the fact still exists. */
    fun factIdFrom(intent: Intent?): String? = factIdFrom(intent?.data)

    fun factIdFrom(uri: Uri?): String? {
        if (uri == null || uri.scheme != SCHEME || uri.host != HOST_FACT) return null
        val id = uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: return null
        return id.takeIf { factById(it) != null }
    }
}
