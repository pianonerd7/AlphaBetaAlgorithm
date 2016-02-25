package edu.cwru.sepia.agent.minimax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.DirectedAction;
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
	public int xExtent;
	public int yExtent;
	public List<Unit.UnitView> archers;
	public List<Unit.UnitView> footmen;

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
		populatePlayers();
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

	private ArrayList<Action> getAction(Unit.UnitView unit) {

		ArrayList<Action> legalActions = new ArrayList<Action>();

		for (Direction direction : Direction.values()) {
			if (direction.xComponent() == 0 || direction.yComponent() == 0) {
				break;
			}

			if (isLocationValid(unit.getXPosition() + direction.xComponent(),
					unit.getYPosition() + direction.yComponent())) {
				legalActions.add(Action.createPrimitiveMove(unit.getID(), direction));
			}
		}

		return legalActions;
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
			}
		}
		return actionPairs;
	}

	private GameState executeAction(Map<Integer, Action> action) {

		try {
			State newState = this.stateView.getStateCreator().createState();

			for (int key : action.keySet()) {

				Direction direction = ((DirectedAction) action.get(key)).getDirection();
				newState.moveUnit(newState.getUnit(key), direction);
			}

			isStateValid(newState);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// return new GameState(newState.getView(action.keySet()[0]));
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

		for (Map<Integer, Action> action : getActionPairs()) {
			childrenList.add(new GameStateChild(action, executeAction(action)));
		}

		return childrenList;
	}
}
