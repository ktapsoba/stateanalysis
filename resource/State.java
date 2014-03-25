package resource;

import java.util.ArrayList;
import java.util.List;


public class State  {
	private final String name;
	private final List<Action> actions;
	
	public State(){
		this.name = "No State";
		actions = new ArrayList<>();
	}
	
	public State(String name, List<Action> actions){
		this.name = name;
		this.actions = actions;
	}
	
	public String toString(){
		return name;
	}
	
	public boolean containsAction(Action action){
		return actions.contains(action);
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof State){
			State state = (State)object;
			return state.name.equals(name) && actions.containsAll(actions);
		}
		return false;
	}
	
}
