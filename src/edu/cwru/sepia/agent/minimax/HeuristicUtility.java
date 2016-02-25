package edu.cwru.sepia.agent.minimax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

public class HeuristicUtility {

	private State.StateView state;
	private List<Unit.UnitView> archers;
	private List<Unit.UnitView> footmen;

	// a Map of <Unit.ID, HP> for each archer/footman
	private Map<Integer, Integer> archersHP = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> footmenHP = new HashMap<Integer, Integer>();

	public HeuristicUtility(State.StateView state, List<Unit.UnitView> archers, List<Unit.UnitView> footmen) {
		this.state = state;
		this.archers = archers;
		this.footmen = footmen;

	}

	private void extractUnitInfo() {

		for (Unit.UnitView archer : archers) {
			archersHP.put(archer.getID(), archer.getHP());
		}
		for (Unit.UnitView footman : footmen) {
			footmenHP.put(footman.getID(), footman.getHP());
		}
	}

	public double getHeuristic() {

		return 0.00;
	}
}
