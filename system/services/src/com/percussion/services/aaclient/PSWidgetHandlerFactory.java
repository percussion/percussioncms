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
package com.percussion.services.aaclient;


/**
 * todo
 */
public class PSWidgetHandlerFactory
{
   static public IPSWidgetHandler getHandler(String widgetName)
   {
      if (widgetName == null || widgetName.length() == 0)
      {
         throw new IllegalArgumentException(
            "widgetName must not be null or empty");
      }
      widgetName = widgetName.toLowerCase();
      if (widgetName.equals(WIDGET_PAGETREE))
      {
         return new PSPageTree();
      }
      else if (widgetName.equals(WIDGET_ACTIONBAR))
      {
         return new PSActionBar();
      }
      else if (widgetName.equals(WIDGET_ACTIONEXECUTOR))
      {
         return new PSActionExecutor();
      }
      else if (widgetName.equals(WIDGET_HASHEDFILE))
      {
         return new PSHashedFileWidgetHandler();
      }
      throw new IllegalArgumentException(
         "No handler is available for widget named '" + widgetName + "'.");
   }

   /*
    * Unique names of the widgets known to the system
    */

   /**
    * Active Assembly Page Tree
    */
   static public final String WIDGET_PAGETREE = "pt";

   /**
    * Action Bar for the Active Assembly Page
    */
   static public final String WIDGET_ACTIONBAR = "ab";

   /**
    * Action Executor
    */
   static public final String WIDGET_ACTIONEXECUTOR = "ae";

   static public final String WIDGET_HASHEDFILE = "hf";
}
