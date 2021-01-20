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
