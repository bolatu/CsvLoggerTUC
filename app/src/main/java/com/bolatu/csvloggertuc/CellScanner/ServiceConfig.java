/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.bolatu.csvloggertuc.CellScanner;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class ServiceConfig extends HashMap<Class<?>, Object> {

    private static final long serialVersionUID = 1111111111L;

    public static Object load(String className) {

        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading [" + className + "] class");
        }
        Constructor[] constructors = c.getConstructors();

        Constructor<?> myConstructor = null;
        for (Constructor<?> construct : constructors) {
            if (construct.getParameterTypes().length == 0) {
                myConstructor = construct;
                break;
            }
        }

        if (myConstructor == null) {
            throw new RuntimeException("No constructor found");
        }

        try {
            return myConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
}
