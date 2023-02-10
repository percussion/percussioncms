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
