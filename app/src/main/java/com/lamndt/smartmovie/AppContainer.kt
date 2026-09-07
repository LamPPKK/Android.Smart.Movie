package com.lamndt.smartmovie

import android.content.Context
import android.content.res.Configuration
import com.lamndt.smartmovie.data.DefaultCatalogRepository
import com.lamndt.smartmovie.data.DefaultLibraryRepository
import com.lamndt.smartmovie.data.DefaultEpisodeProgressRepository
import com.lamndt.smartmovie.data.DurableAccountMutationOutbox
import com.lamndt.smartmovie.data.ImageUrlFactory
import com.lamndt.smartmovie.database.SmartMovieDatabase
import com.lamndt.smartmovie.model.CatalogRepository
import com.lamndt.smartmovie.model.AccountRepository
import com.lamndt.smartmovie.model.CatalogV2Repository
import com.lamndt.smartmovie.model.CapabilitiesV2
import com.lamndt.smartmovie.model.CatalogLocale
import com.lamndt.smartmovie.model.ImageConfiguration
import com.lamndt.smartmovie.model.LibraryRepository
import com.lamndt.smartmovie.model.LibrarySyncRepository
import com.lamndt.smartmovie.model.supportsAccountAuthentication
import com.lamndt.smartmovie.network.AccountNetworkRepository
import com.lamndt.smartmovie.network.CatalogNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class AppContainer(context: Context, baseUrl: String) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isTelevision = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
        Configuration.UI_MODE_TYPE_TELEVISION
    private val accountCapabilityGate = AccountCapabilityGate(isTelevision)
    private val database = SmartMovieDatabase.create(context)
    private val network = CatalogNetworkDataSource(context, baseUrl)
    val catalog: CatalogV2Repository = DefaultCatalogRepository(network)
    val library: LibrarySyncRepository = DefaultLibraryRepository(database)
    val episodeProgress = DefaultEpisodeProgressRepository(database)
    val account: AccountRepository = AccountNetworkRepository(context, baseUrl)
    val accountOutbox = DurableAccountMutationOutbox(database, account)
    val preferences = CatalogPreferences(context)
    val accountSession = AccountSessionController(account, library, accountOutbox, applicationScope)
    val watchRemote: PhoneWatchRemoteController by lazy {
        PhoneWatchRemoteController(context, library, applicationScope)
    }

    private val imageConfiguration = MutableStateFlow(ImageConfiguration.Fallback)
    private val mutableCapabilities = MutableStateFlow<CapabilitiesV2?>(null)
    val capabilities = mutableCapabilities.asStateFlow()
    val images = ImageUrlFactory(imageConfiguration::value)

    init {
        applicationScope.launch {
            runCatching { catalog.imageConfiguration() }
                .onSuccess { imageConfiguration.value = it }
        }
        applicationScope.launch {
            val resolved = runCatching { catalog.capabilities() }.getOrNull()
            mutableCapabilities.value = resolved
            accountCapabilityGate.resolve(
                resolved,
                onEnabled = accountSession::enable,
                onDisabled = accountSession::disable,
                onRefresh = {
                    accountSession.refresh(
                        CatalogLocale.from(
                            java.util.Locale.getDefault().language,
                            java.util.Locale.getDefault().country,
                        ),
                    )
                },
                onCallback = accountSession::handleCallback,
            )
        }
        applicationScope.launch {
            while (isActive) {
                delay(30_000)
                accountSession.flushOutbox()
            }
        }
    }

    fun handleAuthCallback(attemptId: String, language: String) {
        accountCapabilityGate.submit(attemptId, language, accountSession::handleCallback)
    }
}

internal class AccountCapabilityGate(private val isTelevision: Boolean) {
    private val lock = Any()
    private var resolved = false
    private var enabled = false
    private var pendingCallback: Pair<String, String>? = null

    fun resolve(
        capabilities: CapabilitiesV2?,
        onEnabled: () -> Unit,
        onDisabled: () -> Unit,
        onRefresh: () -> Unit,
        onCallback: (String, String) -> Unit,
    ) {
        val callback = synchronized(lock) {
            enabled = capabilities.supportsAccountAuthentication(isTelevision)
            if (enabled) onEnabled() else onDisabled()
            val pending = pendingCallback.takeIf { enabled }
            pendingCallback = null
            if (enabled && pending == null) onRefresh()
            resolved = true
            pending
        }
        callback?.let { (attemptId, language) -> onCallback(attemptId, language) }
    }

    fun submit(attemptId: String, language: String, onCallback: (String, String) -> Unit) {
        val shouldComplete = synchronized(lock) {
            if (!resolved) {
                pendingCallback = attemptId to language
                false
            } else {
                enabled
            }
        }
        if (shouldComplete) onCallback(attemptId, language)
    }
}
