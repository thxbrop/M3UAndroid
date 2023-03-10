package com.m3u.features.setting

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.m3u.core.annotation.ConnectTimeout
import com.m3u.core.architecture.BaseViewModel
import com.m3u.core.architecture.Configuration
import com.m3u.core.architecture.Packager
import com.m3u.core.wrapper.Resource
import com.m3u.core.wrapper.eventOf
import com.m3u.data.repository.FeedRepository
import com.m3u.data.repository.LiveRepository
import com.m3u.data.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val liveRepository: LiveRepository,
    private val remoteRepository: RemoteRepository,
    packager: Packager,
    application: Application,
    private val configuration: Configuration
) : BaseViewModel<SettingState, SettingEvent>(
    application = application,
    emptyState = SettingState()
) {
    init {
        writable.update {
            it.copy(
                version = packager.versionName,
                feedStrategy = configuration.feedStrategy,
                useCommonUIMode = configuration.useCommonUIMode,
                experimentalMode = configuration.experimentalMode,
                editMode = configuration.editMode,
                connectTimeout = configuration.connectTimeout,
                clipMode = configuration.clipMode,
                scrollMode = configuration.scrollMode
            )
        }
        viewModelScope.launch(Dispatchers.Default) {
            val newerMutedUrls = configuration.mutedUrls.toMutableList()
            val lives = newerMutedUrls.mapNotNull { url -> liveRepository.getByUrl(url) }
            configuration.mutedUrls = lives.map { it.url }
            writable.update {
                it.copy(
                    mutedLives = lives
                )
            }
        }
        fetchLatestRelease()
    }

    override fun onEvent(event: SettingEvent) {
        when (event) {
            SettingEvent.OnSubscribe -> subscribe()
            SettingEvent.FetchLatestRelease -> fetchLatestRelease()
            is SettingEvent.OnTitle -> onTitle(event.title)
            is SettingEvent.OnUrl -> onUrl(event.url)
            is SettingEvent.OnSyncMode -> {
                val newValue = event.feedStrategy
                configuration.feedStrategy = newValue
                writable.update {
                    it.copy(
                        feedStrategy = newValue
                    )
                }
            }
            SettingEvent.OnUseCommonUIMode -> {
                val newValue = !configuration.useCommonUIMode
                configuration.useCommonUIMode = newValue
                writable.update {
                    it.copy(
                        useCommonUIMode = newValue
                    )
                }
            }
            SettingEvent.OnConnectTimeout -> {
                val newValue = when (configuration.connectTimeout) {
                    ConnectTimeout.LONG -> ConnectTimeout.SHORT
                    ConnectTimeout.SHORT -> ConnectTimeout.LONG
                    else -> ConnectTimeout.SHORT
                }
                configuration.connectTimeout = newValue
                writable.update {
                    it.copy(
                        connectTimeout = newValue
                    )
                }
            }
            SettingEvent.OnEditMode -> {
                val newValue = !configuration.editMode
                configuration.editMode = newValue
                writable.update {
                    it.copy(
                        editMode = newValue
                    )
                }
            }
            SettingEvent.OnExperimentalMode -> {
                val newValue = !configuration.experimentalMode
                if (!newValue) {
                    // reset experimental ones to default value
                    configuration.scrollMode = Configuration.DEFAULT_SCROLL_MODE
                    writable.update {
                        it.copy(
                            scrollMode = Configuration.DEFAULT_SCROLL_MODE
                        )
                    }
                }
                configuration.experimentalMode = newValue
                writable.update {
                    it.copy(
                        experimentalMode = newValue
                    )
                }
            }
            is SettingEvent.OnClipMode -> {
                val newValue = event.mode
                configuration.clipMode = newValue
                writable.update {
                    it.copy(
                        clipMode = newValue
                    )
                }
            }
            is SettingEvent.OnVoiceLiveUrl -> {
                configuration.mutedUrls -= event.url
                writable.update { readable ->
                    val lives = readable.mutedLives.toMutableList()
                    lives.removeIf { it.url == event.url }
                    readable.copy(
                        mutedLives = lives
                    )
                }
            }
            SettingEvent.OnScrollMode -> {
                val target = !configuration.scrollMode
                configuration.scrollMode = target
                writable.update {
                    it.copy(
                        scrollMode = target
                    )
                }
            }
        }
    }

    private fun onTitle(title: String) {
        writable.update {
            it.copy(
                title = Uri.decode(title)
            )
        }
    }

    private fun onUrl(url: String) {
        writable.update {
            it.copy(
                url = Uri.decode(url)
            )
        }
    }

    private fun subscribe() {
        val title = writable.value.title
        if (title.isEmpty()) {
            writable.update {
                val message = context.getString(R.string.failed_empty_title)
                it.copy(
                    adding = false,
                    message = eventOf(message)
                )
            }
            return
        }
        val url = readable.url
        val strategy = configuration.feedStrategy
        feedRepository.subscribe(title, url, strategy)
            .onEach { resource ->
                writable.update {
                    when (resource) {
                        Resource.Loading -> {
                            it.copy(adding = true)
                        }
                        is Resource.Success -> {
                            val message = context.getString(R.string.success_subscribe)
                            it.copy(
                                adding = false,
                                title = "",
                                url = "",
                                message = eventOf(message)
                            )
                        }
                        is Resource.Failure -> {
                            it.copy(
                                adding = false,
                                message = eventOf(resource.message.orEmpty())
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchLatestRelease() {
        remoteRepository.fetchLatestRelease()
            .onEach { resource ->
                writable.update {
                    it.copy(
                        latestRelease = resource
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}