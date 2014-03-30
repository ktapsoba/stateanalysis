package resource;

public class ContextualState {
	private final Context ctx;
	private final State state;
	
	public ContextualState(Context ctx, State state){
		this.ctx = ctx;
		this.state = state;
	}
	
	public State getState(){
		return state;
	}
	
	public Context getContext(){
		return ctx;
	}
	
	

	@Override
	public String toString() {
		return "[" + ctx.toString() + ": " + state.toString() + "]";
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ContextualState){
			ContextualState ctxState = (ContextualState)object;
			return ctxState.state.equals(state) && ctxState.ctx.equals(ctx);
		}
		return false;
	}
	
	

}
