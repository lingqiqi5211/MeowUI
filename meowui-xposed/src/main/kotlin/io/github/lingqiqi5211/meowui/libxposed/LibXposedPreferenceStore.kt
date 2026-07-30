package io.github.lingqiqi5211.meowui.libxposed

import io.github.libxposed.api.XposedModule
import io.github.lingqiqi5211.meowui.core.preference.PreferenceStore
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Preference store for the module settings process.
 *
 * The store follows [io.github.libxposed.service.XposedServiceHelper] connections and becomes
 * disconnected as soon as the active service dies. Call [close] when the owning component is
 * permanently destroyed.
 */
class XposedServicePreferenceStore private constructor(
    private val delegate: ConnectionBackedPreferenceStore,
) : PreferenceStore by delegate, AutoCloseable {
    constructor(preferenceName: String) : this(
        ConnectionBackedPreferenceStore(
            XposedServiceRemotePreferencesConnection(preferenceName),
        ),
    )

    override fun close() = delegate.close()
}

/** Preference store backed by [XposedModule.getRemotePreferences] in a hooked process. */
class XposedModulePreferenceStore private constructor(
    private val delegate: ConnectionBackedPreferenceStore,
) : PreferenceStore by delegate, AutoCloseable {
    constructor(
        module: XposedModule,
        preferenceName: String,
    ) : this(
        ConnectionBackedPreferenceStore(
            FixedRemotePreferencesConnection(module.remotePreferencesSnapshot(preferenceName)),
        ),
    )

    override fun close() = delegate.close()
}

/** Creates a typed MeowUI preference store in a hooked process. */
fun XposedModule.createPreferenceStore(
    preferenceName: String,
): XposedModulePreferenceStore = XposedModulePreferenceStore(this, preferenceName)

private fun XposedModule.remotePreferencesSnapshot(
    preferenceName: String,
): RemotePreferencesSnapshot {
    require(preferenceName.isNotBlank()) { "Preference name must not be blank." }

    return runCatching {
        RemotePreferencesSnapshot(getRemotePreferences(preferenceName))
    }.getOrElse { error ->
        RemotePreferencesSnapshot(
            preferences = null,
            unavailableCause = error,
        )
    }
}

internal class ConnectionBackedPreferenceStore(
    connection: RemotePreferencesConnection,
    private val adapter: CorePreferenceStoreAdapter = CorePreferenceStoreAdapter(),
) : PreferenceStore by adapter, AutoCloseable {
    private val closed = AtomicBoolean(false)
    private val subscription = connection.subscribe { snapshot ->
        if (!closed.get()) {
            adapter.updateConnection(snapshot)
        }
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        subscription.close()
        adapter.close()
    }
}
