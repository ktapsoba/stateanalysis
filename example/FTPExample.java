package example;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

public class FTPExample {
	
	public static void main(String[] args){
		String server = "localhost";
		String username = "username";
		String password = "password";
		String dir = "directory";
		int n=0;
		try{
			FTPClient ftp = new FTPClient();
			ftp.connect(server);
			ftp.login(username, password);
			FTPListParseEngine engine = ftp.initiateListParsing(dir);
			while (engine.hasNext()) {
				FTPFile[] files = engine.getNext(n);
				printFiles(files);
			}
			ftp.logout();
			ftp.disconnect();
		}catch (Exception e){
			
		}
	}
	
	public static void printFiles(FTPFile[] files){
		for(FTPFile file : files){
			System.out.println(file.getName());
		}
	}
	
	public void printme(Connection con) throws SQLException{
		con.close();
	}
}
