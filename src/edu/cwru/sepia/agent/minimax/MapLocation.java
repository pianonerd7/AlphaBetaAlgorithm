package edu.cwru.sepia.agent.minimax;

public class MapLocation {

	public int x;
	public int y;

	public MapLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "x: " + x + ", y: " + y;
	}

}
