package programAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.Action;
import resource.Method;
import resource.State;
import soot.G;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.toolkits.graph.DirectedGraph;
import soot.util.Chain;
import soot.util.Heap;
import verification.Configuration;
import verification.InvalidAPICallException;
import verification.TransferFunction;

public class StatementVisitor {
	private Map<Local, Set<State>> input, output;
	private Configuration config;
	ControlFlowGraph cfg;
	Chain<Local> localVariables;
	Heap heap;
	Environment environment;
	DependencyMap dependencyMap;
    Map<Local, Set<State>> RET_State;

	public StatementVisitor() {
	}

	public void visit(Stmt stmt, Map<Local, Set<State>> input,
			Map<Local, Set<State>> output, ControlFlowGraph cfg,
			Chain<Local> localVariables, Environment environment, DependencyMap dependencyMap) throws InvalidAPICallException {
		this.input = input;
		this.output = output;
		this.cfg = cfg;
		this.localVariables = localVariables;
		this.environment = environment;
		this.dependencyMap = dependencyMap;
		visit(stmt);
	}

	private void visit(Stmt stmt) throws InvalidAPICallException {
		if (stmt instanceof IdentityStmt) {
			visit((IdentityStmt) stmt);
		} else if (stmt instanceof AssignStmt) {
			visit((AssignStmt) stmt);
		} else if (stmt instanceof InvokeStmt) {
			visit((InvokeStmt) stmt);
		} else if (stmt instanceof IfStmt) {
			visit((IfStmt) stmt);
		} else if (stmt instanceof GotoStmt) {
			visit((GotoStmt) stmt);
		} else if (stmt instanceof TableSwitchStmt) {
			visit((TableSwitchStmt) stmt);
		} else if (stmt instanceof LookupSwitchStmt) {
			visit((LookupSwitchStmt) stmt);
		} else if (stmt instanceof NopStmt) {
			visit((NopStmt) stmt);
		} else if (stmt instanceof ReturnStmt) {
			visit((ReturnStmt) stmt);
		} else if (stmt instanceof RetStmt) {
			visit((RetStmt) stmt);
		} else if (stmt instanceof ReturnVoidStmt) {
			visit((ReturnVoidStmt) stmt);
		} else {
			// throw new Exception("Cannot identiy Statement " + stmt);
			G.v().out.println("cannot identify statement " + stmt);
		}
	}

	private void visit(IdentityStmt stmt) {
		// x ::= @parameter1: type
		// use when arguments are passed to a method
		Value value = stmt.getLeftOp();

		// G.v().out.println("Identity Stmt--> " + stmt + " local--> " +
		// getLocalVariable(value) + " type--> " + value.getType());

	}

	// check for assignment with NewExpr on the right hand side.
	// if newExpr, then get the type and check if class exists or not.
	private void visit(AssignStmt stmt) throws InvalidAPICallException {
		StringBuilder sb = new StringBuilder();
		sb.append("Assign Stmt--> ");
		sb.append(stmt);
		if (cfg.isBranchStmt(stmt)) {
			sb.append(" --->Branching");
			processBranchStmt(stmt);
		} else if (cfg.isCallStmt(stmt)) {
			sb.append(" --->calling");
			Method method = cfg.getMethod(stmt);
			if (Configuration.containsMethod(method)) {
				sb.append(":contains Method " + method.toString());
				if (Configuration.containsAction(method)) {
					sb.append(" + Action");
					
					Object object = stmt.getInvokeExpr().getUseBoxes().get(0);
					sb.append("got Object " + object);
					// G.v().out.println(sb.toString());
					Local rhsL = null;
					
					if (object instanceof JimpleLocalBox) {
						JimpleLocalBox jlBox = (JimpleLocalBox) object;
						Local local = getLocalVariable(jlBox.getValue());
						//inState = Configuration.getHighestState(input.get(local));
						rhsL = local;
					}
					//get actions
					List<Action> actions = Configuration.getActions(method);
					sb.append(" :" + actions);
					//G.v().out.println(sb.toString());
					//get inStates
					List<State> inStates = input.containsKey(rhsL)? environment.getStates(stmt, rhsL) : Arrays.asList(State.getBottom());
					
					//create transfer functions
					List<TransferFunction> transferFunctions = createTransferFunctions(stmt, inStates);
					
					//Apply Transfer Functions
					Set<State> outputStates = applyTransferFunctions(actions, transferFunctions);
					
					if(outputStates.size() > 0){
						Local lhsL = getLocalVariable(stmt.getLeftOp());
						environment.updateLocal(stmt, lhsL, outputStates);
						Set<State> inputStates = input.containsKey(lhsL) ? input.get(lhsL) : new HashSet<State>();//input.get(lhsL) : new HashSet<>();
						inputStates.addAll(outputStates);
						output.put(lhsL, inputStates);
						dependencyMap.addDependent(rhsL, lhsL);
						for(State state : outputStates){
						    if(Configuration.getBaseState(state).equals(state)){
						        output = dependencyMap.updateDependentsOf(stmt, lhsL, environment, output);
						    }
						    else {
						        dependencyMap.removeDependent(lhsL);
						    }
						}
					}
					else {
						throw new InvalidAPICallException(stmt, inStates, actions);
					}
				}
			}
		} else {
			sb.append(" --->normal");
			Value rightOperand = stmt.getRightOp();
			Local rhs = getLocalVariable(rightOperand);
			G.v().out.println("rhs:" + stmt.getRightOp());
			if (rightOperand instanceof NewExpr) {
				G.v().out.println("declaration " + rightOperand.getType());
				if (cfg.isApplicationClass(rightOperand.getType().toString())) {
					G.v().out.println("put in heap");
				}
			} else if (input.containsKey(rhs)) {
				Local lhs = getLocalVariable(stmt.getLeftOp());
				sb.append(":exists");
				output.put(lhs, input.get(rhs));
			}
		}
		G.v().out.println(sb.toString());
	}

	private void visit(InvokeStmt stmt) throws InvalidAPICallException {
		StringBuilder sb = new StringBuilder();
		sb.append("Invoke Stmt--> ");
		sb.append(stmt);
		if (cfg.isBranchStmt(stmt)) {
			sb.append(" --->Branching");
			processBranchStmt(stmt);
		} else if (cfg.isCallStmt(stmt)) {
			sb.append(" --->Calling");
			Method method = cfg.getMethod(stmt);
			if (Configuration.containsMethod(method)) {
				sb.append(":contains");
				Local local = getLocalVariable(stmt.getUseBoxes().get(0).getValue());
				//get actions
				List<Action> actions = Configuration.getActions(method);
				
				//get inStates
				List<State> inStates = input.containsKey(local)? environment.getStates(stmt, local) : Arrays.asList(State.getBottom());
				
				//create transfer functions
				List<TransferFunction> transferFunctions = createTransferFunctions(stmt, inStates);
				
				//Apply Transfer Functions
				Set<State> outputStates = applyTransferFunctions(actions, transferFunctions);
				
				if(outputStates.size() > 0){
					environment.updateLocal(stmt, local, outputStates);
					Set<State> inputStates = input.containsKey(local)? input.get(local) : new HashSet<State>();
					inputStates.addAll(outputStates);
					output.put(local, inputStates);

                    for(State state : outputStates){
                        if(Configuration.isBaseState(state)){
                            G.v().out.println("is base state " + state);
                            output = dependencyMap.updateDependentsOf(stmt, local, environment, output);
                        }
                    }
				}
				else {
					throw new InvalidAPICallException(stmt, inStates, actions);
				}
				
				/*State inState = Configuration.getHighestState(input.get(local));
				TransferFunction tf = new TransferFunction(stmt, inState);
				Set<State> outputStates = tf.apply(Configuration.getStatesByAction(action), action);
				output.put(local, outputStates);*/
			}
		} else {
			sb.append(" --->Normal");
		}
		G.v().out.println(sb.toString());
	}

	private void visit(IfStmt stmt) {

	}

	private void visit(GotoStmt stmt) {

	}

	private void visit(TableSwitchStmt stmt) {

	}

	private void visit(LookupSwitchStmt stmt) {

	}

	private void visit(NopStmt stmt) {

	}

	private void visit(ReturnStmt stmt) {

	}

	private void visit(RetStmt stmt) {
	}

	private void visit(ReturnVoidStmt stmt) {
	    RET_State = output;
	}
	
	public Map<Local, Set<State>> getReturnStates() {
	    return RET_State;
	}

	private Local getLocalVariable(Value value) {
		for (Local localVariable : localVariables) {
			if (localVariable.getName().equals(value.toString())) {
				return localVariable;
			}
		}
		return null;
	}
	
	private List<TransferFunction> createTransferFunctions(Stmt stmt, List<State> inStates){
		List<TransferFunction> transferFunctions = new ArrayList<>();
		for(State inState : inStates){
			transferFunctions.add(new TransferFunction(stmt, inState));
		}
		return transferFunctions;
	}
	
	private Set<State> applyTransferFunctions(List<Action> actions, List<TransferFunction> transferFunctions){
		Set<State> states = new HashSet<>();
		for(TransferFunction transferFunction: transferFunctions){
			for(Action action : actions){
				states.addAll(transferFunction.apply(Configuration.getStatesByAction(action), action));
			}
		}
		return states;
	}
	
	private void processBranchStmt(Stmt stmt){
	    G.v().out.println("*****************BRANCH START****************");
	    List<Value> args = cfg.getArguments(stmt);
	    List<Local> parameters = cfg.getParameters(stmt);
	    Map<Local, Set<State>> newInput = getInputToPass(args, parameters);
	    DirectedGraph<Unit> newGraph = cfg.makeGraph(stmt);
	    Chain<Local> newLocals = cfg.getLocals(stmt);
	    DataFlowAnalysis dataFlowAnalysis = new DataFlowAnalysis(newGraph, newInput, newLocals, cfg, environment, dependencyMap);
	    dataFlowAnalysis.startAnalysis();
	    Map<Local, Set<State>> newOutput = dataFlowAnalysis.getReturnStates();
	    Map<Local, Set<State>> outputStates = getOutputFromCall(newOutput, args, parameters);
	    for(Local local : outputStates.keySet()){
	        output.put(local, outputStates.get(local));
	    }
	    G.v().out.println("*****************BRANCH DONE****************");
	}
	
	private Map<Local, Set<State>> getInputToPass(List<Value> arguments, List<Local> parameters){
	    Map<Local, Set<State>> newInput = new HashMap<>();
	    Map<Integer, Local> parametersName = new HashMap<>();
	    Integer pos = 0;
	    for(Local local : parameters){
	        parametersName.put(pos, local);
	        pos++;
	    }
	    pos = 0;
	    for(Value value : arguments) {
	        Local local = getLocalVariable(value);
	        if(local != null && input.containsKey(local)){
	            newInput.put(parametersName.get(pos), input.get(local));
	            pos++;
	        }
	    }
	    return newInput;
	}
	
	public Map<Local, Set<State>> getOutputFromCall(Map<Local, Set<State>> newOutput, List<Value> arguments, List<Local> parameters){
	    Map<Local, Set<State>> toAddorUpdate = new HashMap();
        Map<String, Local> argumentsByName = new HashMap<>();
        for(Value value : arguments){
            Local local = getLocalVariable(value);
            if(local != null){
                argumentsByName.put(local.getName(), local);
            }
        }
        //G.v().out.println("Agrs Map " + argumentsByName);
        for(Local local : parameters){
            if (newOutput.containsKey(local)){
                toAddorUpdate.put(argumentsByName.get(local.getName()), newOutput.get(local));
            }
        }
        return toAddorUpdate;
	}
	
}
