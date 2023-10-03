package gods.editor;


import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class EvalSimpleExpresssion {

	public static int evaluate(String e) throws Exception
	{
		Expression expression = new ExpressionBuilder(e).build();
		return (int)expression.evaluate();
	}
}
