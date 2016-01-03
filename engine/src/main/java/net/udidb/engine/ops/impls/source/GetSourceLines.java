/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.source;

import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;

/**
 * An Operation to obtain lines of source from a specified file
 *
 * @author mcnulty
 */
@HelpMessage(enMessage="Obtain lines of source from a file")
@LongHelpMessage(enMessage=
        "source lines <file> [source-range]\n\n" +
        "Obtain lines of source from a file"
)
@DisplayName("source lines")
public class GetSourceLines
{

}
