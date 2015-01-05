package net.udidb.expr;

import net.udidb.engine.context.DebuggeeContext;

/**
 * Handle to an expression returned by an ExpressionEvaluator
 *
 * @author dmcnulty
 */
public interface Expression
{
    /**
     * @return the value of the expression, if known or null if not known and execution is required
     */
    String getValue();

    /**
     * Loads the expression into the debuggee for execution
     *
     * @param debuggeeContext the debuggee context
     *
     * @throws ExpressionException on failure to load the expression into the debuggee
     */
    void loadExpression(DebuggeeContext debuggeeContext) throws ExpressionException;

    /**
     * Determines whether the expression has completed execution
     *
     * @param debuggeeContext the debuggee
     * @return true if the expression has completed execution
     *
     * @throws ExpressionException on failure to interrogate the debuggee
     */
    boolean isExpressionCompleted(DebuggeeContext debuggeeContext) throws ExpressionException;
}
