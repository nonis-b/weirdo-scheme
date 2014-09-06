import java.util.ArrayList;
import java.util.List;


public class Evaluator {
	
	public static final int maxRecursion = 100;
	
	public static final boolean verbose = true;
	
	public static EvalItem evaluate(EvalItem evalItem, Environment env) throws LispEvaluatorException {
		
		int loops = 0;
		while (true) {
			
			loops++;
			if (loops > maxRecursion) {
				throw new LispEvaluatorException("Max recursion reached (" + loops + ")");
			}
			
			if (evalItem.isValue()) {
				Environment envWithVar = env.findEnvironmentWithVar(evalItem.getValue());
				if (envWithVar == null) {
					return evalItem;
				} else {
					return envWithVar.findValueOfVar(evalItem.getValue());
				}
			}
			
			if (evalItem.isList()) {
				EvalItem first = evalItem.getList().get(0);
				if (first.isValue()) {
					String keyword = first.getValue();
					List<EvalItem> list = evalItem.getList();
					
					if (keyword.equals("quote")) {
						return new EvalItem(list.subList(1, list.size()));
					}
	
					if (keyword.equals("if")) {
						EvalItem test = list.get(1);
						EvalItem conseq = list.get(2);
						EvalItem alt = list.get(3);
						EvalItem testResult = evaluate(test, env);
						if ((testResult.isList() && testResult.getList().size() > 0)
								|| testResult.isValue() && testResult.getValue().equals("true")) {
							return evaluate(conseq, env);
						} else {
							return evaluate(alt, env);
						}
					}
	
					if (keyword.equals("set!")) {
						String var = list.get(1).getValue();
						Environment envWithVar = env.findEnvironmentWithVar(var);
						EvalItem itemToAdd;
						if (list.size() > 1) {
							itemToAdd = evaluate(new EvalItem(list.subList(1, list.size())), env);
						} else {
							itemToAdd = evaluate(list.get(1), env);
						}
						if (envWithVar != null) {
							envWithVar.add(var, itemToAdd);
						}
						return new EvalItem(new ArrayList<EvalItem>());
					}
	
					if (keyword.equals("define")) {
						String var = list.get(1).getValue();
						EvalItem itemToAdd;
						if (list.size() > 1) {
							itemToAdd = evaluate(new EvalItem(list.subList(1, list.size())), env);
						} else {
							itemToAdd = evaluate(list.get(2), env);
						}
						env.add(var, itemToAdd);
						return new EvalItem(new ArrayList<EvalItem>());
					}
					
					if (keyword.equals("lambda")) {
						EvalItem params = list.get(1);
						EvalItem body = list.get(2);
						return new EvalItem(params, body, env);
					}
					
					if (keyword.equals("begin")) {
						EvalItem ret = null;
						for (EvalItem item : list) {
							ret = evaluate(item, env);
						}
						return ret;
					}
					
					// keyword treated as function name.
					EvalItem proc = evaluate(first, env);
					ArrayList<EvalItem> args = new ArrayList<>();
					for (EvalItem i : list) {
						args.add(evaluate(i, env));
					}
					if (proc.isLambda()) {
						evalItem = proc.getBody();
						env = new Environment(env);
						for (int i = 0; i < proc.getParams().getList().size(); i++) {
							env.add(proc.getParams().getList().get(i).getValue(), args.get(i));
						}
						if (verbose) {
							System.out.println("Calling lambda " + first.getValue() + " with env=" + env);
						}
					} else {
						if (verbose) {
							System.out.println("Calling builtin " + proc.getValue() + " with args=" + args);
						}
						EvalItem returnVal = Builtins.callBuiltin(proc.getValue(), args, env);
						if (returnVal == null) {
							throw new LispEvaluatorException("Undefined procedure " + proc.getValue());
						} else {
							return returnVal;
						}
					}
				}
			}
		}
	}
}
