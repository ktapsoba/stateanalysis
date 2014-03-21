package resource;

public class Action {
	Action(){}
	
	static boolean isValidAction(StateType state, Method method){
		if (state instanceof Connected){
			return isValidAction((Connected)state, method );
		}
		else if (state instanceof NotConnected) {
			return isValidAction((NotConnected)state, method);
		}
		else if (state instanceof Statement){
			return isValidAction((Statement)state, method);
		}
		else if (state instanceof Result){
			return isValidAction((Result)state, method);
		}
		else if (state instanceof LoggedIn){
			return isValidAction((LoggedIn)state, method);
		}
		else if (state instanceof LoggedOut){
			return isValidAction((LoggedOut)state, method);
		}
		return false;
	}
	
	private static boolean isValidAction(Connected state, Method method){
		//rules for JDBC
		if(method.isGetConnection() || method.isCreateStatement() || method.isCloseConnection()){
			return true;
		}
		//rules for FTPClient
		else if( method.isDisconnect() || method.isLogin() || method.isConnect()){
			return true;
		}
		return false;
	}
	
	private static boolean isValidAction(NotConnected state, Method method){
		//rules for JDBC
		if (method.isGetConnection() || method.isCloseConnection() || method.isCloseResult() || method.isCloseStatement()){
			return true;
		}
		//rules for FTPClient
		if (method.isDisconnect() || method.isConnect()){
			return true;
		}
		return false;
	}
	
	private static boolean isValidAction(Statement state, Method method){
		if (method.isCloseStatement() || method.isCreateStatement() || method.isExecuteQuery()){
			return true;
		}
		return false;
	}
	
	private static boolean isValidAction(Result state, Method method){
		if(method.isCloseResult())
			return true;
		return false;
	}
	
	//ONLY FTPClient specific
	private static boolean isValidAction(LoggedIn state, Method method){
		if(method.isLogin() || method.isDisconnect() || method.isLogout()){
			return true;
		}
		return false;
	}
	
	private static boolean isValidAction(LoggedOut state, Method method){
		if (method.isLogin()){
			return true;
		}
		return false;
	}
}
