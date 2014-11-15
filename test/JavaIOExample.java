package test;

import java.io.FileReader;
import java.io.FileWriter;

public class JavaIOExample {

	public void Do(){
		System.out.println("Do n");
	}
	
	public static void main (String[] args){
		(new JavaIOExample()).process();
	}
	
	public void process() {
		WriteFile();
		ReadFile();
	}
	
	public void WriteFile() {
		FileWriter fileWriter;
		try{
		fileWriter = new FileWriter("speedtests.html", true);

        //TODO
        //close writer
        //writer.close();
		fileWriter.write("<table>\n");
		
		fileWriter.close();
		
		} catch(Exception e){
			System.out.println("Error writing to file");
		}
	}
	
	public void ReadFile() {
		FileReader fileReader;
		try {
			fileReader = new FileReader("");
			
			fileReader.read();
			
			fileReader.close();
		} catch(Exception e) {
			System.out.println("Error reading file");
		}
	}
}
