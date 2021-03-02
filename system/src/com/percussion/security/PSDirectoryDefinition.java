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
package com.percussion.security;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Container for one directory with its associated authentication.
 */
public class PSDirectoryDefinition
{
   /**
    * Constructs a new directory for the supplied parameters.
    *
    * @param authentication the authentication associated with the directory,
    *    not <code>null</code>.
    * @param directory the directory definition, not <code>null</code>.
    */
   public PSDirectoryDefinition(PSAuthentication authentication, 
      PSDirectory directory)
   {
      if (authentication == null || directory == null)
         throw new IllegalArgumentException("params cannot be null");

      m_authentication = authentication;
      m_directory = directory;
   }

   /**
    * Get the authentication used for this directory.
    * 
    * @return the authentication associated with this directory, never
    *    <code>null</code>.
    */
   public PSAuthentication getAuthentication()
   {
      return m_authentication;
   }

   /**
    * Get the directory.
    * 
    * @return the directory definition, never <code>null</code>.
    */
   public PSDirectory getDirectory()
   {
      return m_directory;
   }
   
   /**
    * Creates an array with attribute names. The array is created with no 
    * duplicates as a combination of the attribute names defined in this
    * directory and the additional names supplied.
    * 
    * @param additionalReturns a set with attribute names which should be
    *    returned in addition to the returns defined in this directory. May
    *    be <code>null</code> or empty, the method may change the supplied
    *    set.
    * @return a set of attribute names to be returned with search results,
    *    never <code>null</code>.
    */
   @SuppressWarnings(value={"unchecked"})
   public Set<String> getReturnAttributeNames(Set additionalReturns)
   {
      Set<String> results = new HashSet<>();
      
      Collection returns = getDirectory().getAttributes();
      
      if (additionalReturns == null)
         results.addAll(additionalReturns);
         
      if (returns != null)
         results.addAll(returns);
         
      return results;
   }

   /**
    * The authentication associated with this directory. Initialized in
    * constructor, never <code>null</code> or changed after that.
    */
   private PSAuthentication m_authentication = null;

   /**
    * The directory definition. Initialized in constructor, never
    * <code>null</code> or changed after that.
    */
   private PSDirectory m_directory = null;
}
