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

package net.udidb.cli.ops.impls.internals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Inject;

import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.impls.Setting;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.TableRow;

/**
 * Shows all the values for the internals settings
 *
 * @author mcnulty
 */
@HelpMessage(enMessage = "Display all values of internals settings")
@LongHelpMessage(enMessage =
        "internals show\n\n" +
        "Display all values of internal settings"
)
@DisplayName("internals show")
public class ShowInternals extends DisplayNameOperation {

    private final List<Setting> settings;

    @Inject
    public ShowInternals(List<Setting> internalsSettings) {
        this.settings = internalsSettings;
    }

    @Override
    public Result execute() throws OperationException {
        List<TableRow> rows = new ArrayList<>();

        for (Setting setting : settings) {
            InternalSetting internalSetting = new InternalSetting();
            internalSetting.setting = setting.getName();
            internalSetting.value = setting.getSetting().toString();

            rows.add(internalSetting);
        }

        return new TableResult(rows);
    }

    private static class InternalSetting implements TableRow {

        public String setting;

        public String value;

        @Override
        public List<String> getColumnHeaders() {
           return Arrays.asList(
                   "Setting",
                   "Value"
           );
        }

        @Override
        public List<String> getColumnValues() {
            return Arrays.asList(setting, value);
        }
    }
}
