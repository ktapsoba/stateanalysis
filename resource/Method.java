package resource;

public class Method {
	private final String className;
	private final String name;

	public Method(String className, String name) {
		this.className = className;
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.className + "." + this.name;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Method) {
			Method method = (Method) object;
			return method.className.equals(className)
					&& method.name.equals(name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return className.hashCode() * name.hashCode();
	}

}