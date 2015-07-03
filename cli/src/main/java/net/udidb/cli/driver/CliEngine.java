/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import net.udidb.engine.Config;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.ops.Operation;
import net.udidb.cli.ops.OperationReader;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.Result;

/**
 * CLI engine for udidb
 *
 * @author mcnulty
 */
public class CliEngine
{
    private final Config config;

    private final OperationReader reader;

    private final OperationResultVisitor visitor;

    private final EventDispatcher eventDispatcher;

    /**
     * Constructor.
     *
     * @param config the configuration
     * @param reader the reader
     * @param visitor the visitor
     * @param eventDispatcher the event dispatcher for handling events
     */
    public CliEngine(Config config, OperationReader reader, OperationResultVisitor visitor, EventDispatcher eventDispatcher) {
        this.config = config;
        this.reader = reader;
        this.visitor = visitor;
        this.eventDispatcher = eventDispatcher;
    }

    /**
     * Runs the engine, which executes operations and processes the results
     */
    public void run() {
        boolean shouldContinue = true;
        while (shouldContinue) {
            Operation op = null;
            try {
                op = reader.read();

                Result result = op.execute();

                shouldContinue = result.accept(op, visitor);
                if (shouldContinue) {
                    eventDispatcher.handleEvents(result);
                }
            }catch (Exception e) {
               if(!visitor.visit(op, e)) {
                   shouldContinue = false;
               }
            }
        }
    }
}
