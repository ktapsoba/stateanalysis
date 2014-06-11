package analysis;

import resource.Action;
import resource.State;
import soot.jimple.Stmt;


public class InvalidCallError extends RuntimeException{
	private static final long serialVersionUID = 1L;

	InvalidCallError(Stmt stmt, State inState, Action action){
		super("Invalid API method call at Statement " + stmt.toString() + " for the input state " + inState.toString()
				+ " and action :" + action);
	}
}