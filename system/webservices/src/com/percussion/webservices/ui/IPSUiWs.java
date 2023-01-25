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

