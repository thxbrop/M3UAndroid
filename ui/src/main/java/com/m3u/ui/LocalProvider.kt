package com.m3u.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.m3u.ui.model.*

@Composable
fun M3ULocalProvider(
    helper: Helper = EmptyHelper,
    content: @Composable () -> Unit
) {
    val theme = if (isSystemInDarkTheme()) NightTheme
    else DayTheme
    CompositionLocalProvider(
        LocalTheme provides theme,
        LocalHelper provides helper
    ) {
        MaterialTheme(
            typography = Typography,
            content = content
        )
    }
}