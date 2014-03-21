package resource;

public class Transition {
	
	private final State inState;
	private final State outState;
	private final Method method;
	
	public Transition(State inState, State outState, Method method){
		this.inState = inState;
		this.outState = outState;
		this.method = method;
	}
	
	public State getOutState(){
		return outState;
	}
}