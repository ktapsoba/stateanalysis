package analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import resource.Action;
import resource.Method;
import resource.NewConfiguration;
import resource.State;
import soot.G;
import soot.Local;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.util.Chain;

public class NewVisitor {
	private Map<Local, Set<State>> input, output;
	private NewConfiguration config;
	InterProceduralCFG cfg;
	Chain<Local> localVariables;
	

	public NewVisitor(){}
	
	public void visit(Stmt stmt, Map<Local, Set<State>> input, Map<Local, Set<State>> output, InterProceduralCFG cfg, Chain<Local> localVariables) throws InvalidCallError{
		this.input = input;
		this.output = output;
		this.cfg = cfg;
		this.localVariables = localVariables;
		visit(stmt);
	}
	
	private void visit(Stmt stmt) throws InvalidCallError{
		if(stmt instanceof IdentityStmt){
			visit((IdentityStmt)stmt);
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
		else if (stmt instanceof ReturnStmt){
			visit((ReturnStmt) stmt);
		}
		else if (stmt instanceof RetStmt) {
			visit((RetStmt) stmt);
		}
		else if (stmt instanceof ReturnVoidStmt){
			visit((ReturnVoidStmt) stmt);
		}
		else {
			//throw new Exception("Cannot identiy Statement " + stmt);
			G.v().out.println("cannot identify statement " + stmt);
		}
	}
	
	private void visit(IdentityStmt stmt){
		//x ::= @parameter1: type
		// use when arguments are passed to a method
		Value value = stmt.getLeftOp();
		
		//G.v().out.println("Identity Stmt--> " + stmt + " local--> " + getLocalVariable(value) + " type--> " + value.getType());
		
	}
	
	private void visit(AssignStmt stmt) throws InvalidCallError{
		StringBuilder sb = new StringBuilder();
		sb.append("Assign Stmt--> ");
		sb.append(stmt);
		if(cfg.isBranchStmt(stmt)){
			sb.append(" --->Branching");
		} else if (cfg.isCallStmt(stmt)){
			sb.append(" --->calling");
			Method method = cfg.getMethod(stmt);
			if(NewConfiguration.containsMethod(method)){
				sb.append(":contains Method");
				if(NewConfiguration.containsAction(method)){
					sb.append("+Action");
					Action action = NewConfiguration.getAction(method);
					Set<State> newStates = NewConfiguration.getStatesByAction(action);
					sb.append(" newStates-->" + newStates);
					G.v().out.println(sb.toString());
					Object object = stmt.getInvokeExpr().getUseBoxes().get(0);
					sb.append("got Object " + object);
					//G.v().out.println(sb.toString());
					Local rhsL = null;
					State inState = State.getBottom();
					if(object instanceof JimpleLocalBox){
						JimpleLocalBox jlBox = (JimpleLocalBox) object;
						Local local = getLocalVariable(jlBox.getValue());
						inState = NewConfiguration.getHighestState(input.get(local));
						rhsL = local;
					}
					sb.append(" rhsL:" + rhsL);
					G.v().out.println(sb.toString());
					Set<State> outputStates = new HashSet<>();
					for(State outState : newStates){
						if(NewConfiguration.checkTransition(inState, outState, action)){
							outputStates.add(outState);
						}
					}
					if(outputStates.size() > 0){
						Local lhsL = getLocalVariable(stmt.getLeftOp());
						output.put(lhsL, outputStates);
					} else {
						throw new InvalidCallError(stmt, inState, action);
					}
					
				}
			}
		} else {
			sb.append(" --->normal");
			Local rhs = getLocalVariable(stmt.getRightOp());
			G.v().out.println("rhs:" + stmt.getRightOp());
			if(stmt.getRightOp() instanceof DefinitionStmt){
				G.v().out.println("declaration");
			}
			if(input.containsKey(rhs)){
				Local lhs = getLocalVariable(stmt.getLeftOp());
				sb.append(":exists");
				State inState = NewConfiguration.getHighestState(input.get(lhs));
				Set<State> outStates = input.get(rhs);
				sb.append(" newStates-->" + outStates);
				
				Set<State> outputStates = new HashSet<>();
				for(State state : outStates){
					if(NewConfiguration.checkTransition(inState, state, null)){
						outputStates.add(state);
					}
				}
				if(outputStates.size() > 0){
					output.put(lhs, outputStates);
				} else {
					throw new InvalidCallError(stmt, inState, null);
				}
			}
		}
		G.v().out.println(sb.toString());
	}
	
	private void visit(InvokeStmt stmt) throws InvalidCallError{
		StringBuilder sb = new StringBuilder();
		sb.append("Invoke Stmt--> ");
		sb.append(stmt);
		if(cfg.isBranchStmt(stmt)){
			sb.append(" --->Branching");
		} else if(cfg.isCallStmt(stmt)){
			sb.append(" --->Calling");
			Method method = cfg.getMethod(stmt);
			if (NewConfiguration.containsMethod(method)){
				sb.append(":contains");
				Action action = NewConfiguration.getAction(method);
				Set<State> newStates = NewConfiguration.getStatesByAction(action);
				sb.append(" newStates--->" + newStates);
				Local local = getLocalVariable(stmt.getUseBoxes().get(0).getValue());
				State inState = NewConfiguration.getHighestState(input.get(local));
				Set<State> outputStates = new HashSet<>();
				for(State state : newStates){
					if(NewConfiguration.checkTransition(inState, state, action)){
						outputStates.add(state);
					}
				}
				if(outputStates.size() > 0){
					output.put(local, outputStates);
				} else {
					throw new InvalidCallError(stmt, inState, action);
				}
			}
		} else {
			sb.append(" --->Normal");
		}
		G.v().out.println(sb.toString());
	}
	
	private void visit(IfStmt stmt){
		
	}
	
	private void visit(GotoStmt stmt){
		
	}
	
	private void visit(TableSwitchStmt stmt){
		
	}
	
	private void visit(LookupSwitchStmt stmt){
		
	}
	
	private void visit(NopStmt stmt){
		
	}
	
	private void visit(ReturnStmt stmt){
		
	}
	
	private void visit(RetStmt stmt){
		
	}
	
	private void visit(ReturnVoidStmt stmt){
		
	}
	
	private Local getLocalVariable(Value value){
		for(Local localVariable : localVariables){
			if(localVariable.getName().equals(value.toString())){
				return localVariable;
			}
		}
		return null;
	}

}
