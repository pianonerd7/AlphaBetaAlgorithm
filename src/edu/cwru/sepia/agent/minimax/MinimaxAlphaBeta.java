package edu.cwru.sepia.agent.minimax;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State;

public class MinimaxAlphaBeta extends Agent {

	private final int numPlys;
	public static boolean isMaxTurn = false;
	public static boolean f1Cornered = false;
	public static boolean f2Cornered = false;

	public MinimaxAlphaBeta(int playernum, String[] args) {
		super(playernum);

		if (args.length < 1) {
			System.err.println("You must specify the number of plys");
			System.exit(1);
		}

		numPlys = Integer.parseInt(args[0]);
	}

	@Override
	public Map<Integer, Action> initialStep(State.StateView newstate, History.HistoryView statehistory) {
		return middleStep(newstate, statehistory);
	}

	@Override
	public Map<Integer, Action> middleStep(State.StateView newstate, History.HistoryView statehistory) {

		GameStateChild bestChild = alphaBetaSearch(new GameStateChild(newstate), numPlys, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);

		return bestChild.action;
	}

	@Override
	public void terminalStep(State.StateView newstate, History.HistoryView statehistory) {

	}

	@Override
	public void savePlayerData(OutputStream os) {

	}

	@Override
	public void loadPlayerData(InputStream is) {

	}

	/**
	 * You will implement this.
	 *
	 * This is the main entry point to the alpha beta search. Refer to the
	 * slides, assignment description and book for more information.
	 *
	 * Try to keep the logic in this function as abstract as possible (i.e. move
	 * as much SEPIA specific code into other functions and methods)
	 *
	 * @param node
	 *            The action and state to search from
	 * @param depth
	 *            The remaining number of plys under this node
	 * @param alpha
	 *            The current best value for the maximizing node from this node
	 *            to the root
	 * @param beta
	 *            The current best value for the minimizing node from this node
	 *            to the root
	 * @return The best child of this node with updated values
	 */
	public GameStateChild alphaBetaSearch(GameStateChild node, int depth, double alpha, double beta) {

		isMaxTurn = !isMaxTurn;

		if (node.state.lifeExpectancy == Double.MIN_VALUE || node.state.lifeExpectancy == Double.MAX_VALUE
				|| depth == 0) {
			return node;
		}

		GameStateChild bestNode = null;
		GameStateChild bestCurrentNode = null;
		double val;
		List<GameStateChild> children = orderChildrenWithHeuristics(node.state.getChildren());

		if (isMaxTurn) {
			val = Double.NEGATIVE_INFINITY;

			for (GameStateChild child : children) {

				if (child.state.getUtility() > val) {
					val = child.state.getUtility();
					bestNode = alphaBetaSearch(child, depth - 1, alpha, beta);
					bestCurrentNode = child;
					bestCurrentNode.state.utility = bestNode.state.utility;
				}

				alpha = Math.max(alpha, val);

				if (beta <= alpha) {
					break;
				}
			}
		} else {
			val = Double.POSITIVE_INFINITY;

			for (GameStateChild child : children) {

				if (child.state.getUtility() < val) {
					val = child.state.getUtility();
					bestNode = alphaBetaSearch(child, depth - 1, alpha, beta);
					bestCurrentNode = child;
					bestCurrentNode.state.utility = bestNode.state.utility;
				}

				beta = Math.min(beta, val);

				if (beta <= alpha) {
					break;
				}
			}
		}

		return bestCurrentNode;
	}

	/**
	 * You will implement this.
	 *
	 * Given a list of children you will order them according to heuristics you
	 * make up. See the assignment description for suggestions on heuristics to
	 * use when sorting.
	 *
	 * Use this function inside of your alphaBetaSearch method.
	 *
	 * Include a good comment about what your heuristics are and why you chose
	 * them.
	 *
	 * @param children
	 * @return The list of children sorted by your heuristic.
	 */
	public List<GameStateChild> orderChildrenWithHeuristics(List<GameStateChild> children) {

		List<GameStateChild> orderedChildren = new ArrayList<GameStateChild>();

		if (isMaxTurn) {
			for (int i = 0; i < children.size(); i++) {
				double max = Double.NEGATIVE_INFINITY;
				GameStateChild bestChild = null;
				for (int j = 0; j < children.size(); j++) {

					double utility = children.get(j).state.getUtility();

					if (utility > max) {
						max = utility;
						bestChild = children.get(j);
					}
				}
				orderedChildren.add(bestChild);
				children.remove(bestChild);
			}
		} else {
			for (int i = 0; i < children.size(); i++) {
				double min = Double.POSITIVE_INFINITY;
				GameStateChild bestChild = null;
				for (int j = 0; j < children.size(); j++) {

					double utility = children.get(j).state.getUtility();

					if (utility < min) {
						min = utility;
						bestChild = children.get(j);
					}
				}
				orderedChildren.add(bestChild);
				children.remove(bestChild);
			}
		}
		return orderedChildren;
	}
}
