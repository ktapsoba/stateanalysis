package analysis;

import resource.Action;
import resource.State;
import soot.jimple.Stmt;


public class InvalidCallError extends RuntimeException{
	private static final long serialVersionUID = 1L;

	InvalidCallError(String message, State inState, Stmt stmt, Action action){
		super("**************ERROR " + message + " : " + stmt.toString()
				+ "\n Current state:" + inState.toString()
				+ "\n Action :" + action.toString());
	}
}