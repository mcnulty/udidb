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

package net.udidb.engine.ops.results;

import java.util.ArrayList;
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
