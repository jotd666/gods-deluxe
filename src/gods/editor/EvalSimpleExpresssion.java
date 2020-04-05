package gods.editor;
import de.congrace.exp4j.PostfixExpression;

public class EvalSimpleExpresssion {

	@SuppressWarnings("deprecation")
	static public int evaluate(String e) throws Exception
	{
		int rval = (int)(PostfixExpression.fromInfix(e).calculate());
		return rval;
	}
}
