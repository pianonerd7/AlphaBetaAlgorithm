package edu.cwru.sepia.agent.minimax;

import edu.cwru.sepia.agent.minimax.GameState.MapLocation;

public class HeuristicUtility {

	private GameState gameState;

	public HeuristicUtility(GameState state) {
		this.gameState = state;
	}

	public double getHeuristic() {
		System.out.println("in heuristic " + distanceUtility());
		return distanceUtility();
	}

	private double distanceUtility() {

		double distUtility = 0.0;
		int numFootmen = gameState.footmenLocation.size();
		int numArchers = gameState.archerLocation.size();

		if (numFootmen == 2 && numArchers == 2) {
			MapLocation footman1 = gameState.footmenLocation.get(gameState.footmenID.get(0));

			double tempBest = Double.MAX_VALUE;
			int index = -1;

			for (Integer key : gameState.archerLocation.keySet()) {
				double temp = getDistance(footman1, gameState.archerLocation.get(key));

				if (temp < tempBest) {
					tempBest = temp;
					index = key;
				}
			}

			distUtility += tempBest;
			for (Integer key : gameState.archerLocation.keySet()) {

				if (key != index) {
					distUtility += getDistance(gameState.footmenLocation.get(gameState.footmenID.get(1)),
							gameState.archerLocation.get(key));
				}
			}

			// add bonus for keeping both agents alive
			// if (MinimaxAlphaBeta.isMaxTurn) {
			// distUtility += 100;
			// } else {
			// distUtility -= 100;
			// }

		} else if (numFootmen == 2 && numArchers == 1) {
			MapLocation footman1 = gameState.footmenLocation.get(gameState.footmenID.get(0));
			MapLocation footman2 = gameState.footmenLocation.get(gameState.footmenID.get(1));
			MapLocation archer = null;

			for (Integer key : gameState.archerLocation.keySet()) {

				MapLocation temp = gameState.archerLocation.get(key);

				if (temp != null) {
					archer = temp;
				}
			}

			distUtility += getDistance(footman1, archer);
			distUtility += getDistance(footman2, archer);

			// add bonus for killing one archer and keeping both agent alive
			// distUtility += 200;

		} else if (numFootmen == 1 && numArchers == 2) {
			MapLocation archer1 = gameState.archerLocation.get(gameState.archerID.get(0));
			MapLocation archer2 = gameState.archerLocation.get(gameState.archerID.get(1));
			MapLocation footman = null;

			for (Integer key : gameState.footmenLocation.keySet()) {

				MapLocation temp = gameState.footmenLocation.get(key);

				if (temp != null) {
					footman = temp;
				}
			}

			distUtility += getDistance(archer1, footman);
			distUtility += getDistance(archer2, footman);

			// distUtility -= 200;

		} else if (numFootmen == 1 && numArchers == 1) {
			MapLocation footman = null;
			MapLocation archer = null;

			for (Integer key : gameState.footmenLocation.keySet()) {

				MapLocation temp = gameState.footmenLocation.get(key);

				if (temp != null) {
					footman = temp;
				}
			}

			for (Integer key : gameState.archerLocation.keySet()) {

				MapLocation temp = gameState.archerLocation.get(key);

				if (temp != null) {
					archer = temp;
				}
			}

			distUtility += getDistance(archer, footman);
		}

		// // footmen's turn - get as high utility as possible
		// if (MinimaxAlphaBeta.isMaxTurn) {
		// // more distance = less utility.
		// // i want to reward more for less distance
		// gameState.utility += (500 - distUtility);
		// }
		// // archer's turn - get as low utility as possible
		// else {
		// // lower utility means good
		// gameState.utility += distUtility;
		// }

		return distUtility * -2;
	}

	private double getDistance(MapLocation me, MapLocation enemy) {

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
