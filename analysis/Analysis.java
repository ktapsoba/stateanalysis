package analysis;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class Analysis {
	
	public static void main(String[] args){
		(new Analysis()).process(args);
	}

	private void process(String[] args){
		PackManager.v().getPack("jtp")
		.add(new Transform("jtp.myTransform", new BodyTransformer() {

			protected void internalTransform(Body body, String phase, Map options) {
				UnitGraph unitGraph = new ExceptionalUnitGraph(body);
				//new ConstantPropagation(unitGraph);
				new CPAnalysis(unitGraph);
			}

		}));
		soot.Main.main(args);
	}
}
