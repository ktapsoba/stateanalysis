package example;

import test.RepositoryTest;

public class SimpleExample {
	
	public static void main (String[] args){
		Unit unit = new Unit();
		
		unit.setArea(1000);
		unit.setNumber("A");
		
		RepositoryTest repoTest = new RepositoryTest();
		repoTest.testAll();
	}

}
