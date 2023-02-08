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
package com.percussion.error;

import com.percussion.log.PSLogError;
import com.percussion.util.PSBijectionMap;

import java.util.Enumeration;

/**
 *   A static one-to-one mapping from error classes to human readable names
 *   and vice-versa.
 */
public class PSErrorHumanReadableNames
{
   /**
    *   Get the human readable name for an error class, given the name
    */
   public static String getHumanReadableName(String errorClassName)
      throws ClassNotFoundException
   {
      lazyInit();
      return (String)ms_errorNames.getValue(Class.forName(errorClassName));
   }

   /**
    *   Get the human readable name for an error class
    */
   public static String getHumanReadableName(Class errorClass)
   {
      lazyInit();
      return (String)ms_errorNames.getValue(errorClass);
   }

   /**
    *   Get the human readable name for this error instance's class
    */
   public static String getHumanReadableName(PSLogError error)
   {
      lazyInit();
      return (String)ms_errorNames.getValue(error.getClass());
   }

   /**
    *   Given a human readable name, get the error class associated
    *   with it
    */
   public static Class getErrorClass(String humanReadableName)
   {
      lazyInit();
      return (Class)ms_errorNames.getKey(humanReadableName);
   }

   /**
    *   Get an Enumeration of all the human readable names
    */
   public static Enumeration getHumanReadableNames()
   {
      lazyInit();
      return ms_errorNames.values();
   }

   /**
    *   Set up the mapping if it isn't already set up
    */
   protected static void lazyInit()
   {
      /* To improve performance, we don't make the lazyInit() method
       * synchronized. This means that we have to be careful about
       * checking for ms_errorNames == null...for example, if we
       * just did:
       *
       * 1:   if (ms_errorNames == null)
       * 2:      ms_errorNames = new PSBijectionMap(20);
       * 3:
       *
       * we would have a reentrancy problem. Let's say that Thread A
       * enters the function, executes line 1 and sees that ms_errorNames
       * is null. Then Thread A gets put to sleep before it executes
       * line 2. Now Thread B comes in and executes line 1 -- ms_errorNames
       * is still null, so it goes and executes line 2. Then Thread A
       * wakes up and it also executes line 2. So we get two maps being
       * instantiated.
       *
       * We solve this problem by using double entry, where we check for
       * instance variable == null. Then, only if it's null, we synchronize
       * on this, check again for null, then instantiate it if necessary.
       * This lets us avoid the cost of acquiring an object lock after
       * we've already created the instance.
       */
      if (ms_errorNames != null)
         return;

      synchronized (ms_syncObj)
      {   
         // we must check again, because someone might have gone
         // through this synchronized section after our first
         // check for null
         if (ms_errorNames == null)
            ms_errorNames = new PSBijectionMap(20);
      }

      ms_errorNames.put(
         com.percussion.error.PSApplicationAuthorizationError.class,
         "Application authorization");
      ms_errorNames.put(
         com.percussion.error.PSApplicationDesignError.class,
         "Application design");
      ms_errorNames.put(
         com.percussion.error.PSBackEndAuthorizationError.class,
         "Back end authorization");
      ms_errorNames.put(
         com.percussion.error.PSBackEndQueryProcessingError.class,
         "Back end query");
      ms_errorNames.put(
         com.percussion.error.PSBackEndServerDownError.class,
         "Back end server down");
      ms_errorNames.put(
         com.percussion.error.PSBackEndUpdateProcessingError.class,
         "Back end update processing");
      ms_errorNames.put(
         com.percussion.error.PSDataConversionError.class,
         "Data conversion");
      ms_errorNames.put(
         com.percussion.error.PSFatalError.class,
         "Fatal");
      ms_errorNames.put(
         com.percussion.error.PSHtmlProcessingError.class,
         "HTML processing");
      ms_errorNames.put(
         com.percussion.error.PSLargeApplicationRequestQueueError.class,
         "Large application request queue");
      ms_errorNames.put(
         com.percussion.error.PSLargeBackEndRequestQueueError.class,
         "Large back end request queue");
      ms_errorNames.put(
         com.percussion.error.PSRequestHandlerNotFoundError.class,
         "Request handler not found");
      ms_errorNames.put(
         com.percussion.error.PSRequestPreProcessingError.class,
         "Request preprocessing");
      ms_errorNames.put(
         com.percussion.error.PSValidationError.class,
         "Validation");
      ms_errorNames.put(
         com.percussion.error.PSXmlProcessingError.class,
         "XML processing");
   }
   
   private static PSBijectionMap ms_errorNames;
   private static Object ms_syncObj = new Object();
}
