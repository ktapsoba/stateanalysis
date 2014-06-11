package resource;

public class Action {
	Method method;
	
	public Action(Method method){
		this.method = method;
	}
	
	public String toString() {
		return method.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Action){
			Action action = (Action)object;
			return action.method.equals(method);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return method.hashCode() * 1234;
	}
	
	
	
}
