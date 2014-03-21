package resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.G;
import soot.Local;

abstract class StateType {
	final String name;
	
	StateType(String name){ this.name = name; }
	abstract StateType lub(StateType state);
	abstract boolean lessThan(StateType state);
	boolean isTop() {return false;}
	boolean isBottom() {return false;}
	boolean isNotConnectd(){ return false; }
	boolean isConnected() { return false; }
	boolean isResult() { return false; }
	boolean isStatement() {return false;}
	
	boolean isLoggedIn() {return false;}
	boolean isLoggedOut() {return false;}
	
	public String toString(){ return name; }
}

class Top extends StateType{
	static private Top top = new Top();
	
	private Top(){ super("top"); }
	static Top getTop() { return top; }
	StateType lub(StateType state) { return this; }
	boolean lessThan(StateType state) { return this == state; }
	boolean isTop() { return true; }

}

class Bottom extends StateType{
	static private Bottom bottom = new Bottom();
	
	private Bottom(){ super("bottom"); }
	static Bottom getBottom() { return bottom; }
	StateType lub(StateType state) { return state; }
	boolean lessThan(StateType state) { return true; }
	boolean isBottom() { return true; }
	
}

class NotConnected extends StateType {
	static private NotConnected notConnected = new NotConnected();
	
	private NotConnected(){ super("not Connected"); }
	static NotConnected getNotConnected() { return notConnected; }
	boolean isNotConnected() { return true; }
	StateType lub(StateType state) { 
		if (state.isTop() || state.isResult() || state.isConnected()) return state;
		else if (state.isBottom()) return this;
		else return this;
	}
	
	boolean lessThan(StateType state) { 
		if (state.isTop() || state.isResult() || state.isConnected()) return true;
		else if (state.isBottom()) return false;
		return true;
	}
}

class Connected extends StateType {
	static private Connected connected = new Connected();
	
	private Connected(){ super("connected"); }
	static Connected getConnected() { return connected; }
	boolean isConnected() { return true; }
	
	StateType lub(StateType state) { 
		if (state.isTop() || state.isResult()) return state;
		else if (state.isBottom() || state.isNotConnectd()) return this;
		else return this;
	}
	boolean lessThan(StateType state) { 
		if (state.isTop() || state.isResult()) return true;
		else if (state.isNotConnectd() || state.isBottom()) return false;
		else return true;
	}
}

class Statement extends StateType {
static private Statement statement = new Statement();
	
	private Statement(){ super("statement"); }
	static Statement getStatement() { return statement; }
	boolean isStatement() { return true; }
	
	StateType lub(StateType state) { 
		if (state.isTop() || state.isResult()) return state;
		else if (state.isBottom() || state.isNotConnectd() || state.isConnected()) return this;
		else return this;
	}
	boolean lessThan(StateType state) { 
		if (state.isTop() || state.isResult()) return true;
		else if (state.isBottom() || state.isNotConnectd() || state.isConnected()) return false;
		else return true;
	}
}

class Result extends StateType {
	static private Result result = new Result();
	
	private Result(){ super("result"); }
	static Result getResult() { return result; }
	boolean isResult() { return true; }
	
	StateType lub(StateType state) { 
		if (state.isTop()) return state;
		else if (state.isBottom() || state.isNotConnectd() || state.isConnected() || state.isStatement()) return this;
		else return this;
	}
	boolean lessThan(StateType state) { 
		if (state.isTop()) return true;
		else if (state.isBottom() || state.isNotConnectd() || state.isConnected()) return false;
		else return true;
	}
}

class LoggedIn extends StateType{
	static private LoggedIn loggedIn = new LoggedIn();
	
	private LoggedIn() {super("logged in");}
	static LoggedIn getLoggedIn() { return loggedIn; }
	boolean isLoggedIn() { return true;}
	
	StateType lub(StateType state){
		if (state.isTop()) return state;
		else return this;
	}
	boolean lessThan(StateType state){
		if(state.isTop()) return true;
		else if (state.isBottom() || state.isNotConnectd() || state.isConnected()) return false;
		else return true;
	}
}

class LoggedOut extends StateType {
	static private LoggedOut loggedOut = new LoggedOut();
	
	private LoggedOut() { super("logged out"); }
	static LoggedOut getLoggedOut() { return loggedOut; }
	boolean isLoggedOut() {return true;}
	
	StateType lub(StateType state){
		if(state.isTop() || state.isLoggedIn()) return state;
		else return this;
	}
	boolean lessThan(StateType state){
		if(state.isTop() || state.isLoggedIn()) return true;
		else return false;
	}
}

class InvalidState extends StateType{
	static private InvalidState invalidState = new InvalidState();
	
	private InvalidState(){ super("invalid State"); }
	static InvalidState getInvalidState() { return invalidState; }
	boolean isInvalidState() { return true; }
	
	StateType lub(StateType state) { 
		if (state.isTop() || state.isBottom() || state.isNotConnectd() || state.isConnected() || state.isStatement()) return state;
		else return this;
	}
	boolean lessThan(StateType state) { 
		return true;
	}
}


public class State extends HashMap<Local, StateType> {
	private static final long serialVersionUID = 1L;
	
	final StateType stateType;
	static StateType getTopState() { return Top.getTop(); }
	static StateType getBottomState() { return Bottom.getBottom(); }
	static StateType getInvalidState() { return InvalidState.getInvalidState(); }
	
	State(State state) { 
		super(state);
		this.stateType = state.stateType;
	}
	
	public State(){
		super();
		this.stateType = getBottomState();
	}
	
	public State lub(State state){
		ArrayList<Local> locals = new ArrayList<Local>();
		locals.addAll(this.keySet());
		locals.addAll(state.keySet());
		State newState = new State(this);
		
		for(Local local : locals){
			newState.put(local, this.get(local).lub(state.get(local)));
		}
		return newState;
	}
	
	boolean lessThan(State state, Method method){
		if (state != null){
			for(Local local : state.keySet()){
				if (this.containsKey(local)){
					//we have a transfer of statetype for the local
					if (this.get(local) == state.get(local)){
						G.v().out.println("NO STATE CHANGE");
					}
					// state change
					else {
						G.v().out.println("STATE CHANGE");
						if (method.getState() != state.get(local)){
							G.v().out.println("ERROR IN STATE CHANGE:" + state.get(local).toString() + " FOUND. EXPECTED" + method.getState().toString() );
							return false;
						}
						else {
							G.v().out.println("VALID STATE CHANGE " + local.toString());
						}
					}
					/*boolean isLessThan = this.get(local).lessThan(state.get(local));
					//if the previous state is less than 
					if (!isLessThan)
						return isLessThan;*/
				}
				else {
					G.v().out.println("new local " + local + "->" + state.get(local));
					if (state.get(local) != method.getState()){
						G.v().out.println("ERROR IN STATE CHANGE:" + state.get(local).toString() + " FOUND. EXPECTED" + method.getState().toString() );
						return false;
					}
				}
			}
		}
		return true;
	}
	
	StateType getDefault() { return stateType; }
	
	public String toString() {
		String value = "[";
		for(Local local : keySet()){
			value += local.toString() + "->" + get(local).toString() + ", ";
		}
		value +="]";
		return value;
	}
	
	State update (Local local, StateType type){
		State newState = new State(this);
		newState.put(local, type);
		return newState;
	}
	
	State update(List<Local> locals, List<StateType> types){
		State newState = new State(this);
		for(int i = 0; i < locals.size() && i < types.size(); i++){
			newState.put(locals.get(i), types.get(i));
		}
		return newState;
	}
	
	public StateType put(Local local, StateType type){
		return super.put(local, type);
	}
	
	public StateType put(Local local, Method method){
		return super.put(local, method.getState());
	}
	
	public StateType get(Object object){
		StateType type = stateType;
		if (object instanceof Local){
			Local local = (Local)object;
			if(this.containsKey(local)){
				type = super.get(local);
			}
		}
		return type;
	}
	
	public boolean containsLocal(String var){
		//G.v().out.println("varia " + var);
		for(Local local : this.keySet()){
			if(local.getName().equals(var))
				return true;
		}
		return false;
	}
	 
}
