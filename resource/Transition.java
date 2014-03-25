package resource;

public class Transition {
	
	private final State fromState;
	private final State toState;
	private final Action action;
	
	public Transition(State fromState, State toState, Action action){
		this.fromState = fromState;
		this.toState = toState;
		this.action = action;
	}
	
	public State getOutState(){
		return toState;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Transition){
			Transition transition = (Transition)object;
			return transition.fromState.equals(fromState) && transition.toState.equals(toState) && transition.action.equals(action);
		}
		return false;
	}
	
	
	
}