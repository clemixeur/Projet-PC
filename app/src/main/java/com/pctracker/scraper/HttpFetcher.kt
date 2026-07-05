package com.pctracker.scraper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Single entry point for all outbound scraping requests. Requests are always processed one
 * at a time (the worker never fetches in parallel) and this adds a randomized 5-9s pause
 * before every request, so a full cycle stays slow and unremarkable to the target sites
 * rather than bursting - the goal is to stay well under any reasonable rate limit, not to
 * defeat anti-bot detection.
 */
object HttpFetcher {

    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    private const val MIN_DELAY_MS = 5000L
    private const val EXTRA_DELAY_MS = 4000L

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val mutex = Mutex()
    private var lastRequestAt: Long = 0L

    suspend fun fetch(url: String): Result<String> {
        respectRateLimit()
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Language", "fr-FR,fr;q=0.9,en;q=0.5")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string()
                    when {
                        !response.isSuccessful -> Result.failure(IOException("HTTP ${response.code}"))
                        body.isNullOrBlank() -> Result.failure(IOException("Reponse vide"))
                        else -> Result.success(body)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun respectRateLimit() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastRequestAt
            val minGap = MIN_DELAY_MS + Random.nextLong(EXTRA_DELAY_MS)
            if (lastRequestAt != 0L && elapsed < minGap) {
                delay(minGap - elapsed)
            }
            lastRequestAt = System.currentTimeMillis()
        }
    }
}
