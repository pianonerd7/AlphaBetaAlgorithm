package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.environment.model.state.ResourceNode;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;

/**
 * This class stores all of the information the agent needs to know about the
 * state of the game. For example this might include things like footmen HP and
 * positions.
 *
 * Add any information or methods you would like to this class, but do not
 * delete or change the signatures of the provided methods.
 */
public class GameState {

	class MapLocation {
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

	public State.StateView stateView;
	public int xExtent;
	public int yExtent;
	public List<Unit.UnitView> archers;
	public List<Unit.UnitView> footmen;
	private boolean isMaxTurn = true;

	/**
	 * You will implement this constructor. It will extract all of the needed
	 * state information from the built in SEPIA state view.
	 *
	 * You may find the following state methods useful:
	 *
	 * state.getXExtent() and state.getYExtent(): get the map dimensions
	 * state.getAllResourceIDs(): returns all of the obstacles in the map
	 * state.getResourceNode(Integer resourceID): Return a ResourceView for the
	 * given ID
	 *
	 * For a given ResourceView you can query the position using
	 * resource.getXPosition() and resource.getYPosition()
	 *
	 * For a given unit you will need to find the attack damage, range and max
	 * HP unitView.getTemplateView().getRange(): This gives you the attack range
	 * unitView.getTemplateView().getBasicAttack(): The amount of damage this
	 * unit deals unitView.getTemplateView().getBaseHealth(): The maximum amount
	 * of health of this unit
	 *
	 * @param state
	 *            Current state of the episode
	 */
	public GameState(State.StateView state) {
		this.stateView = state;

		this.xExtent = state.getXExtent();
		this.yExtent = state.getYExtent();

	}

	private void populatePlayers() {

		for (Unit.UnitView unit : stateView.getAllUnits()) {
			if (unit.getTemplateView().getName().equals("Archer")) {
				this.archers.add(unit);
			}
			if (unit.getTemplateView().getName().equals("Footman")) {
				this.footmen.add(unit);
			}
		}
	}

	private List<MapLocation> getResources() {

		List<Integer> resourceIDs = stateView.getAllResourceIds();
		List<MapLocation> resourceLocations = new ArrayList<MapLocation>();
		for (Integer resourceID : resourceIDs) {
			ResourceNode.ResourceView resource = stateView.getResourceNode(resourceID);

			resourceLocations.add(new MapLocation(resource.getXPosition(), resource.getYPosition()));
		}

		return resourceLocations;
	}

	private List<MapLocation> getFootmenMapLocation() {

		List<MapLocation> footmenLocation = new ArrayList<MapLocation>();

		for (Unit.UnitView footman : footmen) {
			footmenLocation.add(getMapLocation(footman));
		}

		return footmenLocation;
	}

	private List<MapLocation> getArcherMapLocation() {

		List<MapLocation> archerLocation = new ArrayList<MapLocation>();

		for (Unit.UnitView archer : archers) {
			archerLocation.add(getMapLocation(archer));
		}

		return archerLocation;
	}

	private MapLocation getMapLocation(Unit.UnitView player) {
		return new MapLocation(player.getXPosition(), player.getYPosition());
	}

	private List<MapLocation> getLegalLocations(Unit.UnitView player) {

		List<MapLocation> neighbors = new ArrayList<MapLocation>();
		MapLocation playerLocation = getMapLocation(player);

		int x = playerLocation.x;
		int y = playerLocation.y;

		neighbors.add(new MapLocation(x - 1, y - 1));
		neighbors.add(new MapLocation(x, y - 1));
		neighbors.add(new MapLocation(x + 1, y - 1));
		neighbors.add(new MapLocation(x - 1, y));
		neighbors.add(new MapLocation(x + 1, y));
		neighbors.add(new MapLocation(x - 1, y + 1));
		neighbors.add(new MapLocation(x, y + 1));
		neighbors.add(new MapLocation(x + 1, y + 1));

		List<MapLocation> enemyLocation = null;

		if (isMaxTurn) {
			enemyLocation = getArcherMapLocation();
		} else {
			enemyLocation = getFootmenMapLocation();
		}

		for (MapLocation potentialNeighbor : new ArrayList<MapLocation>(neighbors)) {
			if (potentialNeighbor.x > xExtent || potentialNeighbor.x < 0 || potentialNeighbor.y > yExtent
					|| potentialNeighbor.y < 0) {
				neighbors.remove(potentialNeighbor);
			}
			for (MapLocation resource : getResources()) {
				if (resource.x == potentialNeighbor.x && resource.y == potentialNeighbor.y) {
					neighbors.remove(potentialNeighbor);
				}
			}

			for (MapLocation archerLoc : enemyLocation) {
				if (archerLoc.x == potentialNeighbor.x && archerLoc.y == potentialNeighbor.y) {
					neighbors.remove(potentialNeighbor);
				}
			}
		}

		return neighbors;
	}

	private Map<MapLocation, MapLocation> getMapLocationPairs() {

	}

	/**
	 * You will implement this function.
	 *
	 * You should use weighted linear combination of features. The features may
	 * be primitives from the state (such as hp of a unit) or they may be higher
	 * level summaries of information from the state such as distance to a
	 * specific location. Come up with whatever features you think are useful
	 * and weight them appropriately.
	 *
	 * It is recommended that you start simple until you have your algorithm
	 * working. Then watch your agent play and try to add features that correct
	 * mistakes it makes. However, remember that your features should be as fast
	 * as possible to compute. If the features are slow then you will be able to
	 * do less plys in a turn.
	 *
	 * Add a good comment about what is in your utility and why you chose those
	 * features.
	 *
	 * @return The weighted linear combination of the features
	 */
	public double getUtility() {

		return 0.0;
	}

	/**
	 * You will implement this function.
	 *
	 * This will return a list of GameStateChild objects. You will generate all
	 * of the possible actions in a step and then determine the resulting game
	 * state from that action. These are your GameStateChildren.
	 *
	 * You may find it useful to iterate over all the different directions in
	 * SEPIA.
	 *
	 * for(Direction direction : Directions.values())
	 *
	 * To get the resulting position from a move in that direction you can do
	 * the following x += direction.xComponent() y += direction.yComponent()
	 *
	 * @return All possible actions and their associated resulting game state
	 */
	public List<GameStateChild> getChildren() {

		ArrayList<GameStateChild> childrenList = new ArrayList<GameStateChild>();

		return null;
	}
}
