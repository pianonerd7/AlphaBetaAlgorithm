package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.agent.minimax.GameState.MapLocation;

public class HeuristicUtility {

	private GameState gameState;

	public HeuristicUtility(GameState state) {
		this.gameState = state;
	}

	public double getHeuristic() {
		return distanceBetween();
	}

	private double distanceBetween() {

		double min = Double.MAX_VALUE;

		for (Integer fkey : gameState.footmenLocation.keySet()) {
			for (Integer akey : gameState.archerLocation.keySet()) {

				double dist = minDistance(gameState.footmenLocation.get(fkey), gameState.archerLocation.get(akey));

				if (dist < min) {
					min = dist;
				}
			}
		}
		return 100 - min;
	}

	private double minDistance(MapLocation me, MapLocation enemy) {

		int me_x = me.x;
		int me_y = me.y;
		int enemy_x = enemy.x;
		int enemy_y = enemy.y;

		double a = Math.pow(enemy_x - me_x, 2);
		double b = Math.pow(enemy_y - me_y, 2);
		double c = Math.sqrt(a + b);

		return c;
	}
}
