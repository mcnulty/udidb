/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.source;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.udidb.engine.ops.results.TableRow;

/**
 * Class used to represent a SourceLine as a TableRow
 *
 * @author mcnulty
 */
public class SourceLineRow implements TableRow {

    private final int line;

    private final String text;

    /**
     * Constructor.
     *
     * @param line the line
     * @param text the text
     */
    public SourceLineRow(int line, String text) {
        this.line = line;
        this.text = text;
    }

    /**
     * Creates a SourceLineRow that represents invalid line information
     */
    public SourceLineRow() {
        this.line = -1;
        this.text = "Line information unavailable";
    }

    @Override
    public List<String> getColumnHeaders() {
        return Collections.emptyList();
    }

    @Override
    public List<String> getColumnValues() {
        return Arrays.asList(Integer.toString(line), text);
    }
}
