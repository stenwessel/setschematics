package nl.tue.setschematics.action

import nl.tue.setschematics.state.State

class NoopAction : StateAction {
    override fun apply(state: State) = state
}
