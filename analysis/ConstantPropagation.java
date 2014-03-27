package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.G;
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

public class ConstantPropagation extends ForwardFlowAnalysis<Unit, State>{
	
	private Map<Unit, State> stateByUnitIn;
	private Map<Unit, State> stateByUnitOut;
	private UnitGraph unitGraph;
	private List<Unit> unitAnalysed = new ArrayList<Unit>();
	private Configuration config = new Configuration();
	
	public ConstantPropagation(DirectedGraph<Unit> graph){
		super(graph);
		this.graph = graph;
		this.unitGraph = (UnitGraph)this.graph;
		stateByUnitIn = new HashMap<Unit, State>();
		stateByUnitOut = new HashMap<Unit, State>();
		setConfigs();
		
		G.v().out.println("STATS");
		G.v().out.println(config.Stats());
		//doAnalysis();
		//printResults();
		//checkTransitions();
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
		
	}

	@Override
	protected void merge(State input1, State input2, State output) {
		copy(input1, output);
		
		
	}
	
	private void saveStateByUnit(Unit unit, State state, Map<Unit, State> unitByState){
		
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
		}
	}
	
}
