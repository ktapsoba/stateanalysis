package resource;


public class State  {
	private final String name;
	
	public State(){
		this.name = "No State";
	}
	
	public State(String name){
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
}
