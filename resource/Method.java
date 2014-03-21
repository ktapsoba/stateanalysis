package resource;

import soot.Local;

public abstract class Method {
	final String className;
	final String name;
	Local variable;
	
	Method (String className, String name){
		this.className = className;
		this.name = name;
	}
	boolean isGetConnection() { return false; }
	boolean isCreateStatement() { return false; }
	boolean isExecuteQuery() { return false; }
	boolean isCloseResult() { return false; }
	boolean isCloseStatement() { return false; }
	boolean isCloseConnection() { return false; }
	boolean isInvalidMethod() { return false; }
	
	boolean isConnect() { return false;}
	boolean isLogin() { return false; }
	boolean isLogout() { return false; }
	boolean isDisconnect() { return false; }
	
	public Local getVariable(){
		return variable;
	}
	public void setVariable(Local variable){
		this.variable = variable;
	}
	
	public static Method getMethodByName(String name){
		if (name.equals(GetConnection.getInstance().toString())){
			return GetConnection.getInstance();
		}
		else if (name.equals(CreateStatement.getInstance().toString())){
			return CreateStatement.getInstance();
		}
		else if (name.equals(ExecuteQuery.getInstance().toString())){
			return ExecuteQuery.getInstance();
		}
		else if (name.equals(CloseResult.getInstance().toString())){
			return CloseResult.getInstance();
		}
		else if (name.equals(CloseStatement.getInstance().toString())){
			return CloseStatement.getInstance();
		}
		else if (name.equals(CloseConnection.getInstance().toString())){
			return CloseConnection.getInstance();
		}
		else if (name.equals(Connect.getInstance().toString())){
			return Connect.getInstance();
		}
		else if (name.equals(Disconnect.getInstance().toString())){
			return Disconnect.getInstance();
		}
		else if (name.equals(Login.getInstance().toString())){
			return Login.getInstance();
		}
		else if (name.equals(Logout.getInstance().toString())){
			return Logout.getInstance();
		}
		return InvalidMethod.getInstance();
	}
	
	abstract StateType getState();
	
	public String toString() { return this.className + "." + this.name; }
}

/*
 * JDBC Methods
 */

class GetConnection extends Method {
	static private GetConnection getConnection = new GetConnection();
	private GetConnection() {
		super("DriverManager", "getConnection");
	}
	boolean isGetConnection() { return true; }
	StateType getState(){ return Connected.getConnected(); }
	static GetConnection getInstance(){ return getConnection; }
}

class CreateStatement extends Method {
	static private CreateStatement createStatement = new CreateStatement();	
	private CreateStatement(){
		super("Connection", "createStatement");
	}
	boolean isCreateStatement() { return true; }
	StateType getState(){ return Statement.getStatement(); }
	static CreateStatement getInstance() { return createStatement; }
}

class ExecuteQuery extends Method {
	static private ExecuteQuery executeQuery = new ExecuteQuery();
	private ExecuteQuery() {
		super("Statement", "executeQuery");
	}
	boolean isExecuteQuery() { return true; }
	StateType getState() { return Result.getResult(); }
	static ExecuteQuery getInstance() { return executeQuery; }
}

class CloseResult extends Method {
	static private CloseResult closeResult = new CloseResult();
	private CloseResult(){
		super("ResultSet", "close");
	}
	boolean isCloseResult() { return true; }
	StateType getState() { return NotConnected.getNotConnected(); }
	static CloseResult getInstance() { return closeResult; }
}

class CloseStatement extends Method {
	static private CloseStatement closeStatement = new CloseStatement();
	private CloseStatement() {
		super("Statement", "close");
	}
	boolean isCloseStatement() { return true; }
	StateType getState() { return NotConnected.getNotConnected(); }
	static CloseStatement getInstance() { return closeStatement; }
}

class CloseConnection extends Method {
	static private CloseConnection closeConnection = new CloseConnection();
	private CloseConnection(){
		super("Connection", "close");
	}
	boolean isCloseConnection() { return true; }
	StateType getState() { return NotConnected.getNotConnected(); }
	static CloseConnection getInstance() { return closeConnection; }
}

/*
 * FTPClient Methods
 */
class Connect extends Method {
	static private Connect connect = new Connect();
	private Connect() {
		super("FTPClient", "connect");
	}
	boolean isConnect() { return true;}
	StateType getState() { return Connected.getConnected(); }
	static Connect getInstance() { return connect;}
}

class Login extends Method {
	static private Login login = new Login();
	private Login() {
		super("FTPClient", "login");
	}
	boolean isLogin() {return true;}
	StateType getState() { return LoggedIn.getLoggedIn(); }
	static Login getInstance() { return login; }
}

class Logout extends Method {
	static private Logout logout = new Logout();
	private Logout() {
		super("FTPClient", "logout");
	}
	boolean isLogout() {return true;}
	StateType getState() { return LoggedOut.getLoggedOut(); }
	static Logout getInstance() { return logout; }
}

class Disconnect extends Method {
	static private Disconnect disconnect = new Disconnect();
	private Disconnect() {
		super("FTPClient", "disconnect");
	}
	boolean isDisconnect() {return true;}
	StateType getState() { return NotConnected.getNotConnected(); }
	static Disconnect getInstance() { return disconnect; }
}

class InvalidMethod extends Method {
	static private InvalidMethod invalidMethod = new InvalidMethod();
	private InvalidMethod(){
		super("NoClass", "invalid method");
	}
	boolean isInvalidMethod() { return true; }
	static InvalidMethod getInstance() { return invalidMethod; }

	StateType getState() { return InvalidState.getInvalidState(); }
}