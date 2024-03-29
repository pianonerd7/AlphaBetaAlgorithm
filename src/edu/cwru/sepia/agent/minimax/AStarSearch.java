package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Used the AStarSearch algorithm to estimate a path to the goal, this is
 * especially useful on a map with resources because just taking the difference
 * between two MapLocations doesn't account for resources, but AStarSearch does.
 */
public class AStarSearch {

	/**
	 * Computes a path from the start to the goal by using a heuristic to
	 * estimate and expand favorable nodes
	 * 
	 * @param start
	 * @param goal
	 * @param xExtent
	 * @param yExtent
	 * @param enemyFootmanLoc
	 * @param resourceLocations
	 * @return
	 */
	public Stack<MapLocation> AstarSearch(MapLocation start, MapLocation goal, int xExtent, int yExtent,
			MapLocation enemyFootmanLoc, Set<MapLocation> resourceLocations) {

		ArrayList<MapLocation> openList = new ArrayList<MapLocation>();
		ArrayList<MapLocation> closedList = new ArrayList<MapLocation>();

		start.nodeCost = 0;
		start.heuristicCost = 0;
		start.functionCost = 0;

		openList.add(start);

		while (!openList.isEmpty()) {
			Collections.sort(openList);
			MapLocation current = openList.get(0);
			openList.remove(0);

			if (current.x == goal.x && current.y == goal.y) {

				return returnPath(current, start);
			}

			closedList.add(current);

			List<MapLocation> neighbors = getNeighbors(current, resourceLocations, enemyFootmanLoc, xExtent, yExtent);

			for (MapLocation neighbor : neighbors) {
				neighbor.cameFrom = current;
				neighbor.nodeCost = current.nodeCost + 1;
				neighbor.heuristicCost = getHeuristic(neighbor, goal);
				neighbor.functionCost = neighbor.nodeCost + neighbor.heuristicCost;
				if (canAddToOpenList(neighbor, openList, closedList)) {
					openList.add(neighbor);
				}
			}
		}
		System.exit(0);
		return null;
	}

	/**
	 * Tests to see if this neighbor can be added to the openlist
	 * 
	 * @param neighbor
	 * @param openList
	 * @param closedList
	 * @return
	 */
	private boolean canAddToOpenList(MapLocation neighbor, ArrayList<MapLocation> openList,
			ArrayList<MapLocation> closedList) {

		boolean toAdd = true;
		for (MapLocation node : openList) {
			if (neighbor.x == node.x && neighbor.y == node.y) {
				if (node.functionCost < neighbor.functionCost) {
					toAdd = false;
				}
			}
		}
		for (MapLocation node : closedList) {
			if (neighbor.x == node.x && neighbor.y == node.y) {
				if (node.functionCost < neighbor.functionCost) {
					toAdd = false;
				}
			}
		}
		return toAdd;

	}

	/**
	 * Function to get heuristic
	 * 
	 * @param current
	 * @param goal
	 * @return
	 */
	private double getHeuristic(MapLocation current, MapLocation goal) {
		return Math.max(Math.abs(current.x - goal.x), Math.abs(current.y - goal.y));
	}

	/**
	 * Starts from the goal location, and backtracks until it finds the start
	 * and returns the full path
	 * 
	 * @param goal
	 * @param start
	 * @return
	 */
	private Stack<MapLocation> returnPath(MapLocation goal, MapLocation start) {
		Stack<MapLocation> path = new Stack<MapLocation>();

		MapLocation iter = goal;
		while (iter.cameFrom != null) {
			path.add(iter.cameFrom);
			iter = iter.cameFrom;
		}
		// to remove start

		if (path.isEmpty()) {
			return path;
		}

		path.pop();

		return path;
	}

	/**
	 * Gets all the possible neighbors of a coordinate, excluding diagonals
	 * 
	 * @param current
	 * @param resourceLocations
	 * @param enemyFootmanLoc
	 * @param xExtent
	 * @param yExtent
	 * @return
	 */
	private List<MapLocation> getNeighbors(MapLocation current, Set<MapLocation> resourceLocations,
			MapLocation enemyFootmanLoc, int xExtent, int yExtent) {

		ArrayList<MapLocation> neighbors = new ArrayList<MapLocation>();
		boolean deleted = false;
		int x = current.x;
		int y = current.y;

		neighbors.add(new MapLocation(x, y - 1, current, 1));
		neighbors.add(new MapLocation(x - 1, y, current, 1));
		neighbors.add(new MapLocation(x + 1, y, current, 1));
		neighbors.add(new MapLocation(x, y + 1, current, 1));

		for (MapLocation potentialNeighbor : new ArrayList<MapLocation>(neighbors)) {
			deleted = false;
			if (potentialNeighbor.x > xExtent || potentialNeighbor.x < 0 || potentialNeighbor.y > yExtent
					|| potentialNeighbor.y < 0) {
				neighbors.remove(potentialNeighbor);
				deleted = true;
			}
			for (MapLocation resource : resourceLocations) {
				if (!deleted && resource.x == potentialNeighbor.x && resource.y == potentialNeighbor.y) {
					neighbors.remove(potentialNeighbor);
					deleted = true;
				}
			}
			if (enemyFootmanLoc != null && !deleted && enemyFootmanLoc.x == potentialNeighbor.x
					&& enemyFootmanLoc.y == potentialNeighbor.y) {
				neighbors.remove(potentialNeighbor);
			}
		}
		return neighbors;
	}
}
