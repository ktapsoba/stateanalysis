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
	
	public boolean AddNewState(String name){
		if (name == null || name.isEmpty())
			return false;
		State state = new State(name);
		statesByName.put(name, state);
		return true;
	}
	
	public boolean AddActionToState(String stateName, Action action){
		if (stateName == null || stateName.isEmpty() || action == null)
			return false;
		if (statesByName.containsKey(stateName)){
			State state = statesByName.get(stateName);
			state.addAction(action);
			statesByName.put(stateName, state);
			return true;
		}
		return false;
	}
	
	public boolean AddNewState(String name, List<Action> actions){
		if(name == null || name.isEmpty())
			return false;
		State state = new State(name, actions);
		statesByName.put(name, state);
		return true;
	}
	
	public boolean AddNewTransition(String inStateName, String outStateName, Action action){
		if(inStateName == null || outStateName == null || action == null || inStateName.isEmpty() || outStateName.isEmpty())
			return false;
		return AddNewTransition(statesByName.get(inStateName), statesByName.get(outStateName), action);
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
	
	public State getTopState(){
		return State.getTop();
	}
	public State getBottomState(){
		return State.getBottom();
	}
	
	public List<State> getStatesByAction(Action action){
		List<State> states = new ArrayList<>();
		for(String key : statesByName.keySet()){
			if(statesByName.get(key).containsAction(action)){
				states.add(statesByName.get(key));
			}
		}
		return states;
	}
	
	public List<ContextualState> getContextualStatesByActioin(Context ctx, Action action){
		List<ContextualState> ctxState = new ArrayList<>();
		List<State> states = getStatesByAction(action);
		for(State state : states){
			ctxState.add(new ContextualState(ctx, state));
		}
		return ctxState;
	}
	
	public List<ContextualState> getContextualStatesByAction(Action action){
		List<ContextualState> ctxState = new ArrayList<>();
		List<State> states = getStatesByAction(action);
		Context ctx = Context.getNewContext();
		for(State state : states){
			ctxState.add(new ContextualState(ctx, state));
		}
		return ctxState;
	}

	public String Stats(){
		String ret = "COUNTS";
		ret += "\nMethods: " + methodsByName.size();
		ret += "\nActions: " + actionsByMethod.size();
		ret += "\nStates: " + statesByName.size();
		ret += "\nTransitions: " + transitions.size();
		return ret;
	}
	
	/*
	 * Checking Transition
	 */
	public boolean checkTransition(State inState, State outState, Action action){
		if (inState.equals(outState)){
			return true;
		}
		//anything going from bottom to something else is valid
		else if (inState.equals(getBottomState())){
			return true;
		}
		Transition transition = new Transition(inState, outState, action);
		return transitions.contains(transition);
	}
	
	public boolean checkTransition(ContextualState inState, ContextualState outState, Action action){
		if (inState.getContext().equals(outState.getContext())){
			return checkTransition(inState.getState(), outState.getState(), action);
		}
		return false;
	}
}
