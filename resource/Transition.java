package resource;

public class Transition {

	private final String name;
	private final State fromState;
	private final State toState;
	private final Action action;

	public Transition(String name, State fromState, State toState, Action action) {
		this.name = name;
		this.fromState = fromState;
		this.toState = toState;
		this.action = action;
	}

	public State getOutState() {
		return toState;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Transition) {
			Transition transition = (Transition) object;
			return transition.fromState.equals(fromState)
					&& transition.toState.equals(toState)
					&& transition.action.equals(action);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return fromState.hashCode() * toState.hashCode() * action.hashCode();
	}

}