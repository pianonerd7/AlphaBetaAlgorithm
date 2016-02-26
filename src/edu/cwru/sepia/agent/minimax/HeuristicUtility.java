package edu.cwru.sepia.agent.minimax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.DistanceMetrics;

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
		extractUnitInfo();
	}

	public double getHeuristic() {

		return distanceBetween();
	}

	private void extractUnitInfo() {

		for (Unit.UnitView archer : archers) {
			archersHP.put(archer.getID(), archer.getHP());
		}
		for (Unit.UnitView footman : footmen) {
			footmenHP.put(footman.getID(), footman.getHP());
		}
	}

	private double distanceBetween() {

		double max = Double.MIN_VALUE;

		for (Unit.UnitView footman : footmen) {
			for (Unit.UnitView archer : archers) {

				double newVal = DistanceMetrics.euclideanDistance(footman.getXPosition(), footman.getYPosition(),
						archer.getXPosition(), archer.getYPosition());
				if (newVal > max) {
					max = newVal;
				}
			}
		}
		return 100 - max;
	}

}
