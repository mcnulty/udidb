/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.context;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.libudi.api.Register;
import net.libudi.api.UdiProcess;
import net.libudi.api.UdiProcessConfig;
import net.libudi.api.UdiThread;
import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.debug.symbols.ContextInspectionException;
import net.sourcecrumbs.api.debug.symbols.RegisterContext;
import net.udidb.engine.ops.results.TableRow;

/**
 * A bean class used to encapsulate all the configuration and state for a specific debuggee.
 *
 * @author mcnulty
 */
public class DebuggeeContext implements TableRow, RegisterContext
{

    private Path rootDir;

    private Map<String, String> env;

    private Path execPath;

    private String[] args;

    private UdiProcess process;

    private UdiThread currentThread;

    private boolean active;

    private Executable executable;

    public Path getRootDir() {
        return rootDir;
    }

    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public Path getExecPath() {
        return execPath;
    }

    public void setExecPath(Path execPath) {
        this.execPath = execPath;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public UdiProcess getProcess() {
        return process;
    }

    public void setProcess(UdiProcess process) {
        this.process = process;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UdiThread getCurrentThread() {
        return currentThread;
    }

    public void setCurrentThread(UdiThread currentThread) {
        this.currentThread = currentThread;
    }

    public Executable getExecutable() {
        return executable;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    public UdiProcessConfig createProcessConfig() {
        UdiProcessConfig config = new UdiProcessConfig();
        config.setRootDir(rootDir);

        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DebuggeeContext that = (DebuggeeContext) o;

        if (process != null ? !process.equals(that.process) : that.process != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return process != null ? process.hashCode() : 0;
    }

    @Override
    public List<String> getColumnHeaders() {
        return Arrays.asList(
                "Active",
                "PID",
                "Executable",
                "Arguments"
        );
    }

    @Override
    public List<String> getColumnValues() {
        return Arrays.asList(
                (active ? "*" : ""),
                Integer.toString(process.getPid()),
                execPath.toString(),
                (args != null ? StringUtils.join(args, ' ') : "")
        );
    }

    @Override
    public BigInteger getRegisterValue(Register register) throws ContextInspectionException
    {
        try {
            return BigInteger.valueOf(currentThread.readRegister(register));
        }catch (UdiException e) {
            throw new ContextInspectionException(e);
        }
    }
}
