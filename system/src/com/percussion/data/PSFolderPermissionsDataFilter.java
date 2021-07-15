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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.percussion.cms.objectstore.PSFolderAcl;
import com.percussion.cms.objectstore.PSFolderPermissions;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.error.PSRuntimeException;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;

/**
 * Filters rows representing folders based on the user's permissions for the
 * folder. The row is filtered out if the current user does not have read access
 * to the folder, or if no folder acl is located for the current row.
 */
public class PSFolderPermissionsDataFilter implements IPSResultSetDataFilter
{
   /**
    * Create the filter.
    * 
    * @param folderIdColName The name of the result column specifying the 
    * folder id, may not be <code>null</code> or empty.
    */
   public PSFolderPermissionsDataFilter(String folderIdColName)
   {
      if (StringUtils.isBlank(folderIdColName))
         throw new IllegalArgumentException(
            "folderIdColName may not be null or empty");
      
      m_folderIdColName = folderIdColName;
   }

   // see class desc and interface
   public boolean accept(PSExecutionData data, Object[] vals)
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");
      
      if (vals == null)
         throw new IllegalArgumentException("vals may not be null");
      
      if (vals.length != 1 || !(vals[0] instanceof String))
         throw new IllegalArgumentException(
            "vals must contain a single string");
      
      int contentId = Integer.parseInt((String) vals[0]);
      
      PSRequest request = data.getRequest();
      PSServerFolderProcessor processor = PSServerFolderProcessor.getInstance();
      try
      {
         List<PSLocator> locList = processor.getAncestorLocators(new PSLocator(
            contentId));
         
         // get list of folder ids
         int[] folderids = new int[locList.size() + 1];
         folderids[0] = contentId;
         for (int i = 1; i < folderids.length; i++)
         {
            PSLocator folderLoc = locList.get(i - 1);
            
            // first locator is parent folder
            folderids[i] = folderLoc.getId();
         }
         
         PSFolderAcl[] acls = processor.getFolderAcls(folderids);
         if (acls.length == 0)
            return true;  
         
         for (PSFolderAcl acl : acls)
         {
            PSFolderPermissions folderPerms = new PSFolderPermissions(acl);
            
            if (!folderPerms.hasReadAccess())
               return false;
            
         }
         
         return true;
      }
      catch (PSException e)
      {
         throw new PSRuntimeException(e.getErrorCode(), e.getErrorArguments());
      }
   }

   // see interface
   public List getColumns()
   {
      List<String> cols = new ArrayList<>();
      cols.add(m_folderIdColName);
      
      return cols;
   }

   /**
    * Name of the column specifying the folder id, never <code>null</code> or 
    * empty or modified after construction.
    */
   private final String m_folderIdColName;
}

