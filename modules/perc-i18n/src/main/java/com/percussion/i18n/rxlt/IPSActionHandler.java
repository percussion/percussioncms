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
package com.percussion.i18n.rxlt;

import org.w3c.dom.Element;

/**
 * This interface is implemented by every action handler. User chooses one or
 * more actions to execute in the UI. Every action will have an entry in the
 * configuration file to hold the option parameters to process the action. The
 * actionid in this interface will match with that in the config file.
 */
public interface IPSActionHandler
{
   /**
    * This method actually processes the action.
    * @param cfgdata DOM element with configuration data for the current action.
    * Must not be <code>null</code>
    * @throws PSActionProcessingException if any error occurs during processing
    */
   void process(Element cfgdata)
      throws PSActionProcessingException;

   /**
    * Action id to generate the TMX resourecs bundle from Rhythmyx Content
    * Manager. This bundle typically goes to translation services for translation
    * to other language(s).
    */
   public static final int ACTIONID_GENERATE_TMX_RESOURCES = 1;
   /**
    * Action id to merge the translated resource bundle with the master TMX
    * resource bundle of Rhythmyx.
    */
   public static final int ACTIONID_MERGE_MASTER = 2;
   /**
    * Action id to exit the program.
    */
   public static final int ACTIONID_EXIT = 3;
   /**
    * First action id
    */
   public static final int ACTIONID_FIRST = ACTIONID_GENERATE_TMX_RESOURCES;
   /**
    * Last action id
    */
   public static final int ACTIONID_LAST = ACTIONID_EXIT;
}
