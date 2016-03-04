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

/**
 * This class calculate sthe heuristic of a specific GameState of a GameState
 * child. The heuristic calculation is abstracted away in this class
 *
 */
public class HeuristicUtility {

	private GameState gameState;
	private int xExtent;
	private int yExtent;

	public HeuristicUtility(GameState state) {
		this.gameState = state;
		xExtent = gameState.stateView.getXExtent();
		yExtent = gameState.stateView.getYExtent();
	}

	/**
	 * Computes the heuristic using linear weight
	 * 
	 * @return
	 */
	public double getHeuristic() {

		double heuristicEstimate = 0.0;
		heuristicEstimate += distanceUtility();
		return heuristicEstimate;
	}

	/**
	 * If the enemy is ever at a state where out of the four adjacent locations,
	 * three are occupied (either out of bound, or has a resource), then the
	 * agent should be rewarded for going to the last available location to
	 * block the enemy
	 * 
	 * @return
	 */
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
			if (list.size() == 1) {

				int bestKey = getClosestFootman(list.get(0));
				MapLocation bestFootman = gameState.footmenLocation.get(bestKey);
				blockUtil += aStarSearch(bestFootman, list.get(0), bestKey, gameState.archerID.get(0), false);
			}
		}

		return blockUtil;
	}

	/**
	 * Give me the location of an archer and I will give you the footmenID of
	 * the closest footman
	 * 
	 * @param archer
	 * @return
	 */
	private int getClosestFootman(MapLocation archer) {

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

		return bestKey;
	}

	/**
	 * See if it's possible to corner an enemy and finish them. Higher utility
	 * means one step closer to cornering enemy
	 * 
	 * @return
	 */
	private double cornerEnemyUtility() {

		double cornerUtility = 0.0;

		double blockUtil = blockUtility();
		if (blockUtil > 0) {
			cornerUtility += blockUtil;
			return cornerUtility;
		}

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

	/**
	 * Gets the original location prior to the action
	 * 
	 * @param actions
	 * @return
	 */
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

	/**
	 * Gets the four corners of the map
	 * 
	 * @return
	 */
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

	/**
	 * If the footman has a possibility of being stuck, it retreats to the
	 * closest corner
	 * 
	 * @return
	 */
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

	/**
	 * Calculates the utility of the state based on the number of footmen and
	 * archers still alive
	 * 
	 * @return
	 */
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

			distUtility += aStarSearch(footman1, gameState.archerLocation.get(index), gameState.footmenID.get(0), index,
					true);

			for (Integer key : gameState.archerLocation.keySet()) {

				if (key != index) {
					distUtility += aStarSearch(gameState.footmenLocation.get(gameState.footmenID.get(1)),
							gameState.archerLocation.get(key), gameState.footmenID.get(1), key, true);
				}
			}

		} else if (numFootmen == 2 && numArchers == 1) {
			MapLocation footman1 = gameState.footmenLocation.get(gameState.footmenID.get(1));
			MapLocation footman2 = gameState.footmenLocation.get(gameState.footmenID.get(0));
			MapLocation archer = null;
			int index = -1;

			for (Integer key : gameState.archerLocation.keySet()) {

				MapLocation temp = gameState.archerLocation.get(key);

				if (temp != null) {
					archer = temp;
					index = key;
				}
			}

			distUtility += aStarSearch(footman1, archer, gameState.footmenID.get(1), index, true);
			distUtility += aStarSearch(footman2, footman1, gameState.footmenID.get(0), index, true);

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

			distUtility += aStarSearch(footman, archer1, index, gameState.archerID.get(0), true);
			distUtility += aStarSearch(footman, archer2, index, gameState.archerID.get(1), true);

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

			distUtility += aStarSearch(footman, archer, fIndex, aIndex, true);
		}

		return distUtility;
	}

	/**
	 * Computes the pythagorean theorem distance between two MapLocations
	 * 
	 * @param me
	 * @param enemy
	 * @return
	 */
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

	/**
	 * Gets all the resources on a map
	 * 
	 * @return
	 */
	private Set<MapLocation> getResources() {

		List<Integer> resourceIDs = gameState.stateView.getAllResourceIds();
		Set<MapLocation> resourceLocations = new HashSet<MapLocation>();
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = gameState.stateView.getResourceNode(resourceID);

			resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition()));
		}

		return resourceLocations;
	}

	/**
	 * Uses the AStarSearch algorithm to see if the agent is moving closer
	 * towards the enemy. Especially for footmen, it must locate the archer as
	 * soon as possible, since archers can shoot far, and footmen can only
	 * attack at a close distance.
	 * 
	 * @param start
	 * @param goal
	 * @param footmanKey
	 * @param archerKey
	 * @param toCorner
	 * @return
	 */
	private double aStarSearch(MapLocation start, MapLocation goal, int footmanKey, int archerKey, boolean toCorner) {

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
		if (path.size() <= 1 && toCorner) {
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

	/**
	 * Penalizes the agent if it tries to go in the opposite direction than the
	 * optimal direction
	 * 
	 * @param action
	 * @param start
	 * @param nextLoc
	 * @return
	 */
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

	/**
	 * Gets the original location prior to the action
	 * 
	 * @param start
	 * @param action
	 * @param newStart
	 * @return
	 */
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
