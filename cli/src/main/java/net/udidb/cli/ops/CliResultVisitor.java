/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops;

import java.io.PrintStream;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.cli.ops.impls.util.Quit;
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

    private String getBaseMessage(Throwable e)
    {
        if (e.getCause() == null) {
            return e.getMessage();
        }

        return getBaseMessage(e.getCause());
    }

    private void printException(String msg, Exception e) {
        if (!msg.isEmpty()) {
            out.print(msg);

            if (e.getMessage() != null) {
                out.print(": " + e.getMessage());
            }
        }else{
            if (e.getMessage() != null) {
                out.print(getBaseMessage(e));
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

            // Determine the maximum length string for every column for formatting purposes
            int[] widths = new int[result.getNumColumns()];
            for (TableRow row : rows) {
                List<String> values = row.getColumnValues();
                for (int i = 0; i < values.size(); ++i) {
                    String value = values.get(i);
                    if (value.length() > widths[i]) {
                        widths[i] = value.length();
                    }
                }
            }

            List<String> headers = result.getColumnHeaders();
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

            if (headers.size() > 0) {
                out.println(String.format(formatString.toString(), headers.toArray()));
            }

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
