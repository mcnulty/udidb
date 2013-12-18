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

package net.udidb.cli.ops;

import java.io.PrintStream;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventError;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.event.UdiEventThreadCreate;
import net.libudi.api.event.UdiEventVisitor;
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.impls.util.Quit;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.TableRow;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;

/**
 * The result processor for the debugger when run from command line.
 *
 * @author mcnulty
 */
@Singleton
public class CliResultVisitor implements OperationResultVisitor {

    private final PrintStream out;

    private boolean printStackTraces = false;

    @Inject
    CliResultVisitor(@Named("OUTPUT DESTINATION") PrintStream out) {
        this.out = out;
    }

    public boolean isPrintStackTraces() {
        return printStackTraces;
    }

    public void setPrintStackTraces(boolean printStackTraces) {
        this.printStackTraces = printStackTraces;
    }

    @Override
    public boolean visit(Operation op, VoidResult result) {
        if (op instanceof Quit) return false;

        return true;
    }

    @Override
    public boolean visit(Operation op, ValueResult result) {
        out.println(result);

        return true;
    }

    private void printException(Exception e) {
        printException("", e);
    }

    private void printException(String msg, Exception e) {
        if (!msg.isEmpty()) {
            out.print(msg);

            if (e.getMessage() != null) {
                out.print(": " + e.getMessage());
            }
        }else{
            if (e.getMessage() != null) {
                out.print(e.getMessage());
            }else{
                out.print("Failed to parse operation");
            }
        }

        out.println();

        if (printStackTraces) {
            e.printStackTrace(out);
        }
    }

    @Override
    public boolean visit(Operation op, TableResult result) {

        final int PADDING = 2;

        List<TableRow> rows = result.getRows();
        if (rows.size() > 0) {
            List<String> headers = result.getColumnHeaders();

            // Determine the maximum length string for every column for formatting purposes
            int[] widths = new int[headers.size()];
            for (TableRow row : rows) {
                List<String> values = row.getColumnValues();
                for (int i = 0; i < values.size(); ++i) {
                    String value = values.get(i);
                    if (value.length() > widths[i]) {
                        widths[i] = value.length();
                    }
                }
            }

            for (int i = 0; i < headers.size(); ++i) {
                String header = headers.get(i);
                if (header.length() > widths[i]) {
                    widths[i] = header.length();
                }
            }

            StringBuilder formatString = new StringBuilder();
            for (int i = 0; i < widths.length; ++i) {
                formatString.append("%-" + (widths[i] + PADDING) + "s");
                if (i < (widths.length-1)) {
                    formatString.append(" ");
                }
            }

            out.println(String.format(formatString.toString(), headers.toArray()));

            for (TableRow row : rows) {
               out.println(String.format(formatString.toString(), row.getColumnValues().toArray()));
            }
        }

        return true;
    }

    @Override
    public boolean visit(Operation op, DeferredResult result) {
        // DeferredResults are explicitly ignored for now
        return true;
    }

    @Override
    public boolean visit(Operation op, Exception e) {
        if (op == null) {
            printException(e);
        }else{
            printException("Failed to execute " + op.getName(), e);
        }

        return true;
    }
}
