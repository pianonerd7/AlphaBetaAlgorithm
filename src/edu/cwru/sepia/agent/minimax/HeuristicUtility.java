package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.DirectedAction;
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
		// heuristicEstimate += hpUtility();
		return heuristicEstimate;
	}

	private double cornerEnemyUtility() {

		double cornerUtility = 0.0;

		List<MapLocation> corners = getCorners();
		List<MapLocation> originalFLoc = getOriginalLocList(gameState.gAction);

		int minSize = Integer.MAX_VALUE;
		List<Stack<MapLocation>> optimalPath = new ArrayList<Stack<MapLocation>>();

		for (MapLocation loc : corners) {
			int sumSteps = 0;
			List<Stack<MapLocation>> tempPath = new ArrayList<Stack<MapLocation>>();

			for (MapLocation originalLoc : originalFLoc) {
				AStarSearch aStar = new AStarSearch();
				Stack<MapLocation> path = aStar.AstarSearch(originalLoc, loc, xExtent, yExtent, null, getResources());
				sumSteps += path.size();
				tempPath.add(path);
			}

			if (sumSteps < minSize) {
				minSize = sumSteps;
				optimalPath = tempPath;
			}
		}

		List<MapLocation> nextSteps = new ArrayList<MapLocation>();

		for (Stack<MapLocation> path : optimalPath) {

			if (path.empty()) {
				continue;
			}
			nextSteps.add(path.pop());
		}

		List<MapLocation> footmenLoc = new ArrayList<MapLocation>();

		for (Integer key : gameState.footmenLocation.keySet()) {
			footmenLoc.add(gameState.footmenLocation.get(key));
		}

		for (MapLocation loc : footmenLoc) {
			for (MapLocation path : nextSteps) {
				if (loc.x == path.x && loc.y == path.y) {
					cornerUtility += 500;
				}
			}
		}

		return cornerUtility;
	}

	private List<MapLocation> getOriginalLocList(Map<Integer, Action> actions) {

		List<MapLocation> originalLocList = new ArrayList<MapLocation>();
		List<Integer> footmenID = gameState.footmenID;
		Map<Integer, MapLocation> footmenLoc = gameState.footmenLocation;

		for (Integer id : footmenID) {
			Action act = actions.get(id);

			if (act == null) {
				continue;
			}

			MapLocation originalLoc = new MapLocation(footmenLoc.get(id).x, footmenLoc.get(id).y);

			if (act instanceof DirectedAction) {
				originalLoc = getOriginalLocation(footmenLoc.get(id), act, originalLoc);
			}

			originalLocList.add(originalLoc);
		}

		return originalLocList;
	}

	private List<MapLocation> getCorners() {

		List<MapLocation> corners = new ArrayList<MapLocation>();

		corners.add(new MapLocation(0, 0));
		corners.add(new MapLocation(xExtent, 0));
		corners.add(new MapLocation(0, yExtent));
		corners.add(new MapLocation(xExtent, yExtent));

		for (MapLocation loc : corners) {
			if (gameState.stateView.isResourceAt(loc.x, loc.y)) {
				corners.remove(loc);
			}
		}

		return corners;
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
					distUtility += aStarSearch(gameState.footmenLocation.get(gameState.footmenID.get(1)),
							gameState.archerLocation.get(key), gameState.footmenID.get(1), key);
				}
			}

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

			distUtility += aStarSearch(footman1, archer, gameState.footmenID.get(0), index);
			distUtility += aStarSearch(footman2, archer, gameState.footmenID.get(1), index);

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

			distUtility += aStarSearch(footman, archer1, index, gameState.archerID.get(0));
			distUtility += aStarSearch(footman, archer2, index, gameState.archerID.get(1));

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

	// private double hpUtility() {
	//
	// double hpUtility = 0;
	// double fFullHp = 160;
	// double aFullHp = 50;
	//
	// if (MinimaxAlphaBeta.isMaxTurn) {
	// if (gameState.footmenID.size() < 2) {
	// hpUtility -= 10000;
	// }
	// }
	//
	// double footmen = 0.0;
	// for (Integer key : gameState.footmenHP.keySet()) {
	// footmen += fFullHp - gameState.footmenHP.get(key);
	// }
	//
	// double archers = 0.0;
	// for (Integer key : gameState.archerHP.keySet()) {
	// archers += aFullHp - gameState.archerHP.get(key);
	// }
	//
	// return (archers - footmen);
	// }

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

		Map<Integer, Action> action = gameState.gAction;

		if (action == null) {
			return 0;
		}

		// clone start location
		MapLocation originalStart = new MapLocation(start.x, start.y);
		Action footmanAction = action.get(footmanKey);

		if (footmanAction instanceof DirectedAction) {
			originalStart = getOriginalLocation(start, footmanAction, originalStart);
		} else {
			originalStart = start;
		}

		AStarSearch aStar = new AStarSearch();
		Stack<MapLocation> path = aStar.AstarSearch(originalStart, goal, xExtent, yExtent, null, getResources());

		if (path.isEmpty()) {
			return 0;
		}

		// stop chasing the archer, corner them and finish them!
		if (path.size() <= 1) {
			aStarUtility += cornerEnemyUtility();
			return aStarUtility;
		}

		MapLocation nextLocation = path.pop();

		if (nextLocation.x == start.x && nextLocation.y == start.y) {
			aStarUtility = 500;
		}

		aStarUtility += penaltyForNegativeAction(footmanAction, start, nextLocation);

		return aStarUtility;
	}

	private double penaltyForNegativeAction(Action action, MapLocation start, MapLocation nextLoc) {

		double negativeActUtility = 0.0;

		if (!(action instanceof DirectedAction)) {
			return 0;
		}

		int xDiff = Math.abs(start.x - nextLoc.x);
		int yDiff = Math.abs(start.y - nextLoc.y);

		if (xDiff == 2 || yDiff == 2) {
			negativeActUtility += -1000;
		}

		return negativeActUtility;
	}

	private MapLocation getOriginalLocation(MapLocation start, Action action, MapLocation newStart) {

		int factor;

		if (gameState.depth == -1) {
			factor = 1;
		} else {
			factor = MinimaxAlphaBeta.ply - gameState.depth + 1;
		}

		if (action.toString().contains("NORTH")) {
			newStart.y += factor;
		} else if (action.toString().contains("EAST")) {
			newStart.x -= factor;
		} else if (action.toString().contains("SOUTH")) {
			newStart.y -= factor;
		} else if (action.toString().contains("WEST")) {
			newStart.x += factor;
		}

		return newStart;
	}
}
