package example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ReadFileByteArray {
	 
	  public static void main(String[] args) {
	   
	    //create file object
	    File file = null;//new File("C://FileIO//ReadString.txt");
	   String path = null;
	    try
	    {
	      //create FileInputStream object
	        FileInputStream fin = new FileInputStream(path);
	     FileReader fr = new FileReader(path);
	     FileWriter fw = new FileWriter(path);
	      /*
	       * Create byte array large enough to hold the content of the file.
	       * Use File.length to determine size of the file in bytes.
	       */
	     
	      fin.close();
	       byte fileContent[] = new byte[(int)file.length()];
	     
	       /*
	        * To read content of the file in byte array, use
	        * int read(byte[] byteArray) method of java FileInputStream class.
	        *
	        */
	       fin.read(fileContent);
	     
	       //create string from byte array
	       String strFileContent = new String(fileContent);
	     
	       System.out.println("File content : ");
	       System.out.println(strFileContent);
	     
	    }
	    catch(FileNotFoundException e)
	    {
	      System.out.println("File not found" + e);
	    }
	    catch(IOException ioe)
	    {
	      System.out.println("Exception while reading the file " + ioe);
	    }
	  }
	}
	 