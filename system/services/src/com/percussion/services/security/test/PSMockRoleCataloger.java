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
package com.percussion.services.security.test;

import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.security.IPSRoleCataloger;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.security.PSSecurityCatalogException;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Mock implementation of the {@link IPSRoleCataloger} for testing purposes.
 */
public class PSMockRoleCataloger implements IPSRoleCataloger
{
   /**
    * First list of users
    */
   private static final String[] USERS_1 = {"subCatUser1_1", "subCatUser1_2"};
   
   /**
    * Second list of users
    */
   private static final String[] USERS_2 = {"subCatUser2_1", "subCatUser2_2"};
   
   /**
    * Third list of users
    */
   private static final String[] USERS_3 = {"subCatGroup1", "subCatGroup2"};
   
   /**
    * List of roles.
    */
   private static final String[] ROLES = {"Author", "Editor", 
      "QA"};
   
   /**
    * List of types of members for each role in {@link #ROLES}
    */
   private static final PrincipalTypes[] TYPES = {PrincipalTypes.SUBJECT, 
      PrincipalTypes.SUBJECT, PrincipalTypes.GROUP};

   /**
    * Array of users for each role in {@link #ROLES}, each role has the users
    * in the corresponding <code>USERS_XXX</code> list.
    */
   private static final String[][] ROLEMEMBERS = {USERS_1, USERS_2, USERS_3};   
   
   // see IPSRoleCataloger
   public String getName()
   {
      return m_name;
   }

   // see IPSRoleCataloger
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }

   // see IPSRoleCataloger
   public String getDescription()
   {
      return m_description;
   }

   // see IPSRoleCataloger
   public void setDescription(String desc)
   {
      m_description = desc;
   }

   // see IPSRoleCataloger
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName)
      throws PSSecurityCatalogException
   {
      if (StringUtils.isBlank(roleName))
         throw new IllegalArgumentException(
            "roleName may not be null or empty");
      
      Set<IPSTypedPrincipal> members = new HashSet<>();
      
      for (int i = 0; i < ROLES.length; i++)
      {
         if (ROLES[i].equals(roleName))
         {
            for (int j = 0; j < ROLEMEMBERS[i].length; j++)
            {
               members.add(new PSTypedPrincipal(ROLEMEMBERS[i][j], TYPES[i]));
            }
            break;
         }
      }
      
      return members;
   }

   // see IPSRoleCataloger
   public Set<String> getUserRoles(IPSTypedPrincipal user)
      throws PSSecurityCatalogException
   {
      if (user == null)
         throw new IllegalArgumentException("user may not be null");
      
      Set<String> roles = new HashSet<>();
      
      for (int i = 0; i < ROLEMEMBERS.length; i++)
      {
         for (int j = 0; j < ROLEMEMBERS[i].length; j++)
         {
            if (!TYPES[i].equals(user.getPrincipalType()))
               continue;
            if (ROLEMEMBERS[i][j].equals(user.getName()))
            {
               roles.add(ROLES[i]);
               break;
            }
         }
      }
      
      return roles;
   }

   /**
    * Name of this cataloger, see {@link IPSRoleCataloger#setName(String)}.
    */
   private String m_name;
   
   /**
    * Description of this cataloger, see 
    * {@link IPSRoleCataloger#getDescription()}.
    */
   private String m_description;
}

