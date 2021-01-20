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
package com.percussion.deployer.server.dependencies;


import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;

/**
 * Helper class to handle format the parent and child id combination for
 * a dependency object, who can only identified with the combination of
 * its parent and its own ids.
 */
public class PSPairDependencyId
{
   /**
    * Constructor from a given (formated) id.
    *
    * @param depId The formated id, it may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>depId</code> is
    * <code>null</code> or empty
    * @throws PSDeployException if the <code>depId</code> is not in the
    * format of <code>parentId:childId</code> where <code>parentId</code> is
    * numeric.
    */
   public PSPairDependencyId(String depId) throws PSDeployException
   {
      if (depId == null || depId.trim().length() == 0)
         throw new IllegalArgumentException("depId may not be null or empty");

      // get the parent and child ids
      boolean isValid = true;
      int sepPos = depId.indexOf(":");
      if (sepPos == -1)
         isValid = false;
         
      // test not empty child
      if (isValid)
      {
         m_parentId = depId.substring(0, sepPos);
         m_childId = depId.substring(sepPos + 1);
         
         if (m_parentId.trim().length() == 0 ||
            m_childId.trim().length() == 0)
         {
            isValid = false;
         }
      }
      
      // test numeric parent
      if (isValid)
      {
         try 
         {
            Integer.parseInt(m_parentId);
         }
         catch (NumberFormatException e) 
         {
            isValid = false;
         }
      }
      
      if (!isValid)
      {
          Object[] args = {depId};
          throw new PSDeployException(
             IPSDeploymentErrors.WRONG_FORMAT_FOR_PAIRID_DEP_ID, args);
      }
   }

   /**
    * Generate a dependency id from a given parent and child ids of the
    * database.
    *
    * @param parentId The parent id directly from the database, it may not be
    * <code>null</code> or empty, must represent a numeric value.
    * @param childId The child id directly from the database, it may not
    * be <code>null</code> or empty.
    *
    * @return The generated dependency id for a child dependency object. It
    * will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if a parameter is invalid
    */
   static public String getPairDependencyId(String parentId, String childId)
   {
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");
      if (childId == null || childId.trim().length() == 0)
         throw new IllegalArgumentException(
            "childId may not be null or empty");
      // just trying to make sure a number is passed. Immaterial if it were
      // an integer or long, go LONG
      try 
      {
         Long.parseLong(parentId);
      }
      catch (NumberFormatException e) 
      {
         throw new IllegalArgumentException("parentId of pair must be numeric");
      }
            
      return parentId + ":" + childId;
   }


   /**
    * Get child id from database table.
    *
    * @return child id, will never be <code>null</code> or empty.
    */
   public String getChildId()
   {
      return m_childId;
   }

   /**
    * Get parent id from database table.
    *
    * @return parent id, will never be <code>null</code> or empty.
    */
   public String getParentId()
   {
      return m_parentId;
   }

   /**
    * Child id from the table, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_childId;

   /**
    * Parent id from the table, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_parentId;
}
