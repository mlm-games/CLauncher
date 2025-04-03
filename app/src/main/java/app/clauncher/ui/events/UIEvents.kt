package app.clauncher.ui.events

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed class UiEvent {
    object NavigateToAppDrawer : UiEvent()
    object NavigateToSettings : UiEvent()
    object NavigateToHiddenApps : UiEvent()
    object NavigateBack : UiEvent()

    data class ShowDialog(val dialogType: String) : UiEvent()

    data class LaunchApp(val appId: String) : UiEvent()
    object CheckForMessages : UiEvent()
    object ResetLauncher : UiEvent()

    data class ShowToast(val message: String) : UiEvent()
}

// Class to manage events
class EventsManager {
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun emitEvent(event: UiEvent) {
        _events.send(event)
    }
}