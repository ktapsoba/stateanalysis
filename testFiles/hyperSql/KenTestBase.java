package testFiles.hyperSql;

import hypersql.server.Server;
import hypersql.server.WebServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;




public class KenTestBase {

    String  dbPath = "mem:test;sql.enforce_strict_size=true";
    String  serverProps;
    String  url;
    String  user     = "sa";
    String  password = "";
    Server  server;
    boolean isNetwork = true;
    boolean isHTTP    = false;    // Set false to test HSQL protocol, true to test HTTP, in which case you can use isUseTestServlet to target either HSQL's webserver, or the Servlet server-mode
    boolean isServlet = false;
    String name;

    public KenTestBase(String name) {
        this.name = name;
    }

    public KenTestBase(String name, String url, boolean isNetwork,boolean isHTTP) {

        this(name);

        this.isNetwork = isNetwork;
        this.url       = url;
        this.isHTTP    = isHTTP;
    }
    
    public String getName(){
    	return name;
    }

    protected void setUp() throws Exception {

        if (isNetwork) {

            //  change the url to reflect your preferred db location and name
            if (url == null) {
                if (isServlet) {
                    url = "jdbc:hsqldb:http://localhost:8080/HSQLwebApp/test";
                } else if (isHTTP) {
                    url = "jdbc:hsqldb:http://localhost:8085/test";
                } else {
                    url = "jdbc:hsqldb:hsql://localhost/test";
                }
            }

            if (!isServlet) {
                server = isHTTP ? new WebServer()
                                : new Server();

                if (isHTTP) {
                    server.setPort(8085);
                }

                server.setDatabaseName(0, "test");
                server.setDatabasePath(0, dbPath);
                server.setLogWriter(null);
                server.setErrWriter(null);
                server.start();
            }
        } else {
            if (url == null) {
                url = "jdbc:hsqldb:" + dbPath;
                //url = "jdbc:mysql://localhost:3306/test";
            }
        }

        try {
        	//TODO
        	//do not load driver
            Class.forName("org.hsqldb.jdbc.JDBCDriver");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(this + ".setUp() error: " + e.getMessage());
        }

        System.out.println("Set up is done");
    }

    protected void tearDown() {

        if (isNetwork && !isServlet) {
            server.stop();

            server = null;
        }
    }

    Connection newConnection(Connection connection) throws SQLException {
    	System.out.println("url is " + url);
    	
    	//TODO 	no connection
    	// return null;//connection;
        connection = DriverManager.getConnection(url, user, password);
        return connection;
    }

    /*public static void runWithResult(Class testCaseClass, String testName) {

        try {
            Constructor ctor = testCaseClass.getConstructor(new Class[]{
                String.class });
            TestBase theTest = (TestBase) ctor.newInstance(new Object[]{
                testName });

            theTest.runWithResult();
        } catch (Exception ex) {
            System.err.println("couldn't execute test:");
            ex.printStackTrace(System.err);
        }
    }

    public void runWithResult() {

        TestResult result   = run();
        String     testName = this.getClass().getName();

        if (testName.startsWith("org.hsqldb.test.")) {
            testName = testName.substring(16);
        }

        testName += "." + getName();

        int failureCount = result.failureCount();

        System.out.println(testName + " failure count: " + failureCount);

        java.util.Enumeration failures = result.failures();

        while (failures.hasMoreElements()) {
            System.err.println(failures.nextElement());
        }
    }*/
}
