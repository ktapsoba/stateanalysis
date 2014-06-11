package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.Configuration;
import resource.State;
import soot.G;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;


public class ConstantPropagation extends ForwardFlowAnalysis<Unit, Map<Local, State>>{
	
	private Map<Unit, Map<Local, State>> stateByUnitIn;
	private Map<Unit, Map<Local, State>> stateByUnitOut;
	private final UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	private final Configuration config;
	private final InterProceduralCFG cfg;
	private final Map<Local,State> extremalValue;
	private final Chain<Local> locals;
	private Unit RET_;
	private Visitor visitor;
	private List<String> errors = new ArrayList<>();
	
	public ConstantPropagation(DirectedGraph<Unit> graph, Map<Local, State> extremalValue, Configuration config, InterProceduralCFG cfg, Chain<Local> locals){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
	//	G.v().out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ " + unitGraph.getClass().getMethods().length);
		stateByUnitIn = new HashMap<>();
		stateByUnitOut = new HashMap<>();
		this.config = config;
		this.cfg = cfg;
		this.extremalValue = extremalValue == null? new HashMap<Local,State>(): extremalValue;
		this.locals = locals;
		//G.v().out.println("Locals --> " + this.locals.toString());
		visitor = new Visitor();
	}
	
	public Map<Local, State> getReturnStates(){
		return visitor.getReturnStates();
	}
	
	public void StartAnalysis(){
		doAnalysis();
		if(errors.size() > 0){
			G.v().out.println("Errors Found");
			for(String error: errors){
				G.v().out.println(error);
			}
		}
		else {
			G.v().out.println("No errors found in analysis using entry point " + unitGraph.getBody().getMethod().toString());
		}
		//printResults();
	}
	
	@Override
	protected Map<Local,State> newInitialFlow() {
		return new HashMap<Local,State>();
	}
	
	@Override
	protected Map<Local, State> entryInitialFlow() {
		return extremalValue;
	}

	@Override
	protected void flowThrough(Map<Local,State> input, Unit unit, Map<Local,State> output) {
		
		unitAnalysed.add(unit);
		saveStateByUnit(unit, input, stateByUnitIn);
		//G.v().out.println("INPUT " + input);
		
		copy(input, output);
		
		Stmt stmt = (Stmt) unit;
		//G.v().out.println("stmt " + stmt.toString());
		try{
			visitor.visit(stmt, input, output, config, cfg, locals);
		} catch(InvalidCallError error){
			errors.add(error.toString());
		}
		
		//G.v().out.println("OUTPUT " + output);
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
		
		Set<Local> keys = new HashSet<>();
		keys.addAll(input1.keySet());
		keys.addAll(input2.keySet());
		
		for(Local key : keys){
			if(input1.containsKey(key) && input2.containsKey(key)){
				State state1 = input1.get(key);
				State state2 = input2.get(key);
				
				State state = state1.lub(state2);
				output.put(key, state);
			}
			else if(input1.containsKey(key)){
				output.put(key, input1.get(key));
			}
			else if (input2.containsKey(key)){
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
		G.v().out.println("\n\n/*******************************RESUL***********************/");
	}
	
}
