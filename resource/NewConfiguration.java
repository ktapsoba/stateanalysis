package resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewConfiguration {
	private static Map<String, State> statesByName = new HashMap<>();
	private static Map<Method, Action> actionsByMethod = new HashMap<>();
	private static Map<String, Method> methodsByName = new HashMap<>();
	private static Set<Transition> transitions = new HashSet<>();
	
	public static void addNewMethod(String className, String methodName){
		if (className.isEmpty() || methodName.isEmpty())
			return;
		Method method = new Method(className, methodName);
		methodsByName.put(className + methodName, method);
	}
	
	public static void addNewAction(Method method){
		if(method == null)
			return;
		Action action = new Action(method);
		actionsByMethod.put(method, action);
	}
	
	public static void addNewState(String name, int level){
		if (name == null || name.isEmpty())
			return;
		State state = new State(name, level);
		statesByName.put(name, state);
	}
	
	public static void addActionToState(String stateName, Action action){
		if (stateName == null || stateName.isEmpty() || action == null)
			return;
		if (statesByName.containsKey(stateName)){
			State state = statesByName.get(stateName);
			state.addAction(action);
			statesByName.put(stateName, state);
		}
	}
	
	public static void addNewState(String name, int level, List<Action> actions){
		if(statesByName.isEmpty()){
			statesByName.put("Top", State.getTop());
			statesByName.put("Bottom", State.getBottom());
			statesByName.put("Null", State.getNull());
		}
		if(name == null || name.isEmpty())
			return;
		State state = new State(name, level, actions);
		statesByName.put(name, state);
	}
	
	public static void addNewTransition(String inStateName, String outStateName, Action action){
		if(inStateName == null || outStateName == null || action == null || inStateName.isEmpty() || outStateName.isEmpty())
			return;
		addNewTransition(statesByName.get(inStateName), statesByName.get(outStateName), action);
	}
	
	public static void addNewTransition(State inState, State outState, Action action){
		if (inState == null || outState == null || action == null)
			return;
		Transition transition = new Transition(inState, outState, action);
		transitions.add(transition);
	}
	
	/*
	 * Contains
	 */
	public static boolean containsMethod(Method method){
		return methodsByName.containsKey(method.getClassName() + method.getName());
	}
	public static boolean containsAction(Method method){
		return actionsByMethod.containsKey(method);
	}
	
	/*
	 * Getters
	 */
	public static Method getMethod(String className, String name){
		return methodsByName.get(className + name);
	}
	public static Action getAction(Method method){
		return actionsByMethod.get(method);
	}
	
	public static Set<State> getStatesByAction(Action action){
		Set<State> states = new HashSet<>();
		for(String key : statesByName.keySet()){
			if(statesByName.get(key).containsAction(action)){
				states.add(statesByName.get(key));
			}
		}
		return states;
	}
	
	public static State getHighestState(Set<State> states){
		State highestState = State.getBottom();
		if(states != null){
			for(State state : states){
				highestState = state.lub(highestState);
			}
		}
		return highestState;
	}
	
	/*
	 * Checking Transition
	 */
	public static boolean checkTransition(State inState, State outState, Action action){
		if(inState.equals(outState)){
			return true;
		}
		else if (inState == State.getNull() && action != null){
			return false;
		}
		/*else if(inState == State.getBottom()){
			return true;
		}*/
		Transition transition = new Transition(inState, outState, action);
		return transitions.contains(transition);
	}
	
}
