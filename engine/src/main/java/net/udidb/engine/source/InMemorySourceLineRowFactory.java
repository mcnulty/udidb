/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.source;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourcecrumbs.api.machinecode.SourceLine;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.sourcecrumbs.api.transunit.TranslationUnit;

/**
 * In-memory implementation of SourceLineRowFactory
 *
 * <p>
 *     Note: this implementation is currently very naive and inefficient w.r.t memory because it caches the whole
 *     contents of files in memory.
 * </p>
 *
 * @author mcnulty
 */
public class InMemorySourceLineRowFactory implements SourceLineRowFactory {

    private final Map<String, List<String>> lineCache = new HashMap<>();

    private List<String> getLines(TranslationUnit unit) throws NoSuchLineException
    {
        String pathKey = unit.getPath().toAbsolutePath().toString();

        try {
            List<String> lines;
            synchronized (lineCache) {
                lines = lineCache.get(pathKey);
                if (lines == null) {
                    lines = Files.readAllLines(unit.getPath(), Charset.defaultCharset());
                    lineCache.put(pathKey, lines);
                }
            }

            if (lines == null) {
                throw new NoSuchLineException("Cannot retrieve source lines for translation unit " +
                        unit.getName());
            }
            return lines;
        }catch (IOException e) {
            throw new NoSuchLineException(e);
        }
    }

    private SourceLineRow create(TranslationUnit unit, int line) throws NoSuchLineException {
        List<String> lines = getLines(unit);

        if (line > lines.size()-1) {
            throw new NoSuchLineException(String.format("Cannot retrieve source for line %s:%d", unit.getName(), line));
        }

        return new SourceLineRow(line, lines.get(line-1));
    }

    private List<SourceLineRow> create(TranslationUnit unit) throws NoSuchLineException {
        List<String> lines = getLines(unit);

        List<SourceLineRow> sourceLineRows = new LinkedList<>();
        for (int i = 0; i < lines.size(); ++i) {
            sourceLineRows.add(new SourceLineRow(i+1, lines.get(i)));
        }
        return sourceLineRows;
    }

    @Override
    public SourceLineRow create(SourceLine sourceLine) throws NoSuchLineException {
        return create(sourceLine.getTranslationUnit(), sourceLine.getLine());
    }

    @Override
    public List<SourceLineRow> create(List<SourceLineRange> ranges) throws NoSuchLineException {
        List<SourceLineRow> rows = new LinkedList<>();
        for (SourceLineRange range : ranges) {
            create(range, rows);
        }
        return rows;
    }

    private void create(SourceLineRange range, List<SourceLineRow> output) throws NoSuchLineException {
        // Handle special range for retrieving all lines
        if (range.getLineRange().getStart() == 0 && range.getLineRange().getEnd() == 0) {
            output.addAll(create(range.getTranslationUnit()));
        }else{
            for (int i = range.getLineRange().getStart(); i <= range.getLineRange().getEnd(); ++i) {
                output.add(create(range.getTranslationUnit(), i));
            }
        }
    }

    @Override
    public List<SourceLineRow> create(SourceLineRange range) throws NoSuchLineException {
        List<SourceLineRow> rows = new LinkedList<>();
        create(range, rows);
        return rows;
    }
}
