/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.context;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.libudi.api.UdiProcess;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.Executable;

/**
 * Factory for DebuggeeContext
 *
 * @author mcnulty
 */
public interface DebuggeeContextManager
{

    /**
     * Creates a DebuggeeContext, spawning the underlying process. The environment is determined by the implementation.
     *
     * @param execPath the path to the executable
     * @param args the arguments
     * @param executable the executable
     *
     * @return the DebuggeeContext
     *
     * @throws UdiException on failure to create and initialize the DebuggeeContext
     */
    DebuggeeContext createContext(Path execPath, String[] args, Executable executable) throws UdiException;

    /**
     * Creates a DebuggeeContext, spawning the underlying process
     *
     * @param execPath the path to the executable
     * @param args the arguments
     * @param env the environment
     * @param executable the executable
     *
     * @return the DebuggeeContext
     *
     * @throws UdiException on failure to create and initialize the DebuggeeContext
     */
    DebuggeeContext createContext(Path execPath, String[] args, Map<String, String> env, Executable executable) throws UdiException;

    /**
     * Deletes a DebuggeeContext
     *
     * @param context the context to delete
     */
    void deleteContext(DebuggeeContext context);

    /**
     * Deletes a DebuggeeContext, given its associated process
     *
     * @param process the process
     *
     * @return the deleted context or null if none could be found
     */
    DebuggeeContext deleteContext(UdiProcess process);

    /**
     * @return the contexts managed by this factory
     */
    Map<Integer, DebuggeeContext> getContexts();

    /**
     * @return a list of contexts that could possibly return events
     */
    List<DebuggeeContext> getEventContexts();

    /**
     * @param observer add an observer to DebuggeeContext operations
     */
    void addObserver(DebuggeeContextObserver observer);
}
