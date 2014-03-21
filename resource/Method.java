package resource;

public class Method {
	private final String className;
	private final String name;
	
	public Method(String className, String name){
		this.className = className;
		this.name = name;
	}
	
	public String toString() { return this.className + "." + this.name; }
}