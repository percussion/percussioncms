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
package com.percussion.search;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This exit is meant to be added to the search results processor. The function of this
 * exit is to clean up unnecessary result field values for folders by
 * setting the values of these fields to an empty value. This exit should
 * run after any other search result processing exit that may modify
 * search result values.
 */
public class PSCleanFolderSearchResultsExit implements
         IPSSearchResultsProcessor
{

   /*
    * @see com.percussion.extension.IPSExtension#init(
    * com.percussion.extension.IPSExtensionDef, java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException
   {
      // no op
   }

   /*
    * Iterates through all search result rows looking for folder items
    * and then sets fields that are not allowed to an empty value.
    * @see com.percussion.extension.IPSSearchResultsProcessor#processRows(
    * java.lang.Object[],
    *      java.util.List, com.percussion.server.IPSRequestContext)
    */
   public List processRows(Object[] params, List rows, IPSRequestContext request)
            throws PSExtensionProcessingException
   {
      Iterator it = rows.iterator();
      String[] disallowedFields = null;
      
      while (it.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) it.next();
         
         if(disallowedFields == null)
            disallowedFields = getDisallowedFields(row);
         
         // Only operate on folders
         String cType = row.getColumnValue("sys_contenttypeid");
         if(cType == null || Integer.parseInt(cType) !=
            PSFolder.FOLDER_CONTENT_TYPE_ID)
            continue;
         
         for(int i = 0; i < disallowedFields.length; i++)
         {            
            row.setColumnValue(disallowedFields[i], "");               
            row.setColumnDisplayValue(disallowedFields[i], "");
         }
      }
      return rows;
   }
   
   /**
    * Returns an array of all column names that are not allowed by
    * a folder content type.
    * 
    * @param row search result row assumed not <code>null</code>.
    * @return an array of disallowed fields, never <code>null</code>
    * may be empty.
    */
   private String[] getDisallowedFields(IPSSearchResultRow row)
   {
      List results = new ArrayList();
      Collection allowed = getAllowedFields();
      // "sys_title" and "sys_contentid" must always be allowed
      if(!allowed.contains("sys_title"))
         allowed.add("sys_title");
      if(!allowed.contains("sys_contentid"))
         allowed.add("sys_contentid");
      Iterator it = row.getColumnNames().iterator();
      while(it.hasNext())
      {
         String colName = (String)it.next();
         if(!allowed.contains(colName))
            results.add(colName);
      }
      return (String[])results.toArray(new String[results.size()]);
      
   }
   
   /**
    * Returns a collection of all allowed search result fields. This method 
    * can be overriden in a subclass to add or subtract the allowed fields.
    * The "sys_title" and "sys_contentid" field will be added if it does not
    * exist in the collection.
    * 
    * @return a list of field names, should never be <code>null</code> or
    *  empty.
    */
   protected Collection getAllowedFields()
   {
      List allowed = new ArrayList(9);
      allowed.add("sys_contentlastmodifier");
      allowed.add("sys_contenttypeid");
      allowed.add("sys_contenttypename"); //deprecated column
      allowed.add("sys_contentlastmodifieddate");
      allowed.add("sys_contentcreateddate");
      allowed.add("sys_contentcreatedby");
      allowed.add("sys_communityid");
      allowed.add("sys_lang");
      allowed.add("sys_folderid");
      allowed.add("sys_siteid");
      allowed.add(IPSHtmlParameters.SYS_PERMISSIONS);
      return allowed;
   }

}
