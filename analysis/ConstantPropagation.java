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
import resource.State;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, Map<Local, State>>{
	
	private Map<Unit, Map<Local, State>> stateByUnitIn;
	private Map<Unit, Map<Local, State>> stateByUnitOut;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	private Configuration config;
	
	public ConstantPropagation(DirectedGraph<Unit> graph, Configuration config){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		stateByUnitIn = new HashMap<>();
		stateByUnitOut = new HashMap<>();
		this.config = config;
		
		doAnalysis();
		printResults();
	}
	
	@Override
	protected Map<Local,State> newInitialFlow() {
		return new HashMap<Local,State>();
	}
	
	@Override
	protected Map<Local, State> entryInitialFlow() {
		return new HashMap<Local,State>();
	}

	@Override
	protected void flowThrough(Map<Local,State> input, Unit unit, Map<Local,State> output) {
		
		unitAnalysed.add(unit);
		saveStateByUnit(unit, input, stateByUnitIn);
		copy(input, output);
		Stmt stmt = (Stmt) unit;
		Visitor.getInstance().visit(stmt, input, output, config);
		saveStateByUnit(unit, output, stateByUnitOut);
	}

	
	@Override
	protected void copy(Map<Local,State> input, Map<Local,State> output) {
		for(Local key: input.keySet()){
			output.put(key, input.get(key));
		}
	}

	@Override
	protected void merge(Map<Local,State> input1, Map<Local,State> input2, Map<Local,State> output) {
		copy(input1, output);
		
		//get least upper bound
		for(Local key : input2.keySet()){
			if(input1.containsKey(key)){
				State stateInput1 = input1.get(key);
				State stateInput2 = input2.get(key);
				
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
	
	private void saveStateByUnit(Unit unit, Map<Local,State> state, Map<Unit, Map<Local,State>> unitByState){
		Map<Local,State> newState = new HashMap<>();
		for(Local local : state.keySet()){
			newState.put(local, state.get(local));
		}
		unitByState.put(unit, newState);
	}
	
	private void printResults(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		G.v().out.println("\n\n/*******************************RESULTS***********************/");
		for(Unit unit : units){
			if(unitAnalysed.contains(unit)){
			G.v().out.println("\nUnit : " + unit.toString());
			G.v().out.println("Before : " + stateByUnitIn.get(unit).toString());
			G.v().out.println("After : " + stateByUnitOut.get(unit).toString());
			}
		}
	}
	
}
