package verification;

import java.util.HashSet;
import java.util.Set;

import resource.Action;
import resource.State;
import soot.jimple.Stmt;

public class TransferFunction {
	private State inState;
	private Stmt stmt;

	public TransferFunction(Stmt stmt, State inState) {
		this.inState = inState;
		this.stmt = stmt;
	}

	public Set<State> apply(Set<State> possibleStates, Action action) {
		Set<State> outputStates = new HashSet<>();
		for (State state : possibleStates) {
			if (Configuration.checkTransition(inState, state, action)) {
				outputStates.add(state);
			}
		}
		return outputStates;
	}
}
