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

