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

import net.libudi.api.UdiProcess;
import net.sourcecrumbs.api.files.Executable;

/**
 * Factory for DebuggeeContext
 *
 * @author mcnulty
 */
public interface DebuggeeContextFactory {

    /**
     * Creates a DebuggeeContext
     *
     * @param execPath the path to the executable
     * @param args the arguments
     * @param executable the executable
     *
     * @return the DebuggeeContext
     */
    DebuggeeContext createContext(Path execPath, String[] args, Executable executable);

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
     */
    void deleteContext(UdiProcess process);
}
