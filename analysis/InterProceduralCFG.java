package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import resource.Method;
import soot.Body;
import soot.G;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.EdgePredicate;
import soot.jimple.toolkits.callgraph.Filter;
import soot.toolkits.exceptions.UnitThrowAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.Chain;

public class InterProceduralCFG{

	//being used
	private final CallGraph callGraph;
	private final Map<Method, Body> methodToBody = new HashMap<>();
	
	//retains only callers that are explicit call sites or Thread.start()
	protected static class EdgeFilter extends Filter {		
		protected EdgeFilter() {
			super(new EdgePredicate() {
				public boolean want(Edge e) {				
					return e.kind().isExplicit() || e.kind().isThread();
				}
			});
		}
	}

	// being used
	public InterProceduralCFG() {
		callGraph = Scene.v().getCallGraph();
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		
		for(SootClass sootcl : appClasses){
			G.v().out.println("Applications class " + sootcl.toString());
		}
		SootClass sootClass = Scene.v().getMainClass();
		List<SootMethod> sootMethods = sootClass.getMethods();
		
		
		for(SootMethod sootMethod : sootMethods){
			if (sootMethod.hasActiveBody()){
				Body body = sootMethod.getActiveBody();
				Method method = toMethod(sootMethod);
				methodToBody.put(method, body);
			}
		}
	}
	
	public boolean isDeclarationStmt(Stmt stmt){
		return false;
	}
	
	public boolean isCallStmt(Stmt stmt){
		return stmt.containsInvokeExpr();
	}
	
	public boolean hasActiveBody(Method method){
		return methodToBody.containsKey(method);
	}
	
	public Body getActiveBody(Method method){
		return methodToBody.get(method);
	}
	
	public Body getActiveBody(Stmt stmt){
		Method method = getMethod(stmt.getInvokeExpr());
		return getActiveBody(method);
	}
	
	public boolean isBranchStmt(Stmt stmt){
		if (isCallStmt(stmt)){
			Method method = getMethod(stmt.getInvokeExpr());
			return hasActiveBody(method);
		}
		return false;
	}
	
	// creates a directed graph from the given body
	public synchronized DirectedGraph<Unit> makeGraph(Body body) {
		return new ExceptionalUnitGraph(body, UnitThrowAnalysis.v() ,true);
	}
	
	public synchronized DirectedGraph<Unit> makeGraph(Stmt stmt) {
		Body body = getActiveBody(stmt);
		return makeGraph(body);
	}
	
	public List<Value> getArguments(Stmt stmt){
		return stmt.getInvokeExpr().getArgs();
	}
	
	public List<Local> getParameters(Stmt stmt){
		List<Local> parameters = new ArrayList<>();
		Body body = getActiveBody(stmt);
		for(int i = 0; i<getArguments(stmt).size(); i++){
			parameters.add(body.getParameterLocal(i));
		}
		return parameters;
	}
	
	public synchronized Chain<Local> getLocals(Stmt stmt) {
		Body body = getActiveBody(stmt);
		return body.getLocals();
	}
	
	public Method toMethod(SootMethod sootMethod){
		String methodClass = sootMethod.getDeclaringClass().getShortName();
		String methodName = sootMethod.getName();
		Method method = new Method(methodClass, methodName);
		return method;
	}
	
	public Set<SootMethod> getCalleesOfCallAt(Unit unit) {
		return unitToCallees(unit);
	}
	
	private Set<SootMethod> unitToCallees(Unit unit){
		Set<SootMethod> res = new LinkedHashSet<SootMethod>();
		//only retain callers that are explicit call sites or Thread.start()
		Iterator<Edge> edgeIter = new EdgeFilter().wrap(callGraph.edgesOutOf(unit));					
		while(edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			if(edge.getTgt()==null) {
				System.err.println();
			}
			SootMethod m = edge.getTgt().method();
			if(m.hasActiveBody())
			res.add(m);
		}
		return res; 
	}
	
	private Method getMethod(InvokeExpr invokeExpr){
		String methodName = invokeExpr.getMethod().getName();
		String methodClass = invokeExpr.getMethodRef().declaringClass().getShortName();
		return new Method(methodClass, methodName);
	}
	
	public Method getMethod(Stmt stmt){
		return getMethod(stmt.getInvokeExpr());
	}
}
