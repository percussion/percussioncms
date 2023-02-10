/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.wrapper;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static com.percussion.wrapper.JettyStartUtils.error;

public class StartArgsProxy {


    private Object instance;

    public StartArgsProxy(Object startArgsInstance) {
        instance = startArgsInstance;
    }

    public boolean isRun(){
        try {
        Method isRunMethod = instance.getClass().getMethod("isRun");
            return (Boolean)isRunMethod.invoke(instance);
        } catch (IllegalAccessException e) {
            error("Error accessing jetty method",e);
        } catch (InvocationTargetException e) {
            error("Error accessing jetty method",e);
        } catch (NoSuchMethodException e) {
            error("Error accessing jetty method",e);
        }
        return false;
    }

    public List<String> getMainArgs(){
        try {
            Method getMainArgs = instance.getClass().getMethod("getMainArgs", boolean.class);
            Object cmdlineBuild = getMainArgs.invoke(instance,new Object[]{true});
            Method getArgsMethod = cmdlineBuild.getClass().getMethod("getArgs");
            return (List<String>)getArgsMethod.invoke(cmdlineBuild);
        } catch (IllegalAccessException e) {
            error("Error accessing jetty method",e);
        } catch (InvocationTargetException e) {
            error("Error accessing jetty method",e);
        } catch (NoSuchMethodException e) {
            error("Error accessing jetty method",e);
        }
        return null;
    }


    public Object getInstance() {
        return instance;
    }
}
