package verification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.Action;
import resource.Method;
import resource.State;
import resource.Transition;

public class Configuration {
	private static Map<String, Method> methodsByName = new HashMap<>();
	private static Map<String, Action> actionsByName = new HashMap<>();
	private static Map<String, State> statesByName = new HashMap<>();
	private static Map<String, Transition> transitionsByName = new HashMap<>();
	private static Map<Method, Action> actionsByMethod = new HashMap<>();

	public static void addNewMethod(String className, String methodName) {
		if (className.isEmpty() || methodName.isEmpty())
			return;
		Method method = new Method(className, methodName);
		methodsByName.put(className + methodName, method);
	}
	
	public static void addNewAction(String name) {
		if(name == null)
			return;
		Action action = new Action(name);
		actionsByName.put(name, action);
	}

	public static void addMethodToAction(String actionName, Method method) {
		if (method == null || actionName == null)
			return;
		if(actionsByName.containsKey(actionName)){
			Action action = actionsByName.get(actionName);
			action.addMethod(method);
			actionsByName.put(actionName, action);
		}
	}

	public static void addNewState(String name) {
		if (name == null || name.isEmpty())
			return;
		State state = new State(name);
		statesByName.put(name, state);
	}

	public static void addActionToState(String stateName, Action action) {
		if (stateName == null || stateName.isEmpty() || action == null)
			return;
		if (statesByName.containsKey(stateName)) {
			State state = statesByName.get(stateName);
			state.addAction(action);
			statesByName.put(stateName, state);
		}
	}

	public static void addNewTransition(String name, String inStateName,
			String outStateName, String actionName) {
		if (inStateName == null || outStateName == null || actionName == null
				|| inStateName.isEmpty() || outStateName.isEmpty() || actionName.isEmpty())
			return;
		Action action = actionsByName.get(actionName);
		State inState = statesByName.get(inStateName);
		State outState = statesByName.get(outStateName);
		
		Transition transition = new Transition(name, inState, outState, action);
		transitionsByName.put(name, transition);
	}

	/*
	 * Contains
	 */
	public static boolean containsMethod(Method method) {
		return methodsByName.containsKey(method.getClassName()
				+ method.getName());
	}

	public static boolean containsAction(Method method) {
		for(Action action : actionsByName.values()){
			if(action.hasMethod(method)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean containsTransition(State inState, State outState, Action action){
		Transition transition = new Transition("", inState, outState, action);
		return transitionsByName.values().contains(transition);
	}

	/*
	 * Getters
	 */
	public static Method getMethod(String className, String name) {
		return methodsByName.get(className + name);
	}
	
	public static Action getAction(String name){
		return actionsByName.get(name);
	}
	
	public static List<Action> getActions(Method method){
		List<Action> actions = new ArrayList<>();
		for(Action action : actionsByName.values()){
			if(action.hasMethod(method)){
				actions.add(action);
			}
		}
		return actions;
	}

	public static Set<State> getStatesByAction(Action action) {
		Set<State> states = new HashSet<>();
		for(State state : statesByName.values()){
			if(state.hasAction(action)){
				states.add(state);
			}
		}
		return states;
	}

	/*public static State getHighestState(Set<State> states) {
		State highestState = State.getBottom();
		if (states != null) {
			for (State state : states) {
				highestState = state.lub(highestState);
			}
		}
		return highestState;
	}*/

	/*
	 * Checking Transition
	 */
	public static boolean checkTransition(State inState, State outState, Action action) {
		if(containsTransition(inState, outState, action)){
			return true;
		} else {
			if (inState == State.getBottom()){
				return true;
			}
		}
		return false;
	}
}
