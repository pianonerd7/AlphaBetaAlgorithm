package edu.cwru.sepia.agent.minimax;

import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State;

/**
 * Do not change this class.
 */
public class GameStateChild {
	// * This is set of unit actions that produced the game state
	public Map<Integer, Action> action;
	// * This is the game state resulting from the specified set of actions
	public GameState state;

	public GameStateChild(State.StateView state) {
		action = null;
		this.state = new GameState(state);
	}

	public GameStateChild(Map<Integer, Action> action, GameState state) {
		this.action = action;
		this.state = state;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Integer key : action.keySet()) {
			sb.append(action.get(key) + "\n");
		}

		return sb.toString();
	}
}
