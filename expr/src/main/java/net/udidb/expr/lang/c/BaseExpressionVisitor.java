/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import net.udidb.expr.grammar.c.CBaseVisitor;

/**
 * Base expression visitor
 *
 * @author mcnulty
 */
public abstract class BaseExpressionVisitor<T> extends CBaseVisitor<T>
{
    private final ParseTreeProperty<NodeState> states;

    protected BaseExpressionVisitor(ParseTreeProperty<NodeState> states)
    {
        this.states = states;
    }

    protected NodeState getNodeState(ParseTree node)
    {
        NodeState nodeState = states.get(node);
        if (nodeState == null) {
            nodeState = new NodeState();
            states.put(node, nodeState);
        }

        return nodeState;
    }

    protected void setNodeState(ParseTree node, NodeState state)
    {
        states.put(node, state);
    }
}
