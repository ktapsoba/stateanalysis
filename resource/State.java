package resource;

import java.util.ArrayList;
import java.util.List;


public class State  {
	private final String name;
	private final List<Action> actions;
	private static final State  Top = new State("Top");
	private static final State Bottom = new State("Bottom");
	
	public State(){
		this.name = "No State";
		actions = new ArrayList<>();
	}
	
	public State(String name){
		this.name = name;
		actions = new ArrayList<>();
	}
	
	public State(String name, List<Action> actions){
		this.name = name;
		this.actions = actions;
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

	@Override
	public boolean equals(Object object) {
		if(object instanceof State){
			State state = (State)object;
			return state.name.equals(name) && actions.containsAll(actions);
		}
		return false;
	}
	
}
