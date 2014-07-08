package analysis;

import java.util.Map;

import resource.NewConfiguration;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class NewAnalysis extends BodyTransformer{
	InterProceduralCFG cfg;

	public NewAnalysis(){
		setupNewConfigurations();
	}
	
	public static void main(String[] args) {
		String classPath = "example.JDBCExample";		
		String mainClass = "analysis.NewAnalysis";
		
		String[] sootArgs = {
				"-cp", classPath, "-pp", 
				"-w", "-app", 
				"-p", "cg", "enabled:false",
				//"-keep-line-number",
				//"-keep-bytecode-offset",
				//"-p", "jb.tr", "use-older-type-assigner:true",
				//"-p", "jb", "use-original-names:true",
				//"-dynamic-class", "sun.net.spi.DefaultProxySelector",
				//"-p", "cg", "implicit-entry:false",
				//"-p", "cg.spark", "enabled",
				//"-p", "cg.spark", "simulate-natives",
				//"-p", "cg", "safe-forname",
				//"-p", "cg", "safe-newinstance",
				//"-main-class", mainClass,
				"-f", "none", mainClass 
		};
		
		String[] myArgs = {
				"-cp", classPath, "-pp", 
				"-p", "cg", "implicit-entry:false",
		};
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new NewAnalysis()));
		
		soot.Main.main(args);		
	}


	@Override
	protected void internalTransform(Body body, String phase, Map option) {
		if (cfg == null){
			cfg = new InterProceduralCFG();
		}
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		(new NewConstantPropagation(unitGraph, null, body.getLocals(), cfg)).startAnalysis();
	}
	
	private void setupNewConfigurations(){
		setupJDBCConfigurations();
	}
	
	private void setupJDBCConfigurations(){
		NewConfiguration.addNewMethod("DriverManager", "getConnection");
		NewConfiguration.addNewMethod("Connection", "createStatement");
		NewConfiguration.addNewMethod("Statement", "executeQuery");
		NewConfiguration.addNewMethod("ResultSet", "close");
		NewConfiguration.addNewMethod("Statement", "close");
		NewConfiguration.addNewMethod("Connection", "close");
		
		// Set up Actions
		NewConfiguration.addNewAction(NewConfiguration.getMethod("DriverManager", "getConnection"));
		NewConfiguration.addNewAction(NewConfiguration.getMethod("Connection", "createStatement"));
		NewConfiguration.addNewAction(NewConfiguration.getMethod("Statement", "executeQuery"));
		NewConfiguration.addNewAction(NewConfiguration.getMethod("ResultSet", "close"));
		NewConfiguration.addNewAction(NewConfiguration.getMethod("Statement", "close"));
		NewConfiguration.addNewAction(NewConfiguration.getMethod("Connection", "close"));
		
		// Set up States
		/**Connected**/
		NewConfiguration.addNewState("Connected", 1);
		NewConfiguration.addActionToState("Connected", NewConfiguration.getAction(NewConfiguration.getMethod("DriverManager", "getConnection")));
		
		/**NotConnected**/
		NewConfiguration.addNewState("NotConnected", 0);
		NewConfiguration.addActionToState("NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("ResultSet", "close")));
		NewConfiguration.addActionToState("NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Statement", "close")));
		NewConfiguration.addActionToState("NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "close")));
		
		/**Statement**/
		NewConfiguration.addNewState("Statement", 2);
		NewConfiguration.addActionToState("Statement", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "createStatement")));
		
		/**Result**/
		NewConfiguration.addNewState("Result", 3);
		NewConfiguration.addActionToState("Result", NewConfiguration.getAction(NewConfiguration.getMethod("Statement", "executeQuery")));
		
		// Set up Transitions
		NewConfiguration.addNewTransition("Bottom", "Connected", NewConfiguration.getAction(NewConfiguration.getMethod("DriverManager", "getConnection")));
		NewConfiguration.addNewTransition("NotConnected", "Connected", NewConfiguration.getAction(NewConfiguration.getMethod("DriverManager", "getConnection")));
		NewConfiguration.addNewTransition("NotConnected", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "close")));
		NewConfiguration.addNewTransition("NotConnected", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("ResultSet", "close")));
		NewConfiguration.addNewTransition("NotConnected", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Statement", "close")));
		NewConfiguration.addNewTransition("Connected", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "close")));
		NewConfiguration.addNewTransition("Connected", "Statement", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "createStatement")));
		NewConfiguration.addNewTransition("Statement", "Result", NewConfiguration.getAction(NewConfiguration.getMethod("Statement", "executeQuery")));
		NewConfiguration.addNewTransition("Statement", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("Statement", "close")));
		NewConfiguration.addNewTransition("Statement", "Statement", NewConfiguration.getAction(NewConfiguration.getMethod("Connection", "createStatement")));
		NewConfiguration.addNewTransition("Result", "NotConnected", NewConfiguration.getAction(NewConfiguration.getMethod("ResultSet", "close")));
		
		/**Base State**/
		//NewConfiguration.setBaseState("NotConnected");
	}

}
