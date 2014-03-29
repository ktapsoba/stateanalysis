package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resource.Configuration;
import resource.State;
import soot.G;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class CPAnalysis extends ForwardFlowAnalysis<Unit, List<State>>{

	private Map<Unit, List<State>> statesInByUnit;
	private Map<Unit, List<State>> statesOutByUnit;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalyzed = new ArrayList<>();
	private Configuration config = new Configuration();
	
	public CPAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		statesInByUnit = new HashMap<>();
		statesOutByUnit = new HashMap<>();
		
		setConfigurations();
		doAnalysis();
		printResults();
	}
	
	public void setConfigurations(){
		// Set up Methods
		config.AddNewMethod("DriverManager", "getConnection");
		config.AddNewMethod("Connection", "createStatement");
		config.AddNewMethod("Statement", "executeQuery");
		config.AddNewMethod("ResultSet", "close");
		config.AddNewMethod("Statement", "close");
		config.AddNewMethod("Connection", "close");
		
		// Set up Actions
		config.AddNewAction(config.getMethod("DriverManager", "getConnection"));
		config.AddNewAction(config.getMethod("Connection", "createStatement"));
		config.AddNewAction(config.getMethod("Statement", "executeQuery"));
		config.AddNewAction(config.getMethod("ResultSet", "close"));
		config.AddNewAction(config.getMethod("Statement", "close"));
		config.AddNewAction(config.getMethod("Connection", "close"));
		
		// Set up States
		/**Connected**/
		config.AddNewState("Connected");
		config.AddActionToState("Connected", config.getAction(config.getMethod("DriverManager", "getConnection")));
		
		/**NotConnected**/
		config.AddNewState("NotConnected");
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("ResultSet", "close")));
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("Statement", "close")));
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("Connection", "close")));
		
		/**Statement**/
		config.AddNewState("Statement");
		config.AddActionToState("Statement", config.getAction(config.getMethod("Connection", "createStatement")));
		
		/**Statement**/
		config.AddNewState("Result");
		config.AddActionToState("Result", config.getAction(config.getMethod("Statement", "executeQuery")));
		
		// Set up Transitions
		config.AddNewTransition("NotConnected", "Connected", config.getAction(config.getMethod("DriverManager", "getConnection")));
		config.AddNewTransition("NotConnected", "NotConnected", config.getAction(config.getMethod("Connection", "close")));
		config.AddNewTransition("NotConnected", "NotConnected", config.getAction(config.getMethod("ResultSet", "close")));
		config.AddNewTransition("NotConnected", "NotConnected", config.getAction(config.getMethod("Statement", "close")));
		config.AddNewTransition("Connected", "NotConnected", config.getAction(config.getMethod("Connection", "close")));
		config.AddNewTransition("Connected", "Statement", config.getAction(config.getMethod("Connection", "createStatement")));
		config.AddNewTransition("Statement", "Result", config.getAction(config.getMethod("Statement", "executeQuery")));
		config.AddNewTransition("Statement", "NotConnected", config.getAction(config.getMethod("Statement", "close")));
		config.AddNewTransition("Statement", "Statement", config.getAction(config.getMethod("Connection", "createStatement")));
		config.AddNewTransition("Result", "NotConnected", config.getAction(config.getMethod("ResultSet", "close")));
	}

	@Override
	protected void flowThrough(List<State> input, Unit unit, List<State> output) {
		unitAnalyzed.add(unit);
		saveStateByUnit(unit, input, statesInByUnit);
		copy(input, output);
		Stmt stmt = (Stmt)unit;
		Visitor.getInstance().visit(stmt, input, output, config);
		saveStateByUnit(unit, output, statesOutByUnit);
	}

	@Override
	protected void copy(List<State> input, List<State> output) {
		for(State state : input){
			if (!output.contains(state)){
				output.add(state);
			}
		}
	}

	@Override
	protected List<State> entryInitialFlow() {
		return new ArrayList<State>();
	}

	@Override
	protected void merge(List<State> input1, List<State> input2, List<State> output) {
		copy(input1, output);
		
		for(State state : input2){
			if (!input1.contains(state)){
				output.add(state);
			}
		}
	}

	@Override
	protected List<State> newInitialFlow() {
		return new ArrayList<State>();
	}
	
	private void saveStateByUnit(Unit unit, List<State> states, Map<Unit, List<State>> unitByState){
		List<State> newStates = new ArrayList<>();
		newStates.addAll(states);
		unitByState.put(unit, newStates);
	}
	
	private void printResults(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		for(Unit unit : units){
			if(unitAnalyzed.contains(unit)){
			G.v().out.println("\nUnit : " + unit.toString());
			G.v().out.println("Before : " + statesInByUnit.get(unit).toString());
			G.v().out.println("After : " + statesOutByUnit.get(unit).toString());
			}
		}
	}
	
	private void checkTransitions(){
		PatchingChain<Unit> units = unitGraph.getBody().getUnits();
		G.v().out.println("\n\n/*******************************RESULTS***********************/");
		for(Unit unit : units){
			List<State> stateIn = statesInByUnit.get(unit);
			List<State> stateOut = statesOutByUnit.get(unit);
			
			G.v().out.println("Checking unit " + unit.toString());
			G.v().out.println("IN :  " + stateIn.toString());
			G.v().out.println("OUT :  " + stateOut.toString());
			
			/*for(Local key : stateOutByLocal.keySet()){
				if (stateInByLocal.containsKey(key)){
					State stateIn = stateInByLocal.get(key);
					State stateOut = stateOutByLocal.get(key);
					Method method = Visitor.getInstance().getMethodByStmt().get((Stmt)unit);
					boolean isValid = config.checkTransition(stateIn, stateOut, config.getAction(method));
					if (!isValid){
						G.v().out.println("Transition ERROR for local " + key.toString());
						G.v().out.println("Transition In: " + stateIn.toString() + " -- Transition Out: " + stateOut);
					}
				}
			}*/			
		}
	}

}
