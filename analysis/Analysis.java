package analysis;

import java.util.Map;

import resource.Configuration;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Analysis {
	
	public static void main(String[] args){
		(new Analysis()).process(args);
	}

	private void process(String[] args){
		final Configuration config = setupConfigurations();
		PackManager.v().getPack("jtp")
		.add(new Transform("jtp.myTransform", new BodyTransformer() {

			protected void internalTransform(Body body, String phase, Map options) {
				UnitGraph unitGraph = new ExceptionalUnitGraph(body);
				new ConstantPropagation(unitGraph, config);
				//new ContextualAnalysis(unitGraph);
			}

		}));
		soot.Main.main(args);
	}
	
	private Configuration setupConfigurations(){
		Configuration config = new Configuration();
		
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
		
		return config;
	}
}
