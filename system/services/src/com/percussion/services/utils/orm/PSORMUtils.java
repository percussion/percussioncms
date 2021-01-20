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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.utils.orm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides various utility methods related to ORM processing.
 */
public class PSORMUtils
{
   /**
    * Private noop ctor to enforce static use of this class
    */
   private PSORMUtils()
   {

   }
   
   /**
    * Get the value of the version used by the ORM framework.  Currently this
    * looks for a <code>getVersion()</code> method and attempts to execute it 
    * and return the result.
    * 
    * @param object The object from which to obtain the version, may be
    * <code>null</code>.
    * 
    * @return The version, or <code>null</code> if one could not be obtained or
    * the supplied object is null.
    */
   public static Integer getVersion(Object object)
   {
      Integer version = null;
      
      if (object == null)
         return version;
      
      Method method = null;
      try
      {
         method = object.getClass().getMethod("getVersion", new Class[0]);
      }
      catch (NoSuchMethodException e)
      {
         // guess the object does not support a version, ignore
      }
      
      if (method != null)
      {
         try
         {
            Object result = method.invoke(object, new Object[0]);
            if (result instanceof Integer)
               version = (Integer)result; 
         }
         catch (InvocationTargetException e)
         {
            // should never happen, ignore
         }
         catch (IllegalAccessException e)
         {
            // should never happen, ignore
         }
      }
      
      return version;
   }
}

