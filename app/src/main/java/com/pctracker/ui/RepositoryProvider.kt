package com.pctracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pctracker.PCTrackerApp
import com.pctracker.data.repository.PriceRepository

@Composable
fun rememberRepository(): PriceRepository {
    val context = LocalContext.current.applicationContext as PCTrackerApp
    return context.repository
}
