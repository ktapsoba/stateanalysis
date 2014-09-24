package resource;

import java.util.HashSet;
import java.util.Set;

public class Action {
	Set<Method> methods = new HashSet<>();
	String name;

	public Action(String name){
		this.name = name;
	}

	public void addMethod(Method method){
		methods.add(method);
	}

	public String toString() {
		return name + "::= <{" + methods.toString() + "}>";
	}
	
	public boolean hasMethod (Method method){
		return methods.contains(method);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Action) {
			Action action = (Action) object;
			return action.methods.equals(methods);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return methods.hashCode() * 1234;
	}

}
