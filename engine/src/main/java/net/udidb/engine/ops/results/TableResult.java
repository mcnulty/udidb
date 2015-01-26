/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.results;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.udidb.engine.ops.Operation;

/**
 * A result that represents a table of values
 *
 * @author mcnulty
 */
public class TableResult extends BaseResult {

    private final List<String> columnHeaders;

    private final List<TableRow> rows;

    /**
     * Constructor.
     *
     * @param rows the rows to be include in this result
     */
    public TableResult(List<? extends TableRow> rows) {
        this.rows = new ArrayList<>(rows);
        if (this.rows.size() > 0) {
            columnHeaders = new ArrayList<>(this.rows.get(0).getColumnHeaders());
        }else{
            columnHeaders = new ArrayList<>(0);
        }
    }

    /**
     * Convenience constructor for a table with only one row.
     *
     * @param row the row
     */
    public TableResult(TableRow row) {
        this(Arrays.asList(row));
    }

    public int getNumColumns() {
        if (columnHeaders.size() == 0) {
            if (this.rows.size() > 0) {
                return this.rows.get(0).getColumnValues().size();
            }
            return 0;
        }else{
            return columnHeaders.size();
        }
    }

    public List<String> getColumnHeaders() {
        return new ArrayList<>(columnHeaders);
    }

    public List<TableRow> getRows() {
        return new ArrayList<>(rows);
    }

    @Override
    public boolean accept(Operation op, OperationResultVisitor visitor) {
        return visitor.visit(op, this);
    }
}
