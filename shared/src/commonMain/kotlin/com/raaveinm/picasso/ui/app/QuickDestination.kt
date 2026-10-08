package com.raaveinm.picasso.ui.app

/**
 * Screens the host window (desktop menu bar) can ask [com.raaveinm.picasso.App] to jump to.
 * App owns the NavController, so the platform entry point only names the destination.
 */
enum class QuickDestination {
    Library,
    Chat,
    Friends,
    ServerSettings,
    ApplicationSettings,
    VisualSettings,
    BehaviourSettings
}
