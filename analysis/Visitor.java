package analysis;

import resource.Method;
import resource.State;
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

public class Visitor {
	State input;
	State output;
	
	private static Visitor visitor = new Visitor();
	public static Visitor getInstance() { return visitor; }
	
	private Visitor(){ }
	
	public void visit(Stmt stmt, State input, State output){
		this.input = input;
		this.output = output;
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
		
		

	}
	
	private void visit(InvokeStmt stmt){
		Value local = stmt.getUseBoxes().get(0).getValue();
		Method method = getMethod(stmt.getInvokeExpr());
	}
	
	private Method getMethod(InvokeExpr invokeExpr){
		String methodName = invokeExpr.getMethod().getName();
		String methodClass = invokeExpr.getMethodRef().declaringClass().getShortName();
		return new Method(methodClass , methodName);
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
