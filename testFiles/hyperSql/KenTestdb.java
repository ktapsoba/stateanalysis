package testFiles.hyperSql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Title:        Testdb
 * Description:  simple hello world db example of a
 *               standalone persistent db application
 *
 *               every time it runs it adds four more rows to sample_table
 *               it does a query and prints the results to standard out
 *
 * Author: Karl Meissner karl@meissnersd.com
 */
public class KenTestdb {                                                //our connnection to the db - presist for life of program

    // we dont want this garbage collected until we are done
    public KenTestdb() throws Exception {    // note more general exception

        // Load the HSQL Database Engine JDBC driver
        // hsqldb.jar should be in the class path or made part of the current jar
        Class.forName("org.hsqldb.jdbc.JDBCDriver");

        // connect to the database.   This will load the db files and start the
        // database if it is not alread running.
        // db_file_name_prefix is used to open or create files that hold the state
        // of the db.
        // It can contain directory names relative to the
        // current working directory
    }
    
    public Connection getConnection(String db_file_name_prefix, Connection conn) throws Exception{
        conn = DriverManager.getConnection("jdbc:hsqldb:"
                                           + db_file_name_prefix,    // filenames
                                           "SA",                     // username
                                           "");                      // password
        return conn;
    }

    public void shutdown(Connection conn) throws SQLException {

        Statement st = conn.createStatement();

        // db writes out to files and performs clean shuts down
        // otherwise there will be an unclean shutdown
        // when program ends
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }

//use for SQL command SELECT
    public synchronized void query(String expression, Connection conn) throws SQLException {

        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        rs = st.executeQuery(expression);    // run the query

        // do something with the result set.
        dump(rs);
        st.close();    // NOTE!! if you close a statement the associated ResultSet is

        // closed too
        // so you should copy the contents to some other object.
        // the result set is invalidated also  if you recycle an Statement
        // and try to execute some other query before the result set has been
        // completely examined.
    }

//use for SQL commands CREATE, DROP, INSERT and UPDATE
    public synchronized void update(String expression, Connection conn) throws SQLException {

        Statement st = null;

        //TODO
        //close connection
        //conn.close();
        //no statemnet
        //st = null;
        
        st = conn.createStatement();    // statements

        //TODO
        //close connection
        //connection.close();
        //close statement
        //st.close();
        
        int i = st.executeUpdate(expression);    // run the query
        
        ResultSet rs = st.executeQuery(expression);
        
        if (!rs.rowUpdated() && !rs.rowInserted() && !rs.rowDeleted()) {
            System.out.println("db error : " + expression);
        }

        st.close();
    }    // void update()

    public static void dump(ResultSet rs) throws SQLException {

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column is indexed

                // with 1 not 0
                System.out.print(o.toString() + " ");
            }

            System.out.println(" ");
        }
    }                                       //void dump( ResultSet rs )

    public static void main(String[] args) {

        KenTestdb db = null;
        Connection conn = null;

        try {
            db = new KenTestdb();
        } catch (Exception ex1) {
            ex1.printStackTrace();    // could not start db

            return;                   // bye bye
        }

        try {
        	//TODO
        	//no connection
        	//conn = null;
        	
        	conn = db.getConnection("db_file", conn);
            //make an empty table
            //
            // by declaring the id column IDENTITY, the db will automatically
            // generate unique values for new rows- useful for row keys
            //db.update(
            //    "CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)", conn);
        } catch (SQLException ex2) {

            //ignore
            //ex2.printStackTrace();  // second time we run program
            //  should throw execption since table
            // already there
            //
            // this will have no effect on the db
        } catch(Exception ex){
        	System.out.println(ex.getMessage());
        	ex.printStackTrace();
        }

        /*try {
        	//TODO
        	//close connection
        	//conn.close();
        	
            // add some rows - will create duplicates if run more then once
            // the id column is automatically generated
            db.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)", conn);
            db.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Toyota', 200)", conn);
            db.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('Honda', 300)", conn);
            
            //TODO
            //close connection
            //conn.close();
            
            db.update(
                "INSERT INTO sample_table(str_col,num_col) VALUES('GM', 400)", conn);

            // do a query
            db.query("SELECT * FROM sample_table WHERE num_col < 250", conn);

            // at end of program
            db.shutdown(conn);
        } catch (SQLException ex3) {
            ex3.printStackTrace();
        }*/
    }    // main()
}    // class Testdb

