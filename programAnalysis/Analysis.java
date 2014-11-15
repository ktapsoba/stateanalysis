package programAnalysis;

import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

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
	Handler handler;
	Logger logger = Logger.getLogger(Analysis.class.getName());

	public Analysis() {
		setupConfigurations();
		try{
			handler = new FileHandler("analysis.log");
			logger.addHandler(handler);
		} catch(Exception e) {
			G.v().out.println("error getting hanlder");
		}
	}

	public static void main(String[] args) {
		
		String[] testingClasses = {
			//"test.JDBCExample",
			//"testFiles.hyperSql.KenTestdb",					//Good
			//"testFiles.hyperSql.KenTestBatchExecution", 	//has problem with soot
			//"testFiles.hyperSql.KenTestScript",				//stack overflow
			//"testFiles.hyperSql.KenTestUtil",				//Error with getting params
			//"testFiles.hyperSql.TestCacheSize",			//stack overlfow
			//"testFiles.hyperSql.TransferSQLText",			//no main
			//"hypersql.test.TestDima"
			//"hypersql.test.AllSimpleTests",
			"hypersql.test.AllTests"
			//"test.JavaIOExample"
		};
		(new Analysis()).startAnalysis(testingClasses);
	}
	
	public void startAnalysis(String[] testingClasses){
		for(String testingClass : testingClasses){
			//G.v().out.println( "******************************* Start Testing " + testingClass + "*******************************\n");
			logger.info( "******************************* Start Testing " + testingClass + "*******************************\n");
			PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new Analysis()));
			String[] sootArgs = buildSootArgs(testingClass);
			soot.Main.main(sootArgs);
			logger.info( "******************************* Done Testing " + testingClass + "*****************************\n");
		}
		logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ DONE ALL TESTING ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		handler.close();
	}
	
	public String[] buildSootArgs(String mainClass) {
		String sootOutputDir = System.getProperty("user.dir") + "/sootOutput";
		String libraries = getLibraries();
	
		String[] sootArgs = { 
			"--w",
			"--time",
			"--d",	sootOutputDir,
			"--cp",	libraries,
			"--omit-excepting-unit-edges",
			"--print-tags",
			"-annot-nullpointer",
			"--interactive-mode",
			"--xml-attributes",
			"--allow-phantom-refs",
			"--main-class",	mainClass,
			"--src-prec",	"java",
			mainClass
		};
		
		return sootArgs;
	}

	@Override
	protected void internalTransform(Body body, String phase, Map option) {
		logger.info("------------------------Transforming.." + body.getMethod().getName() + "--------------------");
		if (cfg == null) {
			cfg = new ControlFlowGraph(handler);
		}
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		DataFlowAnalysis dataFlowAnalysis = new DataFlowAnalysis(unitGraph, null, body.getLocals(), cfg, new Environment(), new DependencyMap(), logger);
		dataFlowAnalysis.startAnalysis();
	}

	private void setupConfigurations() {
		//setupJDBCConfigurations();
		setupJavaIOConfigurations();
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
        
        //Set up base states
        Configuration.addBaseState("NotConnected", "NotConnected");
        Configuration.addBaseState("Connected", "NotConnected");
        Configuration.addBaseState("Statement", "StatementClosed");
        Configuration.addBaseState("StatementClosed", "StatementClosed");
        Configuration.addBaseState("ResultSet", "ResultSetClosed");
        Configuration.addBaseState("ResultSetClosed", "ResultSetClosed");
		
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
        
        //Set up base states
        Configuration.addBaseState("Close", "Close");
        Configuration.addBaseState("Open", "Close");
        Configuration.addBaseState("BufferOpen", "BufferClosed");
        Configuration.addBaseState("BufferClosed", "BufferClosed");
        Configuration.addBaseState("ReadWrite", "Close");
        
        // Set up Transitions
        Configuration.addNewTransition("toConnected1", "Close", "Open","openFile");
        Configuration.addNewTransition("toConnected2", "Open", "Open", "openFile");
        
        Configuration.addNewTransition("toNotConnected1", "Open", "Close", "closeFile");
        Configuration.addNewTransition("toNotConnected2", "Close", "Close", "closeFile");
        Configuration.addNewTransition("toNotConnected3", "ReadWrite", "Close", "closeFile");
        
        Configuration.addNewTransition("toBufferOpen1", "Open", "BufferOpen", "getBuffer");
        Configuration.addNewTransition("toBufferOpen2", "BufferOpen", "BufferOpen", "getBuffer");

        Configuration.addNewTransition("toBufferClosed1", "BufferOpen", "BufferClosed", "closeBuffer");
        Configuration.addNewTransition("toBufferClosed2", "BufferClosed", "BufferClosed", "closeBuffer");

        Configuration.addNewTransition("toReadWrite1", "Open", "ReadWrite", "readWriteFile");
        Configuration.addNewTransition("toReadWrite2", "ReadWrite", "ReadWrite", "readWriteFile");
	}

	private String getLibraries() {
		String separator = isMac()? ":":";";
		
		String libraries = System.getProperty("java.class.path") + separator;
		libraries += System.getProperty("java.home") + "/lib/rt.jar" + separator;
		libraries += System.getProperty("user.dir")+"/src";
		
		return libraries;
		/*String libraries = "/C:/Program%20Files/Java/jre7/lib/jsse.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/charsets.jar;" + 
						   "/C:/Users/Nek/Desktop/COMPLETE%20ANALYSIS/external%20lib/javax.jar;"+
						   "/C:/Program%20Files/Java/jre7/lib/ext/zipfs.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/sunjce_provider.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/jfr.jar;" +
						   "/C:/Users/Nek/Desktop/COMPLETE%20ANALYSIS/external%20lib/ant-1.7.1.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/sunmscapi.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/resources.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/dnsns.jar;" +
						   "/C:/Users/Nek/Desktop/COMPLETE%20ANALYSIS/external%20lib/soot-2.5.0.jar;" +
						   "/C:/Users/Nek/Desktop/COMPLETE%20ANALYSIS/eclipse/plugins/org.junit_3.8.2.v3_8_2_v20100427-1100/junit.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/access-bridge-64.jar;/C:/StateAnalysis/src;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/sunec.jar;" +
						   "/C:/Users/Nek/Desktop/COMPLETE%20ANALYSIS/workspace/StateAnalysis/bin/;" +
						   "/C:/Program%20Files/Java/jre7/lib/jce.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/localedata.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/rt.jar;" +
						   "/C:/Program%20Files/Java/jre7/lib/ext/jaccess.jar;;" +
						   "C:/Users/Nek/Desktop/COMPLETE ANALYSIS/workspace/StateAnalysis/src;" +
						   "C:/Program Files/Java/jre7/lib/resources.jar;" +
						   "C:/Program Files/Java/jre7/lib/rt.jar;" +
						   "C:/Program Files/Java/jre7/lib/jsse.jar;" +
						   "C:/Program Files/Java/jre7/lib/jce.jar;" +
						   "C:/Program Files/Java/jre7/lib/charsets.jar;" +
						   "C:/Program Files/Java/jre7/lib/jfr.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/access-bridge-64.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/dnsns.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/jaccess.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/localedata.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/sunec.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/sunjce_provider.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/sunmscapi.jar;" +
						   "C:/Program Files/Java/jre7/lib/ext/zipfs.jar;" +
						   "C:/Users/Nek/Desktop/COMPLETE ANALYSIS/external lib/soot-2.5.0.jar;" +
						   "C:/Users/Nek/Desktop/COMPLETE ANALYSIS/eclipse/plugins/org.junit_3.8.2.v3_8_2_v20100427-1100/junit.jar;" +
						   "C:/Users/Nek/Desktop/COMPLETE ANALYSIS/external lib/javax.jar;" +
						   "C:/Users/Nek/Desktop/COMPLETE ANALYSIS/external lib/ant-1.7.1.jar";*/
		
	}
	
	private boolean isMac(){
		String OSName = System.getProperty("os.name");
		if(OSName.toLowerCase().indexOf("mac") > -1){
			return true;
		}
		return false;
	}

}
