/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
