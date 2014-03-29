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
import resource.Action;
import resource.Configuration;
import resource.Method;
import resource.State;

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, Map<Local, State>>{
	
	private Map<Unit, Map<Local, State>> stateByUnitIn;
	private Map<Unit, Map<Local, State>> stateByUnitOut;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	private Configuration config = new Configuration();
	
	public ConstantPropagation(DirectedGraph<Unit> graph){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		stateByUnitIn = new HashMap<>();
		stateByUnitOut = new HashMap<>();
		setConfigs();
		
		G.v().out.println("STATS");
		G.v().out.println(config.Stats());
		doAnalysis();
		printResults();
		checkTransitions();
	}
	
	public void setConfigs(){
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
			Map<Local, State> stateInByLocal = stateByUnitIn.get(unit);
			Map<Local, State> stateOutByLocal = stateByUnitOut.get(unit);
			
			G.v().out.println("Checking unit " + unit.toString());
			G.v().out.println("IN :  " + stateInByLocal.toString());
			G.v().out.println("OUT :  " + stateOutByLocal.toString());
			
			for(Local key : stateOutByLocal.keySet()){
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
			}			
		}
	}
	
}
