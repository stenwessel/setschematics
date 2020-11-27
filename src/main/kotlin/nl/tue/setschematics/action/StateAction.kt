package nl.tue.setschematics.action

import nl.tue.setschematics.state.State

/**
 * Action that modifies the state.
 *
 * When the state is actually modified, be sure to return a new instance of [State]!
 */
interface StateAction {
    fun apply(state: State): State
}
