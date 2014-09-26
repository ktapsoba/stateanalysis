package programAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.State;
import soot.G;
import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.util.Chain;
import verification.InvalidAPICallException;

public class DataFlowAnalysis extends
		ForwardFlowAnalysis<Unit, Map<Local, Set<State>>> {
	private final UnitGraph unitGraph;
	private final ControlFlowGraph cfg;
	private final Map<Local, Set<State>> extremalValue;
	private final Chain<Local> localVariables;
	private StatementVisitor newVisitor = new StatementVisitor();
	private Map<Unit, Map<Local, Set<State>>> stateByUnitIn;
	private Map<Unit, Map<Local, Set<State>>> stateByUnitOut;
	private Environment environment;
	private DependencyMap dependencyMap;

	public DataFlowAnalysis(DirectedGraph<Unit> graph, Map<Local, Set<State>> extremalValue, Chain<Local> localVariables,
			ControlFlowGraph cfg, Environment environment, DependencyMap dependencyMap) {
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph) graph;
		this.cfg = cfg;
		this.extremalValue = extremalValue == null ? new HashMap<Local, Set<State>>()
				: extremalValue;
		this.localVariables = localVariables;
		this.stateByUnitIn = new HashMap<>();
		this.stateByUnitOut = new HashMap<>();
		this.environment = environment;
		this.dependencyMap = dependencyMap;
	}

	public void startAnalysis() {
		try{
			doAnalysis();
			G.v().out.println("No errors found in analysis using entry point "
					+ unitGraph.getBody().getMethod().toString());
		} catch (InvalidAPICallException ex){
			G.v().out.println("*****Errors Found!!!");
			G.v().out.println(ex.toString());
		} catch(Exception ex) {
			G.v().out.println("Unexpected Exception found!!!!");
			G.v().out.println(ex.getMessage());
		}
	}

	@Override
	protected Map<Local, Set<State>> newInitialFlow() {
		return new HashMap<Local, Set<State>>();
	}

	@Override
	protected Map<Local, Set<State>> entryInitialFlow() {
		return extremalValue;
	}

	@Override
	protected void flowThrough(Map<Local, Set<State>> input, Unit unit,
			Map<Local, Set<State>> output) throws InvalidAPICallException{

		G.v().out.println("INPUT--> " + input);
		saveStateByUnit(unit, input, stateByUnitIn);
		copy(input, output);
		//G.v().out.println("unit-->" + unit);
		Stmt stmt = (Stmt) unit;
		newVisitor.visit(stmt, input, output, cfg, localVariables, environment, dependencyMap);
		List<Unit> successors = unitGraph.getSuccsOf(unit);
		for(Unit succ : successors){
			Stmt succStmt = (Stmt) succ;
			environment.addSuccessor(stmt, succStmt);
		}
		saveStateByUnit(unit, output, stateByUnitOut);
		G.v().out.println("OUTPUT--> " + output);
	}

	@Override
	protected void copy(Map<Local, Set<State>> input,
			Map<Local, Set<State>> output) {
		for (Local key : input.keySet()) {
			output.put(key, input.get(key));
		}
	}

	@Override
	protected void merge(Map<Local, Set<State>> input1,
			Map<Local, Set<State>> input2, Map<Local, Set<State>> output) {
		copy(input1, output);

		Set<Local> keys = new HashSet<>();
		keys.addAll(input1.keySet());
		keys.addAll(input2.keySet());

		for (Local key : keys) {
			if (input1.containsKey(key) && input2.containsKey(key)) {
				Set<State> states = new HashSet<>();
				states.addAll(input1.get(key));
				states.addAll(input2.get(key));
				output.put(key, states);
			} else if (input1.containsKey(key)) {
				output.put(key, input1.get(key));
			} else if (input2.containsKey(key)) {
				output.put(key, input2.get(key));
			} else {
				G.v().out.println("THIS IS IMPOSSIBLE");
			}
		}
	}

	private void saveStateByUnit(Unit unit, Map<Local, Set<State>> state,
			Map<Unit, Map<Local, Set<State>>> unitByState) {
		Map<Local, Set<State>> newState = new HashMap<>();
		for (Local local : state.keySet()) {
			newState.put(local, state.get(local));
		}
		unitByState.put(unit, newState);
	}
	
	public Map<Local, Set<State>> getReturnStates(){
	    return newVisitor.getReturnStates();
	}
}
