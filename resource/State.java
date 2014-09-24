package resource;

import java.util.HashSet;
import java.util.Set;

public class State {
	private final String name;
	//private final Integer level;
	private final Set<Action> actions = new HashSet<>();
	private static final State Top = new State("Top");
	private static final State Bottom = new State("Bottom");
	private static final State Null = new State("Null");

	public State(String name) {
		this.name = name;
	}

	public void addAction(Action action) {
		actions.add(action);
	}

	public String toString() {
		return name;
	}

	public boolean hasAction(Action action) {
		return actions.contains(action);
	}

	public static State getTop() {
		return Top;
	}

	public static State getBottom() {
		return Bottom;
	}

	public static State getNull() {
		return Null;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof State) {
			State state = (State) object;
			return state.name.equals(name) && actions.containsAll(actions);
		}
		return false;
	}

	/*public State lub(State state) {
		if (level == null) {
			return state;
		}
		if (state.level == null) {
			return this;
		}
		if (level < state.level)
			return state;
		else
			return this;
	}*/

	@Override
	public int hashCode() {
		return name.hashCode() * 1234;
	}

}
