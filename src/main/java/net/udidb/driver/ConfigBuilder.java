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

package net.udidb.driver;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.udidb.cli.Config;

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
