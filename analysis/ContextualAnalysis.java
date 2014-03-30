package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.G;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import resource.Configuration;
import resource.Method;
import resource.ContextualState;

public class ContextualAnalysis extends ForwardFlowAnalysis<Unit, Map<Local, ContextualState>>{
	
	private Map<Unit, Map<Local, ContextualState>> stateByUnitIn;
	private Map<Unit, Map<Local, ContextualState>> stateByUnitOut;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	private Configuration config;
	
	public ContextualAnalysis(DirectedGraph<Unit> graph, Configuration config){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		stateByUnitIn = new HashMap<>();
		stateByUnitOut = new HashMap<>();
		this.config = config;
		
		G.v().out.println("STATS");
		G.v().out.println(config.Stats());
		doAnalysis();
		printResults();
		//checkTransitions();
	}
	
	@Override
	protected Map<Local,ContextualState> newInitialFlow() {
		return new HashMap<Local,ContextualState>();
	}
	
	@Override
	protected Map<Local, ContextualState> entryInitialFlow() {
		return new HashMap<Local,ContextualState>();
	}

	@Override
	protected void flowThrough(Map<Local,ContextualState> input, Unit unit, Map<Local,ContextualState> output) {
		
		unitAnalysed.add(unit);
		saveContextualStateByUnit(unit, input, stateByUnitIn);
		copy(input, output);
		Stmt stmt = (Stmt) unit;
		ContextualVisitor.getInstance().visit(stmt, input, output, config);
		saveContextualStateByUnit(unit, output, stateByUnitOut);
	}

	
	@Override
	protected void copy(Map<Local,ContextualState> input, Map<Local,ContextualState> output) {
		for(Local key: input.keySet()){
			output.put(key, input.get(key));
		}
	}

	@Override
	protected void merge(Map<Local,ContextualState> input1, Map<Local,ContextualState> input2, Map<Local,ContextualState> output) {
		copy(input1, output);
		
		//get least upper bound
		for(Local key : input2.keySet()){
			if(input1.containsKey(key)){
				ContextualState stateInput1 = input1.get(key);
				ContextualState stateInput2 = input2.get(key);
				
				if (stateInput1.equals(stateInput2)){
					output.put(key, stateInput1);
				}
				else {
					output.remove(key);
				}
			}else {
				output.put(key, input2.get(key));
			}
		}
	}
	
	private void saveContextualStateByUnit(Unit unit, Map<Local,ContextualState> state, Map<Unit, Map<Local,ContextualState>> unitByContextualState){
		Map<Local,ContextualState> newContextualState = new HashMap<>();
		for(Local local : state.keySet()){
			newContextualState.put(local, state.get(local));
		}
		unitByContextualState.put(unit, newContextualState);
	}
	
	private void printResults(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		for(Unit unit : units){
			if(unitAnalysed.contains(unit)){
			G.v().out.println("\nUnit : " + unit.toString());
			G.v().out.println("Before : " + stateByUnitIn.get(unit).toString());
			G.v().out.println("After : " + stateByUnitOut.get(unit).toString());
			}
		}
	}
	
	private void checkTransitions(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		G.v().out.println("\n\n/*******************************RESULTS***********************/");
		for(Unit unit : units){
			Map<Local, ContextualState> stateInByLocal = stateByUnitIn.get(unit);
			Map<Local, ContextualState> stateOutByLocal = stateByUnitOut.get(unit);
			
			G.v().out.println("Checking unit " + unit.toString());
			G.v().out.println("IN :  " + stateInByLocal.toString());
			G.v().out.println("OUT :  " + stateOutByLocal.toString());
			
			for(Local key : stateOutByLocal.keySet()){
				if (stateInByLocal.containsKey(key)){
					ContextualState stateIn = stateInByLocal.get(key);
					ContextualState stateOut = stateOutByLocal.get(key);
					Method method = Visitor.getInstance().getMethodByStmt().get((Stmt)unit);
					boolean isValid = config.checkTransition(stateIn, stateOut, config.getAction(method));
					if (!isValid){
						G.v().out.println("Transition ERROR for local " + key.toString());
						G.v().out.println("Transition In: " + stateIn.toString() + " -- Transition Out: " + stateOut);
					}
				}
			}			
		}
	}
	
}
