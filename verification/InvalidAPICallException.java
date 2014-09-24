package verification;

import java.util.List;

import resource.Action;
import resource.State;
import soot.jimple.Stmt;

public class InvalidAPICallException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidAPICallException(Stmt stmt, State inState, Action action) {
		super("Invalid API method call at Statement " + stmt.toString()
				+ " for the input state " + inState.toString()
				+ " and action :" + action);
	}
	
	public InvalidAPICallException(Stmt stmt, List<State> inStates, List<Action> actions){
		super("Invalid API method call at Statement " + stmt.toString()
				+ " for the input state " + inStates.toString()
				+ " and action :" + actions);
	}
}
