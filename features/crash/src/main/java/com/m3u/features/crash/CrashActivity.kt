package com.m3u.features.crash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.m3u.ui.M3ULocalProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            M3ULocalProvider {
                CrashApp()
            }
        }
    }
}