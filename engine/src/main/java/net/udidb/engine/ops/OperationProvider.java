/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import net.udidb.engine.ops.annotations.DisplayName;

/**
 * @author mcnulty
 */
public final class OperationProvider
{
    private final Map<String, Class<? extends Operation>> operations = new HashMap<>();

    @Inject
    OperationProvider(@Named("OP_PACKAGES") String[] opPackages) {
        addSupportedOperations(opPackages);
    }

    private void addSupportedOperations(String[] opPackages) {
        Set<URL> packages = new HashSet<>();
        for (String opPackage : opPackages) {
            packages.addAll(ClasspathHelper.forPackage(opPackage));
        }

        Reflections reflections = new Reflections(packages, new SubTypesScanner());
        for (Class<? extends Operation> opClass : reflections.getSubTypesOf(Operation.class)) {
            if (Modifier.isAbstract(opClass.getModifiers())) continue;

            DisplayName displayName = opClass.getAnnotation(DisplayName.class);
            if (displayName != null ) {
                operations.put(displayName.value(), opClass);
            }else{
                throw new RuntimeException(opClass.getSimpleName() + " is an invalid Operation.");
            }
        }
    }

    public Map<String, Class<? extends Operation>> getOperations() {

        return new HashMap<>(operations);
    }

}
