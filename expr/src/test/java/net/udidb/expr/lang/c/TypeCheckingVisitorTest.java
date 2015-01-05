package net.udidb.expr.lang.c;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for CalculateTypesVisitor
 *
 * @author dmcnulty
 */
public class TypeCheckingVisitorTest
{
    private static TypeCheckingVisitor createVisitor() throws Exception
    {
        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        return new TypeCheckingVisitor(states);
    }

    @Test
    @Ignore
    public void exploratoryTest() throws Exception
    {
        TypeCheckingVisitor visitor = createVisitor();
        ParserRuleContext parseTree = CExpressionCompiler.createParseTree("1 + 2.0");
        parseTree.accept(visitor);

        NodeState rootState = visitor.getNodeState(parseTree);
        assertNotNull(rootState);
        assertEquals(Types.DOUBLE_NAME, rootState.getEffectiveType().getName());
    }
}
