package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.HashMap;
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
		return heuristicEstimate;
	}

	private double blockUtility() {

		double blockUtil = 0.0;

		Map<Integer, MapLocation> archerLoc = gameState.archerLocation;

		List<ArrayList<MapLocation>> openPositionList = new ArrayList<ArrayList<MapLocation>>();

		for (Integer key : archerLoc.keySet()) {

			ArrayList<MapLocation> openPositions = new ArrayList<MapLocation>();

			MapLocation temp = archerLoc.get(key);
			openPositions.add(new MapLocation(temp.x, temp.y - 1));
			openPositions.add(new MapLocation(temp.x, temp.y + 1));
			openPositions.add(new MapLocation(temp.x - 1, temp.y));
			openPositions.add(new MapLocation(temp.x + 1, temp.y));

			openPositionList.add(openPositions);
		}

		List<ArrayList<MapLocation>> newList = new ArrayList<ArrayList<MapLocation>>();
		for (ArrayList<MapLocation> list : openPositionList) {

			ArrayList<MapLocation> newLocs = new ArrayList<MapLocation>();
			for (MapLocation loc : list) {
				if (gameState.stateView.isResourceAt(loc.x, loc.y) || !gameState.stateView.inBounds(loc.x, loc.y)) {
					continue;
				}
				newLocs.add(loc);
			}
			newList.add(newLocs);
		}

		for (ArrayList<MapLocation> list : newList) {
			if (list.size() < 3) {

				MapLocation bestFootman = getClosestFootman(list.get(0));
				MapLocation nextBest = null;

				if (list.get(1) != null) {
					nextBest = getClosestFootman(list.get(1));
					blockUtil += isMovingTowards(nextBest, list.get(0));
				}

				blockUtil += isMovingTowards(bestFootman, list.get(0));

			}
		}

		return blockUtil;
	}

	private double isMovingTowards(MapLocation start, MapLocation goal) {

		double utility = 0;

		if (start == null || goal == null) {
			return utility;
		}

		Integer fkey = -1;
		for (Integer key : gameState.footmenLocation.keySet()) {
			fkey = key;
			break;
		}

		Map<Integer, Action> gaction = gameState.gAction;
		Action action = gaction.get(fkey);

		String str = action.toString();
		if (goal.y < start.y && str.contains("NORTH")) {
			utility += 500;
		}
		if (goal.x > start.x && str.contains("EAST")) {
			utility += 500;
		}
		if (goal.y > start.y && str.contains("SOUTH")) {
			utility += 500;
		}
		if (goal.x < start.x && str.contains("WEST")) {
			utility += 500;
		}

		return utility;
	}

	private MapLocation getClosestFootman(MapLocation archer) {

		Map<Integer, MapLocation> footmen = gameState.footmenLocation;

		int min = Integer.MAX_VALUE;
		int bestKey = -1;
		for (Integer key : footmen.keySet()) {
			AStarSearch aStar = new AStarSearch();
			Stack<MapLocation> path = aStar.AstarSearch(footmen.get(key), archer, xExtent, yExtent, null,
					getResources());
			if (path.size() < min) {
				min = path.size();
				bestKey = key;
			}
		}

		return footmen.get(bestKey);
	}

	private double cornerEnemyUtility() {

		double cornerUtility = 0.0;

		// double blockUtil = blockUtility();
		// if (blockUtil > 0) {
		// cornerUtility += blockUtil;
		// return cornerUtility;
		// }

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
		corners.add(new MapLocation(0, yExtent - 1));
		corners.add(new MapLocation(xExtent - 1, yExtent - 1));

		for (MapLocation loc : corners) {
			if (gameState.stateView.isResourceAt(loc.x, loc.y)) {
				corners.remove(loc);
			}
		}

		return corners;
	}

	private double cornerHeuristic() {

		double cornerUtility = 0.0;

		List<MapLocation> corners = getCorners();
		Map<Integer, MapLocation> footmenLoc = gameState.footmenLocation;

		int minSize = Integer.MAX_VALUE;
		Map<Integer, MapLocation> bestLocs = new HashMap<Integer, MapLocation>();

		for (Integer id : gameState.footmenID) {
			int steps = Integer.MAX_VALUE;
			MapLocation bestLoc = null;
			for (MapLocation loc : corners) {
				AStarSearch aStar = new AStarSearch();
				Stack<MapLocation> path = aStar.AstarSearch(footmenLoc.get(id), loc, xExtent, yExtent, null,
						getResources());

				if (path.size() < steps) {
					steps = path.size();
					bestLoc = loc;
				}
			}
			minSize = steps;
			bestLocs.put(id, bestLoc);

			if (minSize < 1) {
				MinimaxAlphaBeta.f1Cornered = true;
			}
		}

		Map<Integer, Action> actions = gameState.gAction;

		if (actions == null) {
			return 0;
		}

		for (Integer key : bestLocs.keySet()) {
			Action act = actions.get(key);

			if (act == null) {
				return 0;
			}

			MapLocation bestLoc = bestLocs.get(key);

			if (bestLoc.y == 0 && act.toString().contains("NORTH")) {
				cornerUtility += 500;
			}
			if (bestLoc.x > 0 && act.toString().contains("EAST")) {
				cornerUtility += 500;
			}
			if (bestLoc.y > 0 && act.toString().contains("SOUTH")) {
				cornerUtility += 500;
			}
			if (bestLoc.x == 0 && act.toString().contains("WEST")) {
				cornerUtility += 500;
			}
		}
		return cornerUtility;
	}

	private double distanceUtility() {

		Set<MapLocation> resources = getResources();

		if (resources != null && !MinimaxAlphaBeta.f1Cornered) {
			return cornerHeuristic();
		}

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

		if (action.toString().contains("NORTH")) {
			newStart.y += 1;
		} else if (action.toString().contains("EAST")) {
			newStart.x -= 1;
		} else if (action.toString().contains("SOUTH")) {
			newStart.y -= 1;
		} else if (action.toString().contains("WEST")) {
			newStart.x += 1;
		}

		return newStart;
	}
}
