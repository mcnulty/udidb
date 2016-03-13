/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.source;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.inject.Inject;

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.Range;
import net.sourcecrumbs.api.machinecode.MachineCodeMapping;
import net.sourcecrumbs.api.machinecode.SourceLineRange;
import net.sourcecrumbs.api.transunit.NoSuchLineException;
import net.sourcecrumbs.api.transunit.TranslationUnit;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.ops.MissingDebugInfoException;
import net.udidb.engine.ops.NoDebuggeeContextException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.parser.RangeParser;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.source.SourceLineRowFactory;

/**
 * An Operation to obtain lines of source from a specified file
 *
 * @author mcnulty
 */
@HelpMessage("Obtain lines of source from a file")
@DisplayName("source lines")
public class GetSourceLines extends DisplayNameOperation implements DebuggeeContextAware
{
    private final SourceLineRowFactory sourceLineRowFactory;
    private DebuggeeContext debuggeeContext;
    private MachineCodeMapping machineCodeMapping;

    @HelpMessage("the range of source lines in the file")
    @Operand(order = 0, operandParser = RangeParser.class, optional = true)
    private Range<Integer> range;

    @HelpMessage("the name of the source file as defined in the binary. " +
                 "If unspecified, the current translation unit is used, if available.")
    @Operand(order = 10, optional = true)
    private String file;

    @Inject
    GetSourceLines(SourceLineRowFactory sourceLineRowFactory) {
        this.sourceLineRowFactory = sourceLineRowFactory;
    }

    public Range<Integer> getRange()
    {
        return range;
    }

    public void setRange(Range<Integer> range)
    {
        this.range = range;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    private TranslationUnit getTranslationUnitFromFile() throws AmbiguousFileException
    {
        Path filePath = Paths.get(file);

        Iterable<TranslationUnit> translationUnits = debuggeeContext.getExecutable().getTranslationUnits();

        // Filter based on equality first
        Optional<TranslationUnit> candidate = StreamSupport.stream(translationUnits.spliterator(), false)
                                                           .filter(t -> t.getPath().equals(filePath))
                                                           .findAny();
        if (candidate.isPresent()) {
            return candidate.get();
        }

        if (filePath.isAbsolute()) {
            // An absolute path should have matched a path
            throw new AmbiguousFileException(file);
        }

        List<TranslationUnit> partialMatches = StreamSupport.stream(translationUnits.spliterator(), false)
                                                            .filter(t -> t.getPath().endsWith(filePath))
                                                            .collect(Collectors.toList());
        if (partialMatches.size() != 1) {
            throw new AmbiguousFileException(file);
        }

        return partialMatches.get(0);
    }

    @Override
    public Result execute() throws OperationException
    {
        if (debuggeeContext == null) {
            throw new NoDebuggeeContextException();
        }

        try {
            if (range != null) {
                TranslationUnit translationUnit;
                if (file != null) {
                    translationUnit = getTranslationUnitFromFile();
                } else {
                    // Determine the current translation unit
                    long currentPc = debuggeeContext.getCurrentThread().getPC();
                    translationUnit = debuggeeContext.getExecutable().getContainingTranslationUnit(currentPc);
                    if (translationUnit == null) {
                        throw new MissingDebugInfoException();
                    }
                }

                SourceLineRange sourceLineRange = new SourceLineRange();
                sourceLineRange.setTranslationUnit(translationUnit);
                sourceLineRange.setLineRange(range);

                return new TableResult(sourceLineRowFactory.create(sourceLineRange));
            }else{
                if (machineCodeMapping == null) {
                    throw new MissingDebugInfoException();
                }

                // Get the current line
                long currentPc = debuggeeContext.getCurrentThread().getPC();
                return new TableResult(sourceLineRowFactory.create(machineCodeMapping.getSourceLinesRanges(currentPc)));
            }
        }catch (UdiException | NoSuchLineException e) {
            throw new OperationException("Failed to obtain requested source lines", e);
        }
    }

    @Override
    public void setDebuggeeContext(DebuggeeContext debuggeeContext)
    {
        this.debuggeeContext = debuggeeContext;
        this.machineCodeMapping = debuggeeContext.getExecutable().getMachineCodeMapping();
    }
}
