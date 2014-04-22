package analysis;

import java.util.List;

import resource.Action;
import resource.Configuration;
import resource.State;
import soot.Local;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

public class TransferFunction {
	Action action;
	State inState;
	State outState;
	Local local;
	
	
	public TransferFunction(State inState, Action action, Local local){
		this.action = action;
		this.inState = inState;
		this.local = local;
	}
	public TransferFunction(State inState, State outState, Action action, Local local){
		this(inState, action, local);
		this.outState = outState;
	}
	
	public State apply(Stmt stmt, Configuration config) throws InvalidCallError{
		if (stmt instanceof InvokeStmt){
			return applyInvoke(stmt, config);
		}
		if (stmt instanceof AssignStmt){
			return applyAssign(stmt, config);
		}
		throw new InvalidCallError("Cannot Evaluate Statement", inState, stmt, action);
	}
	
	private State applyInvoke(Stmt stmt, Configuration config){
		List<State> newStates = config.getStatesByAction(action);
		if(config.checkTransition(inState, newStates.get(0), action))
			return newStates.get(0);
		throw new InvalidCallError("Invalid method Call", inState, stmt, action);
	}
	
	private State applyAssign(Stmt stmt, Configuration config){
		if (outState == null){
			if (config.checkTransition(inState, outState, action))
				return outState;
		}
		else {
			List<State> newStates = config.getStatesByAction(action);
			if(config.checkTransition(inState, newStates.get(0), action))
				return newStates.get(0);
		}
		throw new InvalidCallError("Invalid method Call", inState, stmt, action);
	}
}
