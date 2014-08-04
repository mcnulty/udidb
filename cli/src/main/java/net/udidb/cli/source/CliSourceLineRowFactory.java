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

package net.udidb.cli.source;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourcecrumbs.api.machinecode.SourceLine;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.sourcecrumbs.api.transunit.TranslationUnit;
import net.udidb.engine.source.SourceLineRow;
import net.udidb.engine.source.SourceLineRowFactory;

/**
 * CLI implementation of SourceLineRowFactory
 *
 * <p>
 *     Note: this implementation is currently very naive and inefficient w.r.t memory because it caches the whole
 *     contents of files in memory.
 * </p>
 *
 * @author mcnulty
 */
public class CliSourceLineRowFactory implements SourceLineRowFactory {

    private final Map<String, List<String>> lineCache = new HashMap<>();

    private SourceLineRow create(TranslationUnit unit, int line) throws NoSuchLineException {
        try {
            String pathKey = unit.getPath().toAbsolutePath().toString();

            List<String> lines;
            synchronized (lineCache) {
                lines = lineCache.get(pathKey);
                if (lines == null) {
                    lines = Files.readAllLines(unit.getPath(), Charset.defaultCharset());
                    lineCache.put(pathKey, lines);
                }
            }

            if (lines == null || line > lines.size()-1) {
                throw new NoSuchLineException(String.format("Cannot retrieve source for line %s:%d", unit.getName(), line));
            }

            return new SourceLineRow(line, lines.get(line-1));
        }catch (IOException e) {
            throw new NoSuchLineException(e);
        }
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
        for (int i = range.getLineRange().getStart(); i <= range.getLineRange().getEnd(); ++i) {
            output.add(create(range.getTranslationUnit(), i));
        }
    }

    @Override
    public List<SourceLineRow> create(SourceLineRange range) throws NoSuchLineException {
        List<SourceLineRow> rows = new LinkedList<>();
        create(range, rows);
        return rows;
    }
}
