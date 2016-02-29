package edu.cwru.sepia.agent.minimax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.DirectedAction;
import edu.cwru.sepia.action.TargetedAction;
import edu.cwru.sepia.environment.model.state.State;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.util.Direction;

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
	// id and xy location
	public Map<Integer, MapLocation> footmenLocation = new HashMap<Integer, MapLocation>();
	// id and xy location
	public Map<Integer, MapLocation> archerLocation = new HashMap<Integer, MapLocation>();
	public List<Integer> footmenID = new ArrayList<Integer>();
	public List<Integer> archerID = new ArrayList<Integer>();
	public Map<Integer, Integer> footmenHP = new HashMap<Integer, Integer>();
	public Map<Integer, Integer> archerHP = new HashMap<Integer, Integer>();
	public int footmenAttackRange;
	public int archerAttackRange;
	public int footmenAttackPt;
	public int archerAttackPt;
	public double lifeExpectancy;
	public double utility = 0.0;

	Collection<Unit.UnitView> allUnits;

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
		populatePlayers(state);
	}

	public GameState(State.StateView state, Map<Integer, MapLocation> footmenLocation,
			Map<Integer, MapLocation> archerLocation, List<Integer> footmenID, List<Integer> archerID,
			Map<Integer, Integer> footmenHP, Map<Integer, Integer> archerHP, Collection<Unit.UnitView> allUnits,
			int footmenAttackRange, int archerAttackRange, int footmenAttackPt, int archerAttackpt,
			double lifeExpectancy) {
		this.stateView = state;
		this.footmenLocation = footmenLocation;
		this.archerLocation = archerLocation;
		this.footmenID = footmenID;
		this.archerID = archerID;
		this.footmenHP = footmenHP;
		this.archerHP = archerHP;
		this.footmenAttackRange = footmenAttackRange;
		this.archerAttackRange = archerAttackRange;
		this.footmenAttackPt = footmenAttackPt;
		this.archerAttackPt = archerAttackpt;
		this.lifeExpectancy = lifeExpectancy;
	}

	private void populatePlayers(State.StateView state) {

		Collection<Unit.UnitView> units = state.getAllUnits();

		for (Unit.UnitView unit : units) {
			if (unit.getTemplateView().getName().equals("Archer")) {

				this.archerLocation.put(unit.getID(), new MapLocation(unit.getXPosition(), unit.getYPosition()));
				this.archerID.add(unit.getID());
				this.archerHP.put(unit.getID(), unit.getHP());
				this.archerAttackRange = unit.getTemplateView().getRange();
				this.archerAttackPt = unit.getTemplateView().getBasicAttack();
			}
			if (unit.getTemplateView().getName().equals("Footman")) {

				this.footmenLocation.put(unit.getID(), new MapLocation(unit.getXPosition(), unit.getYPosition()));
				this.footmenID.add(unit.getID());
				this.footmenHP.put(unit.getID(), unit.getHP());
				this.footmenAttackRange = unit.getTemplateView().getRange();
				this.footmenAttackPt = unit.getTemplateView().getBasicAttack();
			}
		}

		this.allUnits = units;
	}

	private ArrayList<Action> getAction(Integer id, MapLocation location) {

		ArrayList<Action> legalActions = new ArrayList<Action>();

		for (Direction direction : Direction.values()) {

			// skip diagonals
			if (!(direction.xComponent() == 0 || direction.yComponent() == 0)) {
				continue;
			}

			if (isLocationValid(location.x + direction.xComponent(), location.y + direction.yComponent())) {

				legalActions.add(Action.createPrimitiveMove(id, direction));
			}
		}

		for (MapLocation enemy : getAttackableEnemies(location)) {

			int enemyId = -1;
			if (MinimaxAlphaBeta.isMaxTurn) {

				for (Integer key : archerLocation.keySet()) {

					MapLocation loc = archerLocation.get(key);

					if (loc.x == enemy.x && loc.y == enemy.y) {
						enemyId = key;
					}
				}
			} else {

				for (Integer key : footmenLocation.keySet()) {

					MapLocation loc = footmenLocation.get(key);

					if (loc.x == enemy.x && loc.y == enemy.y) {
						enemyId = key;
					}
				}
			}

			legalActions.add(Action.createPrimitiveAttack(id, enemyId));
		}

		return legalActions;
	}

	private boolean isLocationValid(int x, int y) {

		if (stateView.isResourceAt(x, y) || !stateView.inBounds(x, y)) {
			return false;
		}

		for (Integer key : footmenLocation.keySet()) {
			MapLocation locations = footmenLocation.get(key);

			if (x == locations.x && y == locations.y) {
				return false;
			}
		}

		for (Integer key : archerLocation.keySet()) {
			MapLocation locations = archerLocation.get(key);

			if (x == locations.x && y == locations.y) {
				return false;
			}
		}

		return true;
	}

	private ArrayList<MapLocation> getAttackableEnemies(MapLocation location) {

		ArrayList<MapLocation> attackables = new ArrayList<MapLocation>();

		if (MinimaxAlphaBeta.isMaxTurn) {
			// footmen's turn, my enemy is the archers
			List<MapLocation> enemies = new ArrayList<MapLocation>();

			for (Integer key : archerLocation.keySet()) {
				enemies.add(archerLocation.get(key));
			}

			attackables = getAttackableEnemyList(location, enemies, footmenAttackRange);
		} else {

			List<MapLocation> enemies = new ArrayList<MapLocation>();

			for (Integer key : footmenLocation.keySet()) {
				enemies.add(footmenLocation.get(key));
			}
			attackables = getAttackableEnemyList(location, enemies, archerAttackRange);
		}

		return attackables;
	}

	private ArrayList<MapLocation> getAttackableEnemyList(MapLocation me, List<MapLocation> enemies, int range) {

		ArrayList<MapLocation> attackables = new ArrayList<MapLocation>();

		for (MapLocation enemy : enemies) {
			int diffX = Math.abs(enemy.x - me.x);
			int diffY = Math.abs(enemy.y - me.y);

			if (range >= (diffX + diffY)) {
				attackables.add(enemy);
			}
		}

		return attackables;
	}

	private List<Map<Integer, Action>> getActionPairs() {

		List<ArrayList<Action>> actions = new ArrayList<ArrayList<Action>>();

		if (MinimaxAlphaBeta.isMaxTurn) {

			for (Integer key : footmenLocation.keySet()) {
				actions.add(getAction(key, footmenLocation.get(key)));
			}
		} else {

			for (Integer key : archerLocation.keySet()) {
				MapLocation ml = archerLocation.get(key);
				actions.add(getAction(key, ml));
			}
		}

		List<Map<Integer, Action>> actionPairs = new ArrayList<Map<Integer, Action>>();

		if (MinimaxAlphaBeta.isMaxTurn) {

			if (footmenLocation.size() == 2) {

				for (int i = 0; i < actions.get(0).size(); i++) {
					for (int j = 0; j < actions.get(1).size(); j++) {

						Map<Integer, Action> map = new HashMap<Integer, Action>();

						ArrayList<Integer> keys = new ArrayList<Integer>();
						for (Integer key : footmenLocation.keySet()) {
							keys.add(key);
						}
						map.put(keys.get(0), actions.get(0).get(i));
						map.put(keys.get(1), actions.get(1).get(j));

						actionPairs.add(map);
					}
				}
				return actionPairs;
			} else if (footmenLocation.size() == 1) {

				for (int i = 0; i < actions.get(0).size(); i++) {

					Map<Integer, Action> map = new HashMap<Integer, Action>();

					ArrayList<Integer> keys = new ArrayList<Integer>();
					for (Integer key : footmenLocation.keySet()) {
						keys.add(key);
					}
					map.put(keys.get(0), actions.get(0).get(i));

					actionPairs.add(map);
				}
				return actionPairs;
			}
		}
		if (!MinimaxAlphaBeta.isMaxTurn) {
			if (archerLocation.size() == 2) {

				for (int i = 0; i < actions.get(0).size(); i++) {
					for (int j = 0; j < actions.get(1).size(); j++) {

						Map<Integer, Action> map = new HashMap<Integer, Action>();

						ArrayList<Integer> keys = new ArrayList<Integer>();

						for (Integer key : archerLocation.keySet()) {
							keys.add(key);
						}
						map.put(keys.get(0), actions.get(0).get(i));
						map.put(keys.get(1), actions.get(1).get(j));

						actionPairs.add(map);
					}
				}
				return actionPairs;
			} else if (archerLocation.size() == 1) {

				for (int i = 0; i < actions.get(0).size(); i++) {

					Map<Integer, Action> map = new HashMap<Integer, Action>();

					ArrayList<Integer> keys = new ArrayList<Integer>();

					for (Integer key : archerLocation.keySet()) {
						keys.add(key);
					}
					map.put(keys.get(0), actions.get(0).get(i));

					actionPairs.add(map);
				}
				return actionPairs;
			}
		}
		return actionPairs;
	}

	private GameState executeAction(Map<Integer, Action> action) {

		List<MapLocation> me = new ArrayList<MapLocation>();
		List<Direction> myDir = new ArrayList<Direction>();
		List<Integer> id = new ArrayList<Integer>();
		int isDist = 0;
		int isAttack = 0;

		Map<Integer, MapLocation> newFootmenLocation = new HashMap<Integer, MapLocation>();
		Map<Integer, MapLocation> newArcherLocation = new HashMap<Integer, MapLocation>();
		List<Integer> newFootmenID = new ArrayList<Integer>();
		List<Integer> newArcherID = new ArrayList<Integer>();
		Map<Integer, Integer> newFootmenHP = new HashMap<Integer, Integer>();
		Map<Integer, Integer> newArcherHP = new HashMap<Integer, Integer>();
		double newLifeExpectancy = 0.0;

		// clone all objects
		for (Integer newFootmenKey : footmenLocation.keySet()) {
			MapLocation newMap = new MapLocation(footmenLocation.get(newFootmenKey).x,
					footmenLocation.get(newFootmenKey).y);
			newFootmenLocation.put(newFootmenKey.intValue(), newMap);
		}

		for (Integer newArcherKey : archerLocation.keySet()) {
			MapLocation newMap = new MapLocation(archerLocation.get(newArcherKey).x,
					archerLocation.get(newArcherKey).y);
			newArcherLocation.put(newArcherKey.intValue(), newMap);
		}

		for (Integer footmanID : footmenID) {
			newFootmenID.add(footmanID);
		}

		for (Integer archID : archerID) {
			newArcherID.add(archID);
		}

		for (Integer footmanHPKey : footmenHP.keySet()) {
			newFootmenHP.put(footmanHPKey.intValue(), footmenHP.get(footmanHPKey).intValue());
		}

		for (Integer archerHPKey : archerHP.keySet()) {
			newArcherHP.put(archerHPKey.intValue(), archerHP.get(archerHPKey).intValue());
		}

		for (int key : action.keySet()) {

			if (action.get(key) instanceof DirectedAction) {

				isDist++;

				if (MinimaxAlphaBeta.isMaxTurn) {

					Set<Integer> fkey = footmenLocation.keySet();
					for (Integer footmanKey : fkey) {

						if (key == footmanKey) {
							MapLocation ml = new MapLocation(footmenLocation.get(footmanKey).x,
									footmenLocation.get(footmanKey).y);
							me.add(ml);
							id.add(key);
						}
					}
				} else {

					for (Integer archerKey : archerLocation.keySet()) {

						if (key == archerKey) {
							MapLocation ml = new MapLocation(archerLocation.get(archerKey).x,
									archerLocation.get(archerKey).y);
							me.add(ml);
							id.add(key);
						}
					}
				}

				Direction direction = ((DirectedAction) action.get(key)).getDirection();
				myDir.add(direction);
			}

			if (action.get(key) instanceof TargetedAction) {

				isAttack++;

				TargetedAction targetedAction = (TargetedAction) action.get(key);

				int enemyId = targetedAction.getTargetId();
				int hp;

				// footman attacking archer
				if (MinimaxAlphaBeta.isMaxTurn) {
					hp = newArcherHP.get(enemyId);
					hp = hp - footmenAttackPt;

					if (hp < 1) {
						newArcherLocation.remove(enemyId);
						newArcherHP.remove(enemyId);

						for (int i = 0; i < newArcherID.size(); i++) {
							if (newArcherID.get(i) == enemyId) {
								newArcherID.remove(i);
							}
						}

						if (newArcherHP.size() == 0) {
							newLifeExpectancy = Double.MAX_VALUE;
						}
					} else {
						newArcherHP.remove(enemyId);
						newArcherHP.put(enemyId, hp);
					}
				} else {// archer attacks footman
					hp = newFootmenHP.get(enemyId);
					hp = hp - archerAttackPt;

					if (hp < 1) {
						newFootmenLocation.remove(enemyId);
						newFootmenHP.remove(enemyId);

						for (int i = 0; i < newFootmenID.size(); i++) {
							if (newFootmenID.get(i) == enemyId) {
								newFootmenID.remove(i);
							}
						}

						if (newFootmenHP.size() == 0) {
							newLifeExpectancy = Double.MIN_VALUE;
						}
					} else {
						newFootmenHP.remove(enemyId);
						newFootmenHP.put(enemyId, hp);
					}
				}
			}
		}

		if (isDist == 2) {
			// footmen's turn
			if (MinimaxAlphaBeta.isMaxTurn) {
				Map<Integer, MapLocation> newFootmenLoc = updateLocation(me, myDir, id);
				return new GameState(stateView, newFootmenLoc, archerLocation, footmenID, archerID, footmenHP, archerHP,
						allUnits, footmenAttackRange, archerAttackRange, footmenAttackPt, archerAttackPt,
						newLifeExpectancy);
			} else {
				Map<Integer, MapLocation> newArcherLoc = updateLocation(me, myDir, id);
				return new GameState(stateView, footmenLocation, newArcherLoc, footmenID, archerID, footmenHP, archerHP,
						allUnits, footmenAttackRange, archerAttackRange, footmenAttackPt, archerAttackPt,
						newLifeExpectancy);
			}
		}

		if (isAttack == 2) {
			return new GameState(stateView, newFootmenLocation, newArcherLocation, newFootmenID, newArcherID,
					newFootmenHP, newArcherHP, allUnits, footmenAttackRange, archerAttackRange, footmenAttackPt,
					archerAttackPt, newLifeExpectancy);
		} else {

			if (MinimaxAlphaBeta.isMaxTurn) {
				Map<Integer, MapLocation> newFootmenLoc = updateLocation(me, myDir, id);

				for (Integer key : newFootmenLoc.keySet()) {

					MapLocation loc = newFootmenLocation.get(key);

					if (loc != null) {
						newFootmenLocation.get(key).x = newFootmenLoc.get(key).x;
						newFootmenLocation.get(key).y = newFootmenLoc.get(key).y;
					}
				}
			} else {
				Map<Integer, MapLocation> newArcherLoc = updateLocation(me, myDir, id);

				for (Integer key : newArcherLoc.keySet()) {

					MapLocation loc = newArcherLocation.get(key);

					if (loc != null) {
						newArcherLocation.get(key).x = newArcherLoc.get(key).x;
						newArcherLocation.get(key).y = newArcherLoc.get(key).y;
					}
				}
			}

			return new GameState(stateView, newFootmenLocation, newArcherLocation, newFootmenID, newArcherID,
					newFootmenHP, newArcherHP, allUnits, footmenAttackRange, archerAttackRange, footmenAttackPt,
					archerAttackPt, newLifeExpectancy);
		}
	}

	private Map<Integer, MapLocation> updateLocation(List<MapLocation> me, List<Direction> myDir, List<Integer> id) {

		Map<Integer, MapLocation> newLoc = new HashMap<Integer, MapLocation>();

		if (me.size() == 2) {

			MapLocation me1 = me.get(0);
			Direction dir1 = myDir.get(0);

			me1.x = me1.x + dir1.xComponent();
			me1.y = me1.y + dir1.yComponent();

			MapLocation me2 = me.get(1);
			Direction dir2 = myDir.get(1);

			me2.x = me2.x + dir2.xComponent();
			me2.y = me2.y + dir2.yComponent();

			newLoc.put(id.get(0), me1);
			newLoc.put(id.get(1), me2);
		} else if (me.size() == 1) {

			MapLocation mee = me.get(0);
			Direction dir = myDir.get(0);

			mee.x = mee.x + dir.xComponent();
			mee.y = mee.y + dir.yComponent();

			newLoc.put(id.get(0), mee);
		}
		return newLoc;
	}

	/*
	 * private boolean isStateValid(State newState) {
	 * 
	 * Collection<PlayerState> playerStates = newState.getPlayerStates();
	 * 
	 * HashMap<Integer, Integer> checker = new HashMap<Integer, Integer>();
	 * 
	 * for (PlayerState player : playerStates) { }
	 * 
	 * newState.stateView }
	 */

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

		// return new HeuristicUtility(this).getHeuristic();
		HeuristicUtility util = new HeuristicUtility(this);
		this.utility = util.getHeuristic();
		return this.utility;
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

		List<Map<Integer, Action>> act = getActionPairs();
		for (Map<Integer, Action> action : act) {
			GameState gs = executeAction(action);
			GameStateChild newChild = new GameStateChild(action, gs);
			childrenList.add(newChild);

			// System.out.println("gs: " + gs.footmenLocation.toString());
			// System.out.println("newChild: " +
			// newChild.state.footmenLocation.toString());
			// for (int i = 0; i < childrenList.size(); i++) {
			// System.out.println(i + " , footmen childlist: " +
			// childrenList.get(i).state.footmenLocation.toString());
			// }
			//
			// for (int i = 0; i < childrenList.size(); i++) {
			// System.out.println(i + " , archer childlist: " +
			// childrenList.get(i).state.archerLocation.toString());
			// }
			//
			// System.out.println("\n\n");
		}
		return childrenList;
	}
}