package resource;

public class Action {
	Method method;
	
	public Action(Method method){
		this.method = method;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Action){
			Action action = (Action)object;
			return action.method.equals(method);
		}
		return false;
	}
	
	
	
}
