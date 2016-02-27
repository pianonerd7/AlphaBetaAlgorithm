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

		double min = Double.MAX_VALUE;

		for (Unit.UnitView footman : footmen) {
			for (Unit.UnitView archer : archers) {

				double newVal = minDistance(footman, archer);
				if (newVal < min) {
					min = newVal;
				}
			}
		}
		return 100 - min;
	}

	private double minDistance(Unit.UnitView me, Unit.UnitView enemy) {

		int me_x = me.getXPosition();
		int me_y = me.getYPosition();
		int enemy_x = enemy.getXPosition();
		int enemy_y = enemy.getYPosition();

		double a = Math.pow(enemy_x - me_x, 2);
		double b = Math.pow(enemy_y - me_y, 2);
		double c = Math.sqrt(a + b);

		return c;
	}
}
