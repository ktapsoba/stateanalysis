package analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resource.Action;
import resource.Configuration;
import resource.Method;
import resource.State;
import soot.G;
import soot.Local;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JimpleLocalBox;

public class Visitor {
	Map<Local,State> input;
	Map<Local,State> output;
	List<State> statesIn;
	List<State> statesOut;
	Configuration config;
	private Map<Stmt, Method> methodByStmt = new HashMap<>();
	
	private static Visitor visitor = new Visitor();
	public static Visitor getInstance() { return visitor; }
	
	public Map<Stmt, Method> getMethodByStmt(){
		return methodByStmt;
	}
	
	private Visitor(){ }
	
	public void visit(Stmt stmt, Map<Local,State> input, Map<Local,State> output, Configuration config){
		this.input = input;
		this.output = output;
		this.config = config;
		visit(stmt);
	}
	
	public void visit(Stmt stmt, List<State> input, List<State> output, Configuration config){
		this.config = config;
		this.statesIn = input;
		this.statesOut = output;
		visit(stmt);
	}
	
	private void visit(Stmt stmt){
		
		if (stmt instanceof IdentityStmt){
			visit((IdentityStmt) stmt);
		}
		else if (stmt instanceof AssignStmt){
			visit((AssignStmt) stmt);
		}
		else if (stmt instanceof InvokeStmt){
			visit((InvokeStmt) stmt);
		}
		else if (stmt instanceof IfStmt){
			visit((IfStmt) stmt);
		}
		else if (stmt instanceof GotoStmt){
			visit((GotoStmt) stmt);
		}
		else if (stmt instanceof TableSwitchStmt){
			visit((TableSwitchStmt) stmt);
		}
		else if (stmt instanceof LookupSwitchStmt){
			visit((LookupSwitchStmt) stmt);
		}
		else if (stmt instanceof NopStmt){
			visit((NopStmt) stmt);
		}
		else {
			//G.v().out.println("Other stmt" + stmt.toString());
		}
	}
	
	private void visit(IdentityStmt stmt){
		//G.v().out.println("Identity --> " + stmt.toString());
	}
	
	private void visit(AssignStmt stmt){
		Value lhs = stmt.getLeftOp();
		Value rhs = stmt.getRightOp();
		
		if (input.containsKey(rhs)){
			State outState = input.get((Local)rhs);
			State inState = config.getBottomState();
			if (input.containsKey(lhs)){
				inState = input.get((Local)lhs);
			}
			if (config.checkTransition(inState, outState, null)){
				output.put((Local)lhs, input.get((Local)rhs));
			}
			else {
				G.v().out.println("ERRRRRRRRRRRRRRRRRRRRRR at transition ASS in ou null " + stmt.toString());
			}
			
		}
		else if (stmt.containsInvokeExpr()) {
			Method method = getMethod(stmt.getInvokeExpr());
			if (method != null){
				methodByStmt.put(stmt, method);
				Action action = config.getAction(method);
				List<State> newStates = config.getStatesByAction(action);
				Object object = stmt.getInvokeExpr().getUseBoxes().get(0);
				State inState = config.getBottomState();
				if(object instanceof JimpleLocalBox){
					JimpleLocalBox jlBox = (JimpleLocalBox)object;
					Local local = (Local)jlBox.getValue();
					if (input.containsKey(local)){
						inState = input.get(local);
					}
				}				
				if (config.checkTransition(inState, newStates.get(0), action)){
					output.put((Local)lhs, newStates.get(0));
				} else {
					G.v().out.println("ERRRRRRRRRRRRRRRRRR ass " + stmt.toString());
				}
			}
		}
	}
	
	private void visit(InvokeStmt stmt){
		Value value = stmt.getUseBoxes().get(0).getValue();
		Method method = getMethod(stmt.getInvokeExpr());
		
		if (method != null){
			methodByStmt.put(stmt, method);
			Action action = config.getAction(method);
			List<State> newStates = config.getStatesByAction(action);
			State inState = config.getBottomState();
			if(input.containsKey((Local)value)){
				inState = input.get((Local)value);
			}
			if (config.checkTransition(inState, newStates.get(0), action)){
				output.put((Local)value, newStates.get(0));
			}
			else{
				G.v().out.println("ERRRRRRRRRRRRRRRRRRRRRRRRRRRRR in transition");
			}
		}
	}
	
	private Method getMethod(InvokeExpr invokeExpr){
		String methodName = invokeExpr.getMethod().getName();
		String methodClass = invokeExpr.getMethodRef().declaringClass().getShortName();
		return config.getMethod(methodClass, methodName);
	}
	
	private void visit(IfStmt stmt){
	//	G.v().out.println("IfStmt --> " + stmt.toString());
	//	G.v().out.println("Condition --> " + stmt.getCondition().toString());
	}
	
	private void visit(GotoStmt stmt){
		//G.v().out.println("GotoStmt --> " + stmt.toString());
	}
	
	private void visit(TableSwitchStmt stmt){
		//G.v().out.println("TableSwitchStmt --> " + stmt.toString());
	}
	
	private void visit(LookupSwitchStmt stmt){
		//G.v().out.println("LookupSwitchStmt --> " + stmt.toString());
	}
	
	private void visit(NopStmt stmt){
		//G.v().out.println("NopStmt --> " + stmt.toString());
	}
}
