/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.source;

import java.util.List;

import net.sourcecrumbs.api.machinecode.SourceLine;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;

/**
 * A factory for creating SourceLineRows
 *
 * @author mcnulty
 */
public interface SourceLineRowFactory {

    /**
     * Creates a SourceLineRow from a SourceLine
     *
     * @param sourceLine the SourceLine
     *
     * @return the SourceLineRow
     *
     * @throws NoSuchLineException when no line information can be retrieved
     */
    SourceLineRow create(SourceLine sourceLine) throws NoSuchLineException;

    /**
     * Create a collection of SourceLineRows from a collection of SourceLineRanges
     *
     * @param ranges the ranges
     *
     * @return the rows
     *
     * @throws NoSuchLineException when no line information can be retrieved
     */
    List<SourceLineRow> create(List<SourceLineRange> ranges) throws NoSuchLineException;

    /**
     * Create a collection of SourceLineRows from a SourceLineRange
     *
     * @param range the range
     *
     * @return the rows
     *
     * @throws NoSuchLineException when no line information can be retrieved
     */
    List<SourceLineRow> create(SourceLineRange range) throws NoSuchLineException;
}
