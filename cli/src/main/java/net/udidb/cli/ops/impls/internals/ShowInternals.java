/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
