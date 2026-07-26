package io.github.lingqiqi5211.meowui.libxposed

import android.content.SharedPreferences
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

internal data class RemotePreferencesSnapshot(
    val preferences: SharedPreferences?,
    val unavailableCause: Throwable? = null,
)

internal fun interface ConnectionSubscription : AutoCloseable {
    override fun close()
}

internal fun interface RemotePreferencesConnection {
    fun subscribe(listener: (RemotePreferencesSnapshot) -> Unit): ConnectionSubscription
}

internal class XposedServiceRemotePreferencesConnection(
    private val preferenceName: String,
    private val serviceRegistry: XposedServiceRegistry = ProcessXposedServiceRegistry,
) : RemotePreferencesConnection {
    init {
        require(preferenceName.isNotBlank()) { "Preference name must not be blank." }
    }

    override fun subscribe(
        listener: (RemotePreferencesSnapshot) -> Unit,
    ): ConnectionSubscription = serviceRegistry.subscribe { service ->
        if (service == null) {
            listener(RemotePreferencesSnapshot(preferences = null))
            return@subscribe
        }

        val snapshot = runCatching {
            RemotePreferencesSnapshot(service.getRemotePreferences(preferenceName))
        }.getOrElse { error ->
            RemotePreferencesSnapshot(
                preferences = null,
                unavailableCause = error,
            )
        }
        listener(snapshot)
    }
}

internal class FixedRemotePreferencesConnection(
    private val snapshot: RemotePreferencesSnapshot,
) : RemotePreferencesConnection {
    override fun subscribe(
        listener: (RemotePreferencesSnapshot) -> Unit,
    ): ConnectionSubscription {
        listener(snapshot)
        return ConnectionSubscription {}
    }
}

internal fun interface XposedServiceRegistry {
    fun subscribe(listener: (XposedService?) -> Unit): ConnectionSubscription
}

private object ProcessXposedServiceRegistry :
    XposedServiceRegistry,
    XposedServiceHelper.OnServiceListener {
    private val lock = Any()
    private val services = mutableListOf<XposedService>()
    private val listeners = linkedSetOf<(XposedService?) -> Unit>()
    private var activeService: XposedService? = null

    init {
        XposedServiceHelper.registerListener(this)
    }

    override fun subscribe(listener: (XposedService?) -> Unit): ConnectionSubscription {
        val currentService = synchronized(lock) {
            listeners += listener
            activeService
        }
        notifyListener(listener, currentService)

        return ConnectionSubscription {
            synchronized(lock) {
                listeners -= listener
            }
        }
    }

    override fun onServiceBind(service: XposedService) {
        val listenersSnapshot = synchronized(lock) {
            services.removeAll { it === service }
            services += service
            activeService = service
            listeners.toList()
        }
        notifyListeners(listenersSnapshot, service)
    }

    override fun onServiceDied(service: XposedService) {
        val update = synchronized(lock) {
            services.removeAll { it === service }
            if (activeService !== service) return

            activeService = services.lastOrNull()
            listeners.toList() to activeService
        }
        notifyListeners(update.first, update.second)
    }

    private fun notifyListeners(
        listeners: List<(XposedService?) -> Unit>,
        service: XposedService?,
    ) {
        listeners.forEach { listener -> notifyListener(listener, service) }
    }

    private fun notifyListener(
        listener: (XposedService?) -> Unit,
        service: XposedService?,
    ) {
        try {
            listener(service)
        } catch (_: Throwable) {
            // One consumer must not prevent other stores from receiving a connection update.
        }
    }
}
