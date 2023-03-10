@file:Suppress("unused")

package com.m3u.data.di

import com.m3u.core.architecture.Logger
import com.m3u.data.util.FileLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LoggerModule {
    @Binds
    @Singleton
    fun bindLogger(logger: FileLogger): Logger
}