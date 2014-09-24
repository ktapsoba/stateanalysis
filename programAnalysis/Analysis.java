package programAnalysis;

import java.util.Map;

import resource.Method;
import soot.Body;
import soot.BodyTransformer;
import soot.G;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import verification.Configuration;

public class Analysis extends BodyTransformer {
	ControlFlowGraph cfg;

	public Analysis() {
		setupConfigurations();
	}

	public static void main(String[] args) {
		String classPath = "example.JDBCExample";
		String mainClass = "programAnalysis.Analysis";

		String[] sootArgs = { "-cp", classPath, "-pp", "-w", "-app", "-p",
				"cg", "enabled:false",
				// "-keep-line-number",
				// "-keep-bytecode-offset",
				// "-p", "jb.tr", "use-older-type-assigner:true",
				// "-p", "jb", "use-original-names:true",
				// "-dynamic-class", "sun.net.spi.DefaultProxySelector",
				// "-p", "cg", "implicit-entry:false",
				// "-p", "cg.spark", "enabled",
				// "-p", "cg.spark", "simulate-natives",
				// "-p", "cg", "safe-forname",
				// "-p", "cg", "safe-newinstance",
				// "-main-class", mainClass,
				"-f", "none", mainClass };

		String[] myArgs = { "-cp", classPath, "-pp", "-p", "cg",
				"implicit-entry:false", };

		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myTransform", new Analysis()));

		soot.Main.main(args);
	}

	@Override
	protected void internalTransform(Body body, String phase, Map option) {
		if (cfg == null) {
			cfg = new ControlFlowGraph();
		}
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		DataFlowAnalysis dataFlowAnalysis = new DataFlowAnalysis(unitGraph, null, body.getLocals(), cfg, new Environment());
		dataFlowAnalysis.startAnalysis();
	}

	private void setupConfigurations() {
		setupJDBCConfigurations();
		//setupJavaIOConfigurations();
	}

	private void setupJDBCConfigurations() {
		// Set up Methods
		Configuration.addNewMethod("DriverManager", "getConnection");
		Configuration.addNewMethod("Connection", "createStatement");
		Configuration.addNewMethod("Statement", "executeQuery");
		Configuration.addNewMethod("ResultSet", "close");
		Configuration.addNewMethod("Statement", "close");
		Configuration.addNewMethod("Connection", "close");
		
		// Set up Actions
		Configuration.addNewAction("getConnection");
		Configuration.addMethodToAction("getConnection", Configuration.getMethod("DriverManager", "getConnection"));

		Configuration.addNewAction("createStatement");
		Configuration.addMethodToAction("createStatement", Configuration.getMethod("Connection", "createStatement"));

		Configuration.addNewAction("executeQuery");
		Configuration.addMethodToAction("executeQuery", Configuration.getMethod("Statement", "executeQuery"));

		Configuration.addNewAction("closeResultSet");
		Configuration.addMethodToAction("closeResultSet", Configuration.getMethod("ResultSet", "close"));

		Configuration.addNewAction("closeStatement");
		Configuration.addMethodToAction("closeStatement", Configuration.getMethod("Statement", "close"));

		Configuration.addNewAction("closeConnection");
		Configuration.addMethodToAction("closeConnection", Configuration.getMethod("Connection", "close"));
		
		// Set up States
		/**NotConnected**/
        Configuration.addNewState("NotConnected");
        Configuration.addActionToState("NotConnected", Configuration.getAction("closeConnection"));
        
		/**Connected**/
		Configuration.addNewState("Connected");
		Configuration.addActionToState("Connected", Configuration.getAction("getConnection"));
		
		/**Statement**/
		Configuration.addNewState("Statement");
		Configuration.addActionToState("Statement", Configuration.getAction("createStatement"));
		
		 /**StatementClosed**/
        Configuration.addNewState("StatementClosed");
        Configuration.addActionToState("StatementClosed", Configuration.getAction("closeStatement"));
		
		/**Result**/
		Configuration.addNewState("ResultSet");
		Configuration.addActionToState("ResultSet", Configuration.getAction("executeQuery"));
		
		/**ResultSet**/
        Configuration.addNewState("ResultSetClosed");
        Configuration.addActionToState("ResultSetClosed", Configuration.getAction("closeResultSet"));
		
		// Set up Transitions
		Configuration.addNewTransition("toConnected1", "NotConnected", "Connected", "getConnection");
		Configuration.addNewTransition("toConnected2", "Connected", "Connected", "getConnection");
		
		Configuration.addNewTransition("toNotConnected1", "Connected", "NotConnected", "closeConnection");
		Configuration.addNewTransition("toNotConnected2", "NotConnected", "NotConnected", "closeConnection");
		
        Configuration.addNewTransition("toStatement1", "Connected", "Statement", "createStatement");
        Configuration.addNewTransition("toStatement2", "Statement", "Statement", "createStatement");

        Configuration.addNewTransition("toStatementClosed1", "Statement", "StatementClosed", "closeStatement");
        Configuration.addNewTransition("toStatementClosed2", "StatementClosed", "StatementClosed", "closeStatement");

        Configuration.addNewTransition("toResultSet1", "Statement", "ResultSet", "executeQuery");
        Configuration.addNewTransition("toResultSet2", "ResultSet", "ResultSet", "executeQuery");

        Configuration.addNewTransition("ResultSetClosed1", "ResultSet", "ResultSetClosed", "closeResultSet");
        Configuration.addNewTransition("ResultSetClosed2", "ResultSetClosed", "ResultSetClosed", "closeResultSet");
		
	}
	
	private void setupJavaIOConfigurations(){
	    // Set up Methods
        Configuration.addNewMethod("FileReader", "FileReader");
        Configuration.addNewMethod("FileReader", "read");
        Configuration.addNewMethod("FileReader", "close");
        Configuration.addNewMethod("BuffferedReader", "BuffferedReader");
        Configuration.addNewMethod("BuffferedReader", "read");
        Configuration.addNewMethod("BuffferedReader", "close");
        Configuration.addNewMethod("FileInputStream", "FileInputStream");
        Configuration.addNewMethod("FileInputStream", "read");
        Configuration.addNewMethod("FileInputStream", "close");

        Configuration.addNewMethod("FileWriter", "FileWriter");
        Configuration.addNewMethod("FileWriter", "write");
        Configuration.addNewMethod("FileWriter", "close");
        Configuration.addNewMethod("BufferedWriter", "BufferedWriter");
        Configuration.addNewMethod("BufferedWriter", "write");
        Configuration.addNewMethod("BufferedWriter", "close");
        Configuration.addNewMethod("FileOutputStream", "FileOutputStream");
        Configuration.addNewMethod("FileOutputStream", "write");
        Configuration.addNewMethod("FileOutputStream", "close");
        
        // Set up Actions
        Configuration.addNewAction("openFile");
        Configuration.addMethodToAction("openFile", Configuration.getMethod("FileReader", "FileReader"));
        Configuration.addMethodToAction("openFile", Configuration.getMethod("FileInputStream", "FileInputStream"));
        Configuration.addMethodToAction("openFile", Configuration.getMethod("FileWriter", "FileWriter"));
        Configuration.addMethodToAction("openFile", Configuration.getMethod("FileOutputStream", "FileOutputStream"));

        Configuration.addNewAction("closeFile");
        Configuration.addMethodToAction("closeFile", Configuration.getMethod("FileReader", "close"));
        Configuration.addMethodToAction("closeFile", Configuration.getMethod("FileInputStream", "close"));
        Configuration.addMethodToAction("closeFile", Configuration.getMethod("FileWriter", "close"));
        Configuration.addMethodToAction("closeFile", Configuration.getMethod("FileOutputStream", "close"));

        Configuration.addNewAction("getBuffer");
        Configuration.addMethodToAction("getBuffer", Configuration.getMethod("BuffferedReader", "BuffferedReader"));
        Configuration.addMethodToAction("getBuffer", Configuration.getMethod("BuffferedWriter", "BuffferedWriter"));

        Configuration.addNewAction("readWriteFile");
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("FileReader", "read"));
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("BuffferedReader", "read"));
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("FileInputStream", "read"));
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("FileWriter", "write"));
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("BuffferedWriter", "write"));
        Configuration.addMethodToAction("readWriteFile", Configuration.getMethod("FileOutputStream", "write"));

        Configuration.addNewAction("closeBuffer");
        Configuration.addMethodToAction("closeBuffer", Configuration.getMethod("BuffferedReader", "close"));
        Configuration.addMethodToAction("closeBuffer", Configuration.getMethod("BuffferedWriter", "close"));
        
        // Set up States
        /**NotConnected**/
        Configuration.addNewState("Close");
        Configuration.addActionToState("Close", Configuration.getAction("closeFile"));
        
        /**Connected**/
        Configuration.addNewState("Open");
        Configuration.addActionToState("Open", Configuration.getAction("openFile"));
        
        /**BufferOpen**/
        Configuration.addNewState("BufferOpen");
        Configuration.addActionToState("BufferOpen", Configuration.getAction("getBuffer"));
        
         /**BufferClosed**/
        Configuration.addNewState("BufferClosed");
        Configuration.addActionToState("BufferClosed", Configuration.getAction("closeBuffer"));
        
        /**ReadWrite**/
        Configuration.addNewState("ReadWrite");
        Configuration.addActionToState("ReadWrite", Configuration.getAction("readWriteFile"));
        
        // Set up Transitions
        Configuration.addNewTransition("toConnected1", "Close", "Open","openFile");
        Configuration.addNewTransition("toConnected2", "Open", "Open", "openFile");
        
        Configuration.addNewTransition("toNotConnected1", "Open", "Close", "closeFile");
        Configuration.addNewTransition("toNotConnected2", "Close", "Close", "closeFile");
        
        Configuration.addNewTransition("toBufferOpen1", "Open", "BufferOpen", "getBuffer");
        Configuration.addNewTransition("toBufferOpen2", "BufferOpen", "BufferOpen", "getBuffer");

        Configuration.addNewTransition("toBufferClosed1", "BufferOpen", "BufferClosed", "closeBuffer");
        Configuration.addNewTransition("toBufferClosed2", "BufferClosed", "BufferClosed", "closeBuffer");

        Configuration.addNewTransition("toReadWrite1", "Open", "ReadWrite", "readWriteFile");
        Configuration.addNewTransition("toReadWrite2", "ReadWrite", "ReadWrite", "readWriteFile");
	}

}
