package io.github.lingqiqi5211.meowui.libxposed

import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/**
 * Access to the Xposed framework service bound in this process.
 *
 * Do **not** call [XposedServiceHelper.registerListener] yourself. It keeps a single listener slot
 * and overwrites it, and the first registrant drains the pending-binder cache — so a second
 * registrant displaces the first and still receives nothing. The usual symptom is that remote
 * preferences quietly stop connecting while the newcomer appears to work.
 *
 * MeowUI already registers exactly once. Everything else in the app, including anything the host
 * app needs the service for, goes through here.
 */
object MeowXposedService {

    /** The currently bound service, or `null` while the framework is not connected. */
    val current: XposedService?
        get() = ProcessXposedServiceRegistry.currentService()

    /**
     * Observe connection changes. The listener is invoked immediately with the current state and
     * again on every change. Close the returned subscription when it is no longer needed.
     */
    fun subscribe(listener: (XposedService?) -> Unit): AutoCloseable =
        ProcessXposedServiceRegistry.subscribe(listener)
}
