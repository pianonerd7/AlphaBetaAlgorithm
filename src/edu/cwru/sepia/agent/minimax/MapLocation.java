package edu.cwru.sepia.agent.minimax;

class MapLocation implements Comparable<MapLocation> {
	public int x;
	public int y;
	public double heuristicCost = 0;
	public double functionCost = 0;
	public double nodeCost = 0;
	public MapLocation cameFrom;

	public MapLocation(int x, int y) {
		this.x = x;
		this.y = y;

	}

	public MapLocation(int x, int y, MapLocation cameFrom, float cost) {
		this.x = x;
		this.y = y;
		this.nodeCost = (cameFrom == null) ? (int) cost : cameFrom.nodeCost + (int) cost;
	}

	@Override
	public int compareTo(MapLocation otherMapLocation) {
		return new Double(this.heuristicCost).compareTo(otherMapLocation.heuristicCost);
	}

	@Override
	public String toString() {
		return "x: " + x + ", y: " + y + ", f: " + functionCost + ", heuristic: " + heuristicCost + ", nodecost: "
				+ nodeCost;
	}
}