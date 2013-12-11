/*
 * Copyright (c) 2011-2013, Dan McNulty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the UDI project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package net.udidb.cli.driver;

import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.udidb.engine.Config;
import net.udidb.engine.UdidbEngine;
import net.udidb.engine.ops.OperationReader;
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

        UdidbEngine engine = new UdidbEngine(config,
                injector.getInstance(OperationReader.class),
                injector.getInstance(OperationResultVisitor.class));

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
