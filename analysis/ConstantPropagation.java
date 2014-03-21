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
import resource.State;
import resource.Transition;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, State>{
	
	private Map<Unit, State> stateByUnitIn;
	private Map<Unit, State> stateByUnitOut;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	
	public ConstantPropagation(DirectedGraph<Unit> graph){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		stateByUnitIn = new HashMap<Unit, State>();
		stateByUnitOut = new HashMap<Unit, State>();
		
		doAnalysis();
		printResults();
		//checkTransitions();
	}
	
	@Override
	protected State newInitialFlow() {
		return new State();
	}
	
	@Override
	protected State entryInitialFlow() {
		return new State();
	}

	@Override
	protected void flowThrough(State input, Unit unit, State output) {
		
		unitAnalysed.add(unit);
		saveStateByUnit(unit, input, stateByUnitIn);
		
		copy(input, output);
		Stmt stmt = (Stmt) unit;
		Visitor.getInstance().visit(stmt, input, output);
		
		saveStateByUnit(unit, output, stateByUnitOut);
	}

	
	@Override
	protected void copy(State input, State output) {
		for(Local local : input.keySet()){
			output.put(local, input.get(local));
		}
	}

	@Override
	protected void merge(State input1, State input2, State output) {
		copy(input1, output);
		
		output = input1.lub(input2);
	}
	
	private void saveStateByUnit(Unit unit, State state, Map<Unit, State> unitByState){
		State newState = new State();
		for(Local local : state.keySet()){
			newState.put(local, state.get(local));
		}
		unitByState.put(unit, newState);
	}
	
	private void printResults(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		for(Unit unit : units){
			if(unitAnalysed.contains(unit)){
			G.v().out.println("\nUnit : " + unit.toString());
			G.v().out.println("Before : " + stateByUnitIn.get(unit).toString());
			G.v().out.println("After : " + stateByUnitOut.get(unit).toString());
			}
			else{
				G.v().out.println("MMMMMMMMMMMMMMMMMMM " + unit.toString());
			}
		}
	}
	
	private void checkTransitions(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		for(Unit unit : units){
			G.v().out.println("Checking unit " + unit.toString());
			G.v().out.println("IN :  " + stateByUnitIn.get(unit));
			G.v().out.println("OUT :  " + stateByUnitOut.get(unit));
			Transition.getInstance().checkStateStmt((Stmt)unit, stateByUnitIn.get(unit), stateByUnitOut.get(unit));
		}
	}
	
}
