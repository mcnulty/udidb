/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.grammar.c.test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import net.udidb.expr.grammar.c.CLexer;
import net.udidb.expr.grammar.c.CParser;

/**
 * @author mcnulty
 */
public class DisplayParseTree {

    public static void main(String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();

        Iterator<String> iter = Arrays.asList(args).iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(" ");
            }
        }

        ANTLRInputStream inputStream = new ANTLRInputStream(new ByteArrayInputStream(builder.toString().getBytes()));
        CLexer lexer = new CLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        ParserRuleContext tree = parser.expression();
        tree.inspect(parser);
    }
}
