/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import java.util.LinkedList;
import java.util.List;

/**
 * Generic container for models
 *
 * @author mcnulty
 */
public final class ModelContainer<T>
{
    private List<T> elements = new LinkedList<>();

    public List<T> getElements()
    {
        return elements;
    }

    public void setElements(List<T> elements)
    {
        this.elements = elements;
    }
}
