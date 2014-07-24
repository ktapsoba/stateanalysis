package example;
//STEP 1. Import required packages
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCExample {
	
	public JDBCExample(){}
 
	public static void main(String[] args) {
		
		// JDBC driver name and database URL
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		String DB_URL = "jdbc:mysql://localhost:3306/test";
		String url = "jdbc:mysql://localhost:3306/db_project";
		FTPExample ftpExample = new FTPExample();

		//  Database credentials
		String USER = "testUser";
		String PASS = "testUser";
		
		Statement one = null;
		Connection conn = null;
		Statement stmt = one;
		Connection con2 = null;
		
		try{
			//STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
			//conn.close();
			//conn.close();
			//STEP 4: Execute a query
			System.out.println("Creating statement...");
			con2 = DriverManager.getConnection(DB_URL);
			String sql = "SELECT * from country";
			stmt = conn.createStatement();
			//conn = DriverManager.getConnection(url, "root", "root");
			//conn.close();
			
			ftpExample.printme(con2);
			System.out.println("getting result");

			
			ResultSet rs = stmt.executeQuery(sql);
			//CallableStatement callStmt = conn.prepareCall(sql);
			//callStmt.setInt(1, 3);
			//ResultSet rs = callStmt.executeQuery();
			System.out.println("printing results");
			//STEP 5: Extract data from result set
			while(rs.next()){
				
				//Retrieve by column name
				String code = rs.getString("Code");
				String name = rs.getString("Name");
				int population = rs.getInt("Population");

		       printResults(code, name, population, con2);
		       printResults(code, name, population, conn);
			}
			
		    //STEP 6: Clean-up environment
			System.out.println("close conn");
			System.out.println("close rs");
		    rs.close();
		    System.out.println("close stmt");
		    stmt.close();
		    conn.close();
		    FTPExample ftp = new FTPExample();
		    
		}catch(SQLException se){
		    //Handle errors for JDBC
		    se.printStackTrace();
		    
		}catch(Exception e){
		    //Handle errors for Class.forName
		    e.printStackTrace();
		    
		}finally{
			
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
				
			}catch(SQLException se2){
				
			}// nothing we can do
			
			try{
				if(conn!=null)
					System.out.println("Close Connection again");
					conn.close();
				
			}catch(SQLException se){
				se.printStackTrace();
				
			}//end finally try
			
		}//end finally
		
	}//end main
	
	public static void printResults(String code, String name, int population, Connection con2) throws SQLException{
		//Display values
		System.out.print("Code: " + code);
		System.out.print(", Name: " + name);
		System.out.print(", Population: " + population);
		System.out.println();
		//Statement stmt = con2.createStatement();
		//con2 = DriverManager.getConnection("");;
	}
	
	public static Connection closeit(Connection con){
		return con;
	}
	
	public void oneMethod(int v){
		System.out.println(v);
	}

}
