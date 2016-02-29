package edu.cwru.sepia.agent.minimax;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.ResourceNode;

public class HeuristicUtility {

	private GameState gameState;
	private int xExtent;
	private int yExtent;

	public HeuristicUtility(GameState state) {
		this.gameState = state;
		xExtent = gameState.stateView.getXExtent();
		yExtent = gameState.stateView.getYExtent();
	}

	public double getHeuristic() {

		double heuristicEstimate = 0.0;
		heuristicEstimate += distanceUtility();
		heuristicEstimate += hpUtility();
		// System.out.println(heuristicEstimate);
		return heuristicEstimate;
	}

	private double distanceUtility() {

		double distUtility = 0.0;
		int numFootmen = gameState.footmenLocation.size();
		int numArchers = gameState.archerLocation.size();

		if (numFootmen == 2 && numArchers == 2) {
			MapLocation footman1 = gameState.footmenLocation.get(gameState.footmenID.get(0));

			double tempBest = Double.POSITIVE_INFINITY;
			int index = -1;

			for (Integer key : gameState.archerLocation.keySet()) {
				double temp = getDistance(footman1, gameState.archerLocation.get(key));

				if (temp < tempBest) {
					tempBest = temp;
					index = key;
				}
			}

			// distUtility += tempBest;
			distUtility += aStarSearch(footman1, gameState.archerLocation.get(index), gameState.footmenID.get(0),
					index);

			for (Integer key : gameState.archerLocation.keySet()) {

				if (key != index) {
					// distUtility +=
					// getDistance(gameState.footmenLocation.get(gameState.footmenID.get(1)),
					// gameState.archerLocation.get(key));

					distUtility += aStarSearch(gameState.footmenLocation.get(gameState.footmenID.get(1)),
							gameState.archerLocation.get(key), gameState.footmenID.get(1), key);
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
			int index = -1;

			for (Integer key : gameState.archerLocation.keySet()) {

				MapLocation temp = gameState.archerLocation.get(key);

				if (temp != null) {
					archer = temp;
					index = key;
				}
			}

			// distUtility += getDistance(footman1, archer);
			// distUtility += getDistance(footman2, archer);

			distUtility += aStarSearch(footman1, archer, gameState.footmenID.get(0), index);
			distUtility += aStarSearch(footman2, archer, gameState.footmenID.get(1), index);

			// add bonus for killing one archer and keeping both agent alive
			// distUtility += 200;

		} else if (numFootmen == 1 && numArchers == 2) {
			MapLocation archer1 = gameState.archerLocation.get(gameState.archerID.get(0));
			MapLocation archer2 = gameState.archerLocation.get(gameState.archerID.get(1));
			MapLocation footman = null;
			int index = -1;

			for (Integer key : gameState.footmenLocation.keySet()) {

				MapLocation temp = gameState.footmenLocation.get(key);

				if (temp != null) {
					footman = temp;
					index = key;
				}
			}

			// distUtility += getDistance(archer1, footman);
			// distUtility += getDistance(archer2, footman);

			distUtility += aStarSearch(footman, archer1, index, gameState.archerID.get(0));
			distUtility += aStarSearch(footman, archer2, index, gameState.archerID.get(1));
			// distUtility -= 200;

		} else if (numFootmen == 1 && numArchers == 1) {
			MapLocation footman = null;
			MapLocation archer = null;
			int fIndex = -1;
			int aIndex = -1;

			for (Integer key : gameState.footmenLocation.keySet()) {

				MapLocation temp = gameState.footmenLocation.get(key);

				if (temp != null) {
					footman = temp;
					fIndex = key;
				}
			}

			for (Integer key : gameState.archerLocation.keySet()) {

				MapLocation temp = gameState.archerLocation.get(key);

				if (temp != null) {
					archer = temp;
					aIndex = key;
				}
			}

			distUtility += aStarSearch(footman, archer, fIndex, aIndex);
			// distUtility += getDistance(archer, footman);
		}

		return distUtility;
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

	private double hpUtility() {

		double hpUtility = 0;
		double fFullHp = 160;
		double aFullHp = 50;

		if (MinimaxAlphaBeta.isMaxTurn) {
			if (gameState.footmenID.size() < 2) {
				hpUtility -= 10000;
			}
		}

		double footmen = 0.0;
		for (Integer key : gameState.footmenHP.keySet()) {
			footmen += fFullHp - gameState.footmenHP.get(key);
		}

		double archers = 0.0;
		for (Integer key : gameState.archerHP.keySet()) {
			archers += aFullHp - gameState.archerHP.get(key);
		}

		return (archers - footmen);
	}

	private Set<MapLocation> getResources() {

		List<Integer> resourceIDs = gameState.stateView.getAllResourceIds();
		Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = gameState.stateView.getResourceNode(resourceID);

			resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition()));
		}

		return resourceLocations;
	}

	private double aStarSearch(MapLocation start, MapLocation goal, int footmanKey, int archerKey) {

		double aStarUtility = 0.0;

		AStarSearch aStar = new AStarSearch();
		Stack<MapLocation> path = aStar.AstarSearch(start, goal, xExtent, yExtent, null, getResources());

		Map<Integer, Action> action = gameState.gAction;
		MapLocation nextLocation = path.pop();

		if (action == null) {
			return 0;
		}

		System.out.println("begin h action print");
		for (Integer key : action.keySet()) {
			System.out.println(action.get(key).toString());
		}

		System.out.println("\n");

		// clone start location
		MapLocation hypotheticalLoc = new MapLocation(start.x, start.y);

		Action footmanAction = action.get(footmanKey);

		if (footmanAction.toString().contains("NORTH")) {
			hypotheticalLoc.y -= 1;
		} else if (footmanAction.toString().contains("EAST")) {
			hypotheticalLoc.x += 1;
		} else if (footmanAction.toString().contains("SOUTH")) {
			hypotheticalLoc.y += 1;
		} else {// WEST
			hypotheticalLoc.x -= 1;
		}

		if (nextLocation.x == hypotheticalLoc.x && nextLocation.y == hypotheticalLoc.y) {
			aStarUtility = 500;
		}

		return aStarUtility;
	}
}
