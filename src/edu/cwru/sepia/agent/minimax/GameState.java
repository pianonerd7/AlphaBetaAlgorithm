package edu.cwru.sepia.agent.minimax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public State.StateView stateView;
	public List<Unit.UnitView> archers = new ArrayList<Unit.UnitView>();
	public List<Unit.UnitView> footmen = new ArrayList<Unit.UnitView>();
	public Collection<Unit.UnitView> units;

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

		// units = state;
		populatePlayers(state);
	}

	private void populatePlayers(State.StateView state) {

		Collection<Unit.UnitView> units = state.getAllUnits();

		for (Unit.UnitView unit : units) {
			if (unit.getTemplateView().getName().equals("Archer")) {
				this.archers.add(unit);
			}
			if (unit.getTemplateView().getName().equals("Footman")) {
				this.footmen.add(unit);
			}
		}
		System.out.println("");
	}

	private ArrayList<Action> getAction(Unit.UnitView unit) {

		ArrayList<Action> legalActions = new ArrayList<Action>();

		for (Direction direction : Direction.values()) {
			if (!(direction.xComponent() == 0 || direction.yComponent() == 0)) {
				continue;
			}

			if (isLocationValid(unit.getXPosition() + direction.xComponent(),
					unit.getYPosition() + direction.yComponent())) {
				System.out.println("x: " + direction.xComponent() + "y: " + direction.yComponent());
				legalActions.add(Action.createPrimitiveMove(unit.getID(), direction));
			}
		}

		for (Unit.UnitView enemy : getAttackableEnemies(unit)) {
			legalActions.add(Action.createPrimitiveAttack(unit.getID(), enemy.getID()));
		}

		return legalActions;
	}

	private ArrayList<Unit.UnitView> getAttackableEnemies(Unit.UnitView unit) {

		ArrayList<Unit.UnitView> attackables = new ArrayList<Unit.UnitView>();

		int range = unit.getTemplateView().getRange();

		if (MinimaxAlphaBeta.isMaxTurn) {
			attackables = getEnemyList(unit, archers, range);
		} else {
			attackables = getEnemyList(unit, footmen, range);
		}

		return attackables;
	}

	private ArrayList<Unit.UnitView> getEnemyList(Unit.UnitView unit, List<Unit.UnitView> enemies, int range) {

		ArrayList<Unit.UnitView> attackables = new ArrayList<Unit.UnitView>();

		for (Unit.UnitView enemy : enemies) {
			int diffX = Math.abs(enemy.getXPosition() - unit.getXPosition());
			int diffY = Math.abs(enemy.getYPosition() - unit.getYPosition());

			if (range >= (diffX + diffY)) {
				attackables.add(enemy);
			}
		}

		return attackables;
	}

	private boolean isLocationValid(int x, int y) {

		return !(stateView.isUnitAt(x, y) && stateView.isResourceAt(x, y)) && stateView.inBounds(x, y);
	}

	private List<Map<Integer, Action>> getActionPairs() {

		List<ArrayList<Action>> actions = new ArrayList<ArrayList<Action>>();

		if (MinimaxAlphaBeta.isMaxTurn) {

			for (Unit.UnitView footman : footmen) {
				actions.add(getAction(footman));
			}
		} else {

			for (Unit.UnitView archer : archers) {
				actions.add(getAction(archer));
			}
		}

		List<Map<Integer, Action>> actionPairs = new ArrayList<Map<Integer, Action>>();

		for (int i = 0; i < actions.get(0).size(); i++) {
			for (int j = 0; j < actions.get(1).size(); j++) {

				Map<Integer, Action> map = new HashMap<Integer, Action>();

				if (MinimaxAlphaBeta.isMaxTurn) {
					map.put(footmen.get(0).getID(), actions.get(0).get(i));
					map.put(footmen.get(1).getID(), actions.get(1).get(j));
				} else {
					map.put(archers.get(0).getID(), actions.get(0).get(i));
					map.put(archers.get(1).getID(), actions.get(1).get(j));
				}

				actionPairs.add(map);
			}
		}
		return actionPairs;
	}

	private GameState executeAction(Map<Integer, Action> action) {

		try {
			State newState = this.stateView.getStateCreator().createState();

			for (int key : action.keySet()) {

				if (action.get(key) instanceof TargetedAction) {

					Unit beingAttacked = newState.getUnit(((TargetedAction) action.get(key)).getTargetId());
					beingAttacked.setHP(
							beingAttacked.getCurrentHealth() - newState.getUnit(key).getTemplate().getBasicAttack());

					if (beingAttacked.getCurrentHealth() < 1) {
						newState.removeUnit(((TargetedAction) action.get(key)).getTargetId());
					}
				} else {
					Direction direction = ((DirectedAction) action.get(key)).getDirection();
					newState.moveUnit(newState.getUnit(key), direction);
				}
			}

			// isStateValid(newState);

			State.StateView s = newState.getView(this.stateView.getPlayerNumbers()[0]);
			GameState g = new GameState(s);
			return g;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
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

		return new HeuristicUtility(this.stateView, this.archers, this.footmen).getHeuristic();
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

		for (Map<Integer, Action> action : getActionPairs()) {
			GameState newState = executeAction(action);

			if (newState == null) {
				continue;
			}

			childrenList.add(new GameStateChild(action, newState));
		}

		return childrenList;
	}
}
