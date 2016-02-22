package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.environment.model.state.ResourceNode;

public class Utility {

	class MapLocation implements Comparable<MapLocation> {
		public int x;
		public int y;
		public int heuristicCost = 0;
		public int functionCost = 0;
		public int nodeCost = 0;
		public MapLocation cameFrom;

		public MapLocation(int x, int y, MapLocation cameFrom, float cost) {
			this.x = x;
			this.y = y;
			this.nodeCost = (cameFrom == null) ? (int) cost : cameFrom.nodeCost + (int) cost;
		}

		@Override
		public int compareTo(MapLocation otherMapLocation) {
			return this.functionCost - otherMapLocation.functionCost;
		}

		@Override
		public String toString() {
			return "x: " + x + ", y: " + y + ", f: " + functionCost + ", heuristic: " + heuristicCost + ", nodecost: "
					+ nodeCost;
		}
	}

	public static double estimateHeuristic(GameStateChild node) {

		return 0;
	}

	private List<MapLocation> getSources(GameStateChild node) {

		List<Integer> resourceIDs = node.state.stateView.getAllResourceIds();
		List<MapLocation> resourceLocations = new ArrayList<MapLocation>();
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = node.state.stateView.getResourceNode(resourceID);

			resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition(), null, 0));
		}

		return resourceLocations;
	}
}
