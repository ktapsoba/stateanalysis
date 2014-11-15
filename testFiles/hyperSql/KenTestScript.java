package testFiles.hyperSql;

import java.sql.Connection;


public class KenTestScript {

//    String path = "TestAny.txt";
    String path = "TestSelf.txt";
//    String path = "TestSelfArrays.txt";
//    String path = "TestSelf3PartNames.txt";
//    String path = "TestSelfArithmetic.txt";
//    String path = "TestSelfAlterColumn.txt";
//    String path = "TestSelfCaseWhen.txt";
//    String path = "TestSelfCheckConstraints.txt";
//    String path = "TestSelfColGrant.txt";
//    String path = "TestSelfCreate.txt";
//    String path = "TestSelfConstraints.txt";
//    String path = "TestSelfFunction.txt";
//    String path = "TestSelfGrantees.txt";
//    String path = "TestSelfGroupBy.txt";
//    String path = "TestSelfInsertDeleteQueries.txt";
//    String path = "TestSelfInterval.txt";
//    String path = "TestSelfInternalFunctions.txt";
//    String path = "TestSelfFieldLimits.txt";
//    String path = "TestSelfFKModes.txt";
//    String path = "TestSelfInPredicateReferencing.txt";
//    String path = "TestSelfInsteadOfTriggers.txt";
//    String path = "TestSelfIssues.txt";
//    String path = "TestSelfJoins.txt";
//    String path = "TestSelfLeftJoin.txt";
//    String path = "TestSelfNameResolution.txt";
//    String path = "TestSelfImmediateShutdown.txt";
//    String path = "TestSelfInsertDeleteQueries.txt";
//    String path = "TestSelfInPredicateReferencing.txt";
//    String path = "TestSelfMultiGrants.txt";
//    String path = "TestSelfNot.txt";
//    String path = "TestSelfOrderLimits.txt";
//    String path = "TestSelfRoleNesting.txt";
//    String path = "TestSelfQueries.txt";
//    String path = "TestSelfSchemaPersistB1.txt";
//    String path = "TestSelfSeqRightsA.txt";
//    String path = "TestSelfStoredProcedure.txt";
//    String path = "TestSelfStoredProcedureTypes.txt";
//    String path = "TestSelfSubselects.txt";
//    String path = "TestSelfSysTables.txt";
//    String path = "TestSelfTempTable1.txt";
//    String path = "TestSelfTransaction.txt";
//    String path = "TestSelfTriggers.txt";
//    String path = "TestSelfTriggers2.txt";
//    String path = "TestSelfUnions.txt";
//    String path = "TestSelfUserFunction.txt";
//    String path = "TestSelfViews.txt";
//    String path = "TestSelfViewGrants.txt";
//    String path = "TestSelfSeqRightsA.txt";
//    String path = "TestSelfSysTables.txt";
//    String path = "TestTemp.txt";
    
    KenTestBase testBase;
    public KenTestScript(String name) {
        testBase = new KenTestBase(name, null, false, false);
    }

    public void test() throws java.lang.Exception {

        Connection conn = null;
        
        //KenTestUtil.deleteDatabase("test");
        testBase.setUp();
        
        //TODO
        // no connection
        //connection = null;
        
        conn = testBase.newConnection(conn);
        String fullPath = "testrun/hsqldb/" + path;
        
        //TODO
        //close connection
        //conn.close();
        
        KenTestUtil.testScript(conn, fullPath);
        conn.createStatement().execute("SHUTDOWN");
   }

    public static void main(String[] Args) throws Exception {

        KenTestScript ts = new KenTestScript("test");

        ts.test();
    }
}
