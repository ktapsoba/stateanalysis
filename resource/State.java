package resource;

import java.util.ArrayList;
import java.util.List;


public class State  {
	private final String name;
	private final Integer level;
	private final List<Action> actions;
	private static final State  Top = new State("Top", Integer.MAX_VALUE);
	private static final State Bottom = new State("Bottom", Integer.MIN_VALUE);
	private static final State Null = new State("Null", null);
	
	public State(){
		this.name = "No State";
		actions = new ArrayList<>();
		level = -1;
	}
	
	public State(String name, Integer level){
		this.name = name;
		actions = new ArrayList<>();
		this.level = level;
	}
	
	public State(String name, int level, List<Action> actions){
		this.name = name;
		this.actions = actions;
		this.level = level;
	}
	
	public void addAction(Action action){
		actions.add(action);
	}
	
	public String toString(){
		return name;
	}
	
	public boolean containsAction(Action action){
		return actions.contains(action);
	}
	
	public static State getTop() { return Top; }
	public static State getBottom() { return Bottom; }
	public static State getNull() { return Null; }

	@Override
	public boolean equals(Object object) {
		if(object instanceof State){
			State state = (State)object;
			return state.name.equals(name) && actions.containsAll(actions);
		}
		return false;
	}
	
	public State lub(State state){
		if (level == null){
			return state;
		}
		if (state.level == null){
			return this;
		}
		if(level < state.level)
			return state;
		else return this;
	}

	@Override
	public int hashCode() {
		return name.hashCode()* level;
	}
	
	
	
}
