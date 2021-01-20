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
package com.percussion.webservices.ui;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.webservices.PSErrorException;

import java.util.List;

/**
 * This interface defines all ui related webservices.
 */
public interface IPSUiWs
{
   /**
    * Loads all menu actions for the supplied name in read-only mode.
    * 
    * @param name the name of the action to load, may be <code>null</code> or 
    *    empty, asterisk wildcards are accepted. All actions are loaded if 
    *    not supplied or empty.
    * @return a list with all loaded menu actions in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    * @throws PSErrorException if an error occurs while loading the actions.
    */
   public List<PSAction> loadActions(String name) throws PSErrorException;
   
   /**
    * Loads all display formats for the supplied name in read-only mode.
    * 
    * @param name the name of the display format to load, may be 
    *    <code>null</code> or empty, asterisk wildcards are accepted. If 
    *    not supplied or empty all display formats will be loaded.
    * @return a list with all loaded display formats in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    * @throws PSErrorException if an error occurs while loading the display 
    *    formats.
    */
   public List<PSDisplayFormat> loadDisplayFormats(String name)
         throws PSErrorException;
   
   /**
    * Loads all searches for the supplied name in read-only mode.
    * 
    * @param name the name of the search to load, may be <code>null</code> or 
    *    empty, asterisk wildcards are accepted. All searches will be loaded 
    *    if not supplied or empty.
    * @return a list with all loaded searches in read-only mode, never 
    *    <code>null</code>, may be empty, ordered in alpha order by name.
    * @throws PSErrorException if an error occurs while loading the searches.
    */
   public List<PSSearch> loadSearches(String name)  throws PSErrorException;
   
   /**
    * Loads all views for the supplied name in read-only mode.
    * 
    * @param name the name of the view to load, may be <code>null</code> or 
    *    empty, asterisk wildcards are accepted. All views will be loaded if not 
    *    supplied or empty.
    * @return a list with all loaded view definitions in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    * @throws PSErrorException if an error occurs while loading the views.
    */
   public List<PSSearch> loadViews(String name)   throws PSErrorException;
}

