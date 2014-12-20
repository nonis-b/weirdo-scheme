package net.tapire_solutions.weirdoscheme;
import java.util.ArrayList;


public class ExpressionParser {
	
	public static EvalItem read(String program) throws LispParserException {
		ArrayList<String> tokens = tokenize(program);
		EvalItem parsed = readFrom(tokens);
		return parsed;
	}
	
	private static EvalItem readFrom(ArrayList<String> tokens) throws LispParserException {
		if (tokens.isEmpty()) {
			throw new LispParserException("Empty expression!");
		}
		String token = tokens.remove(0);
		if (token.equals("(")) {
			ArrayList<EvalItem> evalItems = new ArrayList<EvalItem>();
			while (!tokens.get(0).equals(")")) {
				evalItems.add(readFrom(tokens));
			}
			tokens.remove(0);
			return new EvalItem(evalItems);
		}
		if (token.equals(")")) {
			throw new LispParserException("Unexpected ) !");
		} 
		return atom(token);
	}
	
	private static EvalItem symbol(String token) {
		return new EvalItem(token);
	}
	
	private static EvalItem atom(String token) {
		try {
			Integer.parseInt(token);
			return new EvalItem(token);
		} catch (NumberFormatException e1) {
			try {
				Float.parseFloat(token);
				return new EvalItem(token);
			} catch (NumberFormatException e2) {
				return symbol(token);
			}
		}
		
	}
	
	public static ArrayList<String> tokenize(String program) {
		program = (program.replaceAll("\\(", " ( ")).replaceAll("\\)", " ) ");
		String[] tokensRaw = program.split(" ");
		ArrayList<String> tokens = new ArrayList<String>();
		for (String token : tokensRaw) {
			if (!token.isEmpty()) {
				token = token.trim();
				tokens.add(token);
			}
		}
		return tokens;
	}
}