package resource;

public class Context {
	private static int counter;
	private final int label;
	
	private Context(int label){
		this.label = label;
	}
	
	public static Context getNewContext(){
		return new Context(++counter);
	}
	
	@Override
	public String toString(){
		return Integer.toString(label);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Context){
			Context context = (Context)object;
			return context.label == label;
		}
		return false;
	}
}
