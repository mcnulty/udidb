/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.udidb.engine.Config;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.cli.ops.OperationReader;
import net.udidb.engine.ops.results.OperationResultVisitor;

/**
 * Driver class for udidb launched with command line arguments
 *
 * @author mcnulty
 */
public class UdidbDriver {

    private final String[] cmdLineArgs;

    private final Injector injector = Guice.createInjector(new CliModule());

    /**
     * Constructor.
     *
     * @param cmdLineArgs command line arguments
     */
    public UdidbDriver(String[] cmdLineArgs) {
        this.cmdLineArgs = cmdLineArgs;
    }

    /**
     * @return the result from executing the debugger (true on success, false on failure)
     */
    public boolean execute() {
        Config config;
        try {
            ConfigBuilder builder = new ConfigBuilder();
            config = builder.build(cmdLineArgs);
        }catch(HelpMessageRequested e) {
            return true;
        }catch(ParseException e) {
            System.err.println("Invalid arguments: " + e);
            return false;
        }

        CliEngine engine = new CliEngine(config,
                injector.getInstance(OperationReader.class),
                injector.getInstance(OperationResultVisitor.class),
                injector.getInstance(EventDispatcher.class));

        // When this returns, the debugger should exit
        engine.run();

        return true;
    }

    private static final int EXIT_SUCCESS = 0;

    private static final int EXIT_FAILURE = 1;

    /**
     * Entry point for udidb launched from the command line
     *
     * @param args command line arguments (see help message)
     */
    public static void main(String[] args) {
        UdidbDriver driver = new UdidbDriver(args);

        System.exit(driver.execute() ? EXIT_SUCCESS : EXIT_FAILURE );
    }
}
