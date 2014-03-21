package resource;

import java.util.List;
import java.util.Set;

public class Action {
	private Set<Method> validMethods;
	private Set<Method> invalidMethods;
	private final State state;
	
	public Action(State state){
		this.state = state;
	}
	
	public Action(State state, List<Method> validMethods, List<Method> invalidMethods){
		this.state = state;
		this.validMethods.addAll(validMethods);
		this.invalidMethods.addAll(invalidMethods);
	}
	
	public void addToValidMethod(Method method){
		validMethods.add(method);
	}
	
	public void addToInvalidMethod(Method method){
		invalidMethods.add(method);
	}
	
	public String toString(){
		return state.toString() + "\nValid Methods:" + validMethods.toString() + "\nInvalid Methods: " + invalidMethods.toString();
	}
}
