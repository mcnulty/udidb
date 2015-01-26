/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.udidb.engine.Config;

/**
 * Builder used to construct a configuration for udidb from command line arguments
 *
 * @author mcnulty
 */
public class ConfigBuilder {

    private final Option helpOption;

    private final Options options;

    /**
     * Constructor.
     */
    public ConfigBuilder() {
        helpOption = new Option("h", "help", false, "Display this help message");

        options = new Options();
        options.addOption(helpOption);
    }

    /**
     * Builds the configuration for udidb from the command line parameters
     *
     * @param args the command line arguments
     *
     * @return the configuration
     *
     * @throws ParseException if the configuration cannot be created due to invalid parameters
     * @throws HelpMessageRequested when the user requests the help message
     */
    public Config build(String[] args) throws ParseException, HelpMessageRequested {

        CommandLineParser parser = new BasicParser();

        CommandLine commandLine = parser.parse(options, args);

        if ( commandLine.hasOption(helpOption.getOpt()) ) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("udidb", options, true);
            throw new HelpMessageRequested();
        }

        // TODO convert options to config

        return new CommandLineConfigImpl();
    }
}
