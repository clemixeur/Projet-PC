package com.pctracker.domain

import java.util.concurrent.TimeUnit

/**
 * Computes the default "bonne affaire" threshold as the average best-config total observed
 * during the first 7 days of tracking, minus a safety margin. Returns null while that
 * 7-day window is not complete yet, so callers should keep whatever threshold is already
 * set (seed default or user-edited) until enough history exists.
 */
object ThresholdCalculator {
    private val WINDOW_MILLIS = TimeUnit.DAYS.toMillis(7)
    const val DEFAULT_MARGIN_PERCENT = 9.0

    fun computeAutoThreshold(
        firstTrackedAtMillis: Long,
        dailyTotalsInWindow: List<Double>,
        nowMillis: Long,
        marginPercent: Double = DEFAULT_MARGIN_PERCENT
    ): Double? {
        val windowElapsed = nowMillis - firstTrackedAtMillis >= WINDOW_MILLIS
        if (!windowElapsed || dailyTotalsInWindow.isEmpty()) return null
        val average = dailyTotalsInWindow.average()
        return average * (1 - marginPercent / 100.0)
    }
}
