package verification;

import java.util.List;

import resource.Action;
import resource.State;
import soot.G;
import soot.jimple.Stmt;
import soot.tagkit.LineNumberTag;

public class InvalidAPICallException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidAPICallException(Stmt stmt, State inState, Action action) {
		super("Invalid API method call at Statement " + stmt.toString()
				+ " for the input state " + inState.toString()
				+ " and action :" + action);
		getLineNumber(stmt);
	}
	
	public InvalidAPICallException(Stmt stmt, List<State> inStates, List<Action> actions){
		super("Invalid API method call at Statement " + stmt.toString()
				+ " for the input state " + inStates.toString()
				+ " and action :" + actions);
		getLineNumber(stmt);
	}
	
	private void getLineNumber(Stmt stmt) {
		LineNumberTag lineNumberTag =  (LineNumberTag) stmt.getTag("LineNumberTag");
		G.v().out.println("tag " + lineNumberTag);
		if(lineNumberTag != null){
			G.v().out.println("line " + lineNumberTag.getLineNumber());
		}
	}
}
