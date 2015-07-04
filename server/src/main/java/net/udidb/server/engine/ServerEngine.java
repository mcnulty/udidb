/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.List;

import net.udidb.engine.ops.OperationException;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.OperationDescriptionModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.ThreadModel;

/**
 * @author mcnulty
 */
public interface ServerEngine
{

    List<DebuggeeContextModel> getDebuggeeContexts() throws OperationException;

    DebuggeeContextModel getDebuggeeContext(String id) throws OperationException;

    DebuggeeContextModel createDebuggeeContext(DebuggeeConfigModel config) throws OperationException;

    ProcessModel getProcess(String id) throws OperationException;

    List<ThreadModel> getThreads(String id) throws OperationException;

    ThreadModel getThread(String id, String threadId) throws OperationException;

    OperationModel createOperation(String id, OperationModel operation) throws OperationException;

    OperationModel getOperation(String id) throws OperationException;

    List<OperationDescriptionModel> getOperationDescriptions(String id) throws OperationException;
}
