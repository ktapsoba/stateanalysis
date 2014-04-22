package analysis;

import java.util.Map;

import resource.Configuration;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.Transform;
import soot.jimple.Constant;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Analysis extends BodyTransformer {
	
	Configuration config;
	InterProceduralCFG cfg;
	public Analysis(){
		config = new Configuration();
		setupConfigurations();
	}
	
	public static void main(String[] args){
		(new Analysis()).process(args);
	}
	
	private void process(String[] args){
		
		setupConfigurations();
		String classPath = "example.JDBCExample";		
		String mainClass = "analysis.Analysis";
		
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
		
		PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new Analysis()));
		
		soot.Main.main(args);		
	}
	
	@Override
	protected void internalTransform(Body body, String phase, Map option) {
		if (cfg == null){
			cfg = new InterProceduralCFG();
		}
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		(new ConstantPropagation(unitGraph, null , config, cfg, body.getLocals())).StartAnalysis();
		
	}
	
	public static String formatConstants(Map<Local, Constant> value) {
		if (value == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<Local,Constant> entry : value.entrySet()) {
			Local local = entry.getKey();
			Constant constant = entry.getValue();
			if (constant != null) {
				sb.append("(").append(local).append("=").append(constant).append(") ");
			}
		}
		return sb.toString();
	}
	
	private void setupConfigurations(){
		setupJDBCConfig();
		
		//return config;
	}
	
	private void setupJDBCConfig(){
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
		config.AddNewState("Connected", 1);
		config.AddActionToState("Connected", config.getAction(config.getMethod("DriverManager", "getConnection")));
		
		/**NotConnected**/
		config.AddNewState("NotConnected", 0);
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("ResultSet", "close")));
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("Statement", "close")));
		config.AddActionToState("NotConnected", config.getAction(config.getMethod("Connection", "close")));
		
		/**Statement**/
		config.AddNewState("Statement", 2);
		config.AddActionToState("Statement", config.getAction(config.getMethod("Connection", "createStatement")));
		
		/**Result**/
		config.AddNewState("Result", 3);
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
	
	private Configuration setupFTPConfig(Configuration config){
		return config;
	}
	
	private Configuration setupHTTPConfig(Configuration config){
		return config;
	}
	
	private Configuration setupJavaIOConfig(Configuration config){
		return config;
	}
}
