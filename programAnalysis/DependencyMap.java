package programAnalysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import resource.State;
import soot.G;
import soot.Local;
import soot.jimple.Stmt;
import verification.Configuration;

public class DependencyMap {
    Map<Local, Set<Local>> dependentsByLocal;
    
    public DependencyMap(){
        dependentsByLocal = new HashMap<>();
    }
    
    public DependencyMap(DependencyMap dependencyMap){
        dependentsByLocal = dependencyMap.dependentsByLocal;
    }
    
    public void addDependent(Local variable, Local dependent){
        if(variable != null){
            Set<Local> dependents = dependentsByLocal.containsKey(variable) ? dependentsByLocal.get(variable) : new HashSet<Local>();
            dependents.add(dependent);
            dependentsByLocal.put(variable, dependents);
        }
    }
    
    public void removeDependent(Local variable){
        if(dependentsByLocal.containsKey(variable)){
            dependentsByLocal.remove(variable);
        }
    }
    
    public boolean hasDependents(Local variable) {
        if(dependentsByLocal.containsKey(variable)){
            return !dependentsByLocal.get(variable).isEmpty();
        }
        return false;
    }
    
    public Map<Local, Set<State>> updateDependentsOf(Stmt stmt, Local variable, Environment environment, Map<Local, Set<State>> output) {
        G.v().out.println("updating dependents of " + variable + " " + dependentsByLocal.get(variable));
        if(hasDependents(variable)){
            for(Local dependent : dependentsByLocal.get(variable)){
                for(State state : environment.getStates(stmt, dependent)){
                    Set<State> baseStates = new HashSet<>();
                    baseStates.add(Configuration.getBaseState(state));
                    environment.updateLocal(stmt, dependent, baseStates);
                    Set<State> outputStates = output.get(dependent);
                    output.put(dependent, outputStates);
                    updateDependentsOf(stmt, dependent, environment, output);
                }
            }
        }
        return output;
    }

    @Override
    public String toString() {
        String ret = "{";
        for(Entry<Local, Set<Local>> entry : dependentsByLocal.entrySet()){
            ret += entry.getKey() + " <<" + entry.getValue() + ">> , ";
        }
        ret += "}";
        return ret;
    }
    
    
}
