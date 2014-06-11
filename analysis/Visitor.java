package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import resource.Action;
import resource.Configuration;
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
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.internal.JimpleLocalBox;
import soot.toolkits.graph.DirectedGraph;
import soot.util.Chain;

public class Visitor {
	Map<Local,State> input;
	Map<Local,State> output;
	List<State> statesIn;
	List<State> statesOut;
	Configuration config;
	InterProceduralCFG cfg;
	Chain<Local> locals;
	Map<Local, State> RET_State;
	Map<Local, List<Local>> dependencies  = new HashMap<>();;
	
	//private Map<Stmt, Method> methodByStmt = new HashMap<>();
	
	//public Map<Stmt, Method> getMethodByStmt(){
	//	return methodByStmt;
	//}
	
	public Visitor(){ }
	
	public void visit(Stmt stmt, Map<Local,State> input, Map<Local,State> output, Configuration config, InterProceduralCFG cfg, Chain<Local> locals){
		this.input = input;
		this.output = output;
		this.config = config;
		this.cfg = cfg;
		this.locals = locals;
		visit(stmt);
	}
	
	public void visit(Stmt stmt, List<State> input, List<State> output, Configuration config){
		this.config = config;
		this.statesIn = input;
		this.statesOut = output;
		visit(stmt);
	}
	
	private void visit(Stmt stmt){
		G.v().out.println("visit " + stmt.toString());
		
		if (cfg.isBranchStmt(stmt)){
			G.v().out.println("*****************COMPI****************");
			List<Value> args = cfg.getArguments(stmt);
			List<Local> parameters = cfg.getParameters(stmt);
			G.v().out.println("ARGS -> " + args.toString());
			G.v().out.println("PARA -> " + parameters.toString());
			Map<Local, State> newInput = getInputToPass(args, parameters);
			G.v().out.println("*****************BRANCH START****************");
			DirectedGraph<Unit> newGraph = cfg.makeGraph(stmt);
			Chain<Local> newLocals = cfg.getLocals(stmt);
			
			ConstantPropagation cp = new ConstantPropagation(newGraph, newInput, config, cfg, newLocals);
			cp.StartAnalysis();
			Map<Local, State> newOutput = cp.getReturnStates();
			Map<Local, State> nowOutput = getOutputFromCall(newOutput, args, parameters);
			G.v().out.println("*****************************INI******************");
			G.v().out.println("OUTP ->" + nowOutput.toString());
			for(Local  key : nowOutput.keySet()){
				output.put(key, nowOutput.get(key));
			}
			G.v().out.println("*****************BRANCH DONE****************");
		}
		
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
		else if (stmt instanceof ReturnStmt){
			visit((ReturnStmt)stmt);
		}
		else if (stmt instanceof RetStmt){
			visit((RetStmt)stmt);
		}
		else if (stmt instanceof ReturnVoidStmt){
			visit((ReturnVoidStmt)stmt);
		}
		else {
			//G.v().out.println("Other stmt" + stmt.toString());
		}
	}
	
	private void visit(IdentityStmt stmt){
		//G.v().out.println("Identity --> " + stmt.toString() + " -------- " + (stmt.containsArrayRef()? stmt.getTags(): ""));
	}
	
	private void visit(AssignStmt stmt){
		
		Value lhs = stmt.getLeftOp();
		Value rhs = stmt.getRightOp();
		if(rhs.toString().equals("null")){
			output.put(getLocal(lhs), config.getNullState());
		}
		else if (input.containsKey(getLocal(rhs))){
			Local rhsL = getLocal(rhs);
			Local lhsL = getLocal(lhs);
			State outState = input.get(rhsL);
			State inState = input.containsKey(lhsL)? input.get(lhsL) :config.getBottomState();
			inState = inState == config.getNullState() ? config.getBottomState() : inState;
			if (config.checkTransition(inState, outState, null)){
				output.put(lhsL, outState);
				addDependent(rhsL, lhsL);
				updateDependencies(lhsL, outState);
			}
			else {
				throw new InvalidCallError(stmt, inState, null);
			}
			
		}
		else if (stmt.containsInvokeExpr()) {
			Method method = getMethod(stmt.getInvokeExpr());

			if (method != null){
				//methodByStmt.put(stmt, method);
				Action action = config.getAction(method);
				List<State> newStates = config.getStatesByAction(action);
				Object object = stmt.getInvokeExpr().getUseBoxes().get(0);
				State inState = config.getBottomState();
				Local rhsL = null;
				if(object instanceof JimpleLocalBox){
					JimpleLocalBox jlBox = (JimpleLocalBox)object;
					Local local = getLocal(jlBox.getValue());
					if (input.containsKey(local)){
						inState = input.get(local);
						rhsL = local;
					}
				}	
				if (config.checkTransition(inState, newStates.get(0), action)){
					//G.v().out.println("Getting new state");
					output.put(getLocal(lhs), newStates.get(0));
					if(rhsL != null){
						addDependent(rhsL, getLocal(lhs));
						updateDependencies(getLocal(lhs), newStates.get(0));
					}
				} else {
					throw new InvalidCallError(stmt, inState, action);
				}
			}
		}
	}
	
	private void visit(InvokeStmt stmt){
		
		Value value = stmt.getUseBoxes().get(0).getValue();
		Local local = getLocal(value);
		Method method = getMethod(stmt.getInvokeExpr());
		//G.v().out.println("invoke " + value + " nan " + getLocal(value));

		if (method != null){
			//methodByStmt.put(stmt, method);
			Action action = config.getAction(method);
			List<State> newStates = config.getStatesByAction(action);
			State inState = config.getBottomState();
			if(input.containsKey(local)){
				inState = input.get(local);
				//G.v().out.println("input has key " + value);
			}
			
			//TransferFunction tf = new TransferFunction(inState, action, (Local)value);
			//State rs = tf.apply(stmt, config);
			//output.put((Local)value, rs);
			
			if (config.checkTransition(inState, newStates.get(0), action)){
				output.put(local, newStates.get(0));
				updateDependencies(local, newStates.get(0));
			}
			else{
				throw new InvalidCallError(stmt, inState, action);
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
	
	private void visit(ReturnStmt stmt){
		Value value = stmt.getOp();
		//G.v().out.println("return value -> " + value);
	}
	
	private void visit(RetStmt stmt){
		//G.v().out.println("RET STtmt" + stmt.toString());
	}
	
	private void visit(ReturnVoidStmt stmt){
		//G.v().out.println("Return Void Stmt" + stmt.toString());
		RET_State = output;
	}
	
	private Map<Local, State> getInputToPass(List<Value> arguments, List<Local> parameters){
		Map<Local, State> newInput = new HashMap<>();
		Map<Integer, Local> parametersName = new HashMap<>();
		Integer pos = 0;
		for(Local param : parameters){
			parametersName.put(pos, param);
			pos++;
		}
		pos = 0;
		for(Value arg : arguments){
			Local argL = getLocal(arg);
			if (input.containsKey(argL)){
				newInput.put(parametersName.get(pos), input.get(argL));
			}
			pos++;
		}
		return newInput;
	}
	
	private Map<Local, State> getOutputFromCall(Map<Local, State> newOutput, List<Value> arguments, List<Local> parameters){
		Map<Local, State> toAddorUpdate = new HashMap();
		Map<String, Local> argumentsByName = new HashMap<>();
		for(Value arg : arguments){
			Local argL = getLocal(arg);
			if(argL != null){
				argumentsByName.put(argL.getName(), argL);
			}
		}
		//G.v().out.println("Agrs Map " + argumentsByName);
		for(Local param : parameters){
			if (newOutput.containsKey(param)){
				toAddorUpdate.put(argumentsByName.get(param.getName()), newOutput.get(param));
			}
		}
		return toAddorUpdate;
	}
	
	private Local getLocal(Value value){
		for(Local local : locals){
			if (local.getName().equals(value.toString())){
				return local;
			}
		}
		return null;
	}
	
	public Map<Local, State> getReturnStates(){
		return RET_State;
	}
	
	public void addDependent(Local parent, Local dependent){
		List<Local> dependents = new ArrayList<>();
		if(dependencies.containsKey(parent)){
			dependents = dependencies.get(parent);
		}
		dependents.add(dependent);
		dependencies.put(parent, dependents);
		//G.v().out.println(" now we got " + dependencies);
	}
	
	public void updateDependencies(Local parent, State outState){
		if(hasDependents(parent)){
			if(outState != config.getBaseState()){
				dependencies.remove(parent);
			}
			else {
				List<Local> dependents = dependencies.get(parent);
				for(Local dependent : dependents){
					output.put(dependent, config.getBaseState());
					updateDependencies(dependent, config.getBaseState());
				}
			}
		}
	}
	
	public boolean hasDependents(Local parent){
		boolean result = false;
		if(dependencies.containsKey(parent)){
			
			List<Local> dependents = dependencies.get(parent);
			//G.v().out.println("inside dependencies " + dependents);
			return dependents == null? false : !dependents.isEmpty();
		}
		return false;
	}
}
