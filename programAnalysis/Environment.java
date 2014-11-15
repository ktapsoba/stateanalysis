package programAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.State;
import soot.Local;
import soot.jimple.Stmt;

public class Environment {
	
	private Map<Stmt, Map<Local, Set<State>>> localStatesByStmt;
	
	public Environment(){
		localStatesByStmt = new HashMap<>();
	}
	
	public Environment(Environment environment){
		this.localStatesByStmt = environment.localStatesByStmt;
	}
	
	public List<State> getStates(Stmt stmt, Local local){
		List<State> states = new ArrayList<>();
		if(localStatesByStmt.containsKey(stmt)){
			states.addAll(localStatesByStmt.get(stmt).get(local));
		}
		return states;
	}
	
	public void updateLocal(Stmt stmt, Local local, Set<State> outputStates) {
		Map<Local, Set<State>> entry = localStatesByStmt.containsKey(stmt)? localStatesByStmt.get(stmt) : new HashMap<Local, Set<State>>();
		entry.put(local, outputStates);
		localStatesByStmt.put(stmt, entry);
		//G.v().out.println(this);
	}
	
	public void addStmtState(Stmt stmt, Map<Local, Set<State>> states){
		this.localStatesByStmt.put(stmt, states);
	}
	
	public void addSuccessor(Stmt stmt, Stmt successor) {
		if(localStatesByStmt.containsKey(stmt))
			localStatesByStmt.put(successor, localStatesByStmt.get(stmt));
	}
	
	public String toString() {
		String ret = "{";
		for(Stmt stmt : localStatesByStmt.keySet()){
			ret += stmt.toString() + " <<" + localStatesByStmt.get(stmt) + ">>, ";
		}
		ret += "}";
		return ret;
	}

}
