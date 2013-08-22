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

package net.udidb.engine.ops.impls.help;

import com.google.inject.Inject;

import net.udidb.engine.ops.DisplayNameOperation;
import net.udidb.engine.ops.InvalidOperandException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;

/**
 * Display help messages for operations
 *
 * @author mcnulty
 */
@HelpMessage(enMessage = "Display help for operations")
@LongHelpMessage(enMessage =
        "help [operation name]\n\n" +
        "Display detailed help for a specific operation or short help for all operations"
)
@DisplayName(name = "help")
public class Help extends DisplayNameOperation {

    private final HelpMessageProvider provider;

    @Operand(order=0, optional=true)
    private String opName;

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    @Inject
    Help(HelpMessageProvider provider) {
        this.provider = provider;
    }

    @Override
    public Result execute() throws OperationException {
        if (opName == null) {
            StringBuilder builder = new StringBuilder();
            provider.getAllShortMessages(builder);

            return new ValueResult<>(builder.toString());
        }

        String longMessage = provider.getLongMessage(opName);
        if (longMessage == null) {
            throw new InvalidOperandException(getName(), "opName");
        }

        return new ValueResult<>(longMessage);
    }
}
