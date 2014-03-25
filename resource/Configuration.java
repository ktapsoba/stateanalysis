package resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
	private Map<String, State> statesByName;
	private Map<Method, Action> actionsByMethod;
	private List<Transition> transitions;
	private Map<String, Method> methodsByName;
	
	/*
	 * Configure the File
	 */
	public Configuration(){
		statesByName = new HashMap<>();
		actionsByMethod = new HashMap<>();
		transitions = new ArrayList<>();
		methodsByName = new HashMap<>();
	}
	
	public boolean AddNewMethod(String className, String methodName){
		if (className.isEmpty() || methodName.isEmpty())
			return false;
		Method method = new Method(className, methodName);
		methodsByName.put(className + methodName, method);
		return true;
	}
	
	public boolean AddNewAction(Method method){
		if(method == null)
			return false;
		Action action = new Action(method);
		actionsByMethod.put(method, action);
		return true;
	}
	
	public boolean AddNewState(String name, List<Action> actions){
		if(name == null || name.isEmpty())
			return false;
		State state = new State(name, actions);
		statesByName.put(name, state);
		return true;
	}
	
	public boolean AddNewTransition(State inState, State outState, Action action){
		if (inState == null || outState == null || action == null)
			return false;
		Transition transition = new Transition(inState, outState, action);
		transitions.add(transition);
		return true;
	}
	
	/*
	 * Getters
	 */
	public Method getMethod(String className, String name){
		return methodsByName.get(className + name);
	}
	
	public Action getAction(Method method){
		return actionsByMethod.get(method);
	}
	
	public List<State> getStates(Action action){
		List<State> states = new ArrayList<>();
		for(String key : statesByName.keySet()){
			if(statesByName.get(key).containsAction(action)){
				states.add(statesByName.get(key));
			}
		}
		return states;
	}
	
	/*
	 * Checking Transition
	 */
	public boolean checkTransition(State inState, State outState, Action action){
		Transition transition = new Transition(inState, outState, action);
		return transitions.contains(transition);
	}
}
