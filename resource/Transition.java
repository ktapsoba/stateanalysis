package resource;

import soot.G;
import soot.Local;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocalBox;

public class Transition {
	
	private static Transition transition = new Transition();
	
	private Transition(){
	}
	
	public static Transition getInstance() { return transition;} 
	
	public State getNewState(State state, Method method, Local local){
		if (method.isInvalidMethod()){
			return state;
		}
		state.put(local, method.getState());
		return state;
	}
	
	
	/*
	 * 
	 */
	
	public void checkStateStmt(Stmt stmt, State inState, State outState){
		boolean valid = true;
		if (stmt instanceof AssignStmt){
			valid = checkAssignStmt((AssignStmt)stmt, inState, outState);
		}
		else if (stmt instanceof InvokeStmt){
			valid = checkInvokeStmt((InvokeStmt)stmt, inState, outState);
		}
		
		if (!valid) {
			G.v().out.println("ERRRRRRRRRRRRRRRRRRRRRRRRR" + stmt.toString());
		}
	}
	
	private boolean checkInvokeStmt(InvokeStmt stmt, State inState, State outState){
		Value local = stmt.getUseBoxes().get(0).getValue();
		Method method = getMethod(stmt.getInvokeExpr());
		
		if (method.isInvalidMethod()){
			return true;
		}
		
		StateType inStateType = inState.get((Local)local);
		StateType outStateType = outState.get((Local)local);
		if (!Action.isValidAction(inStateType, method))
			return false;
		if (method.getState() != outStateType)
			return false;
		return true;
	}
	
	private boolean checkAssignStmt(AssignStmt stmt, State inState, State outState){
		Value lhs = stmt.getLeftOp();
		Value rhs = stmt.getRightOp();
		
		if(stmt.containsInvokeExpr()){
			Method method = getMethod(stmt.getInvokeExpr());
			Object obj = stmt.getInvokeExpr().getUseBoxes().get(0);
			if (obj instanceof JimpleLocalBox){
				JimpleLocalBox objJ = (JimpleLocalBox)obj;
				Value val = objJ.getValue();
			
				if (method.isInvalidMethod()){
					return true;
				}
				
				if(inState.containsLocal(val.toString())){
					
					if(val.toString().equals("stmt")){
						G.v().out.println("inState -> " + inState.get((Local)val));
						G.v().out.println("outState -> " + outState.get((Local)lhs));
						G.v().out.println("method -> " + method.toString());
						G.v().out.println("valid??? " + Action.isValidAction(inState.get((Local)val), method));
					}
					
					G.v().out.println("rhs in InState " + rhs.toString());
					StateType rhsInStateType = inState.get((Local)val);
					if(!Action.isValidAction(rhsInStateType, method))
						return false;
					if (outState.get((Local)lhs) != method.getState())
						return false;
				}
			}
		}
		else {
			if (inState.containsLocal(rhs.toString())){
				if(inState.get((Local)rhs) != outState.get((Local)lhs))
					return false;
			}
		}
		
		return true;
	}
	
	private Method getMethod(InvokeExpr invokeExpr){
		String methodName = invokeExpr.getMethod().getName();
		String methodClass = invokeExpr.getMethodRef().declaringClass().getShortName();
		return Method.getMethodByName(methodClass + "." + methodName);
	}
	
}