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