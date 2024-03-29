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

import com.percussion.design.objectstore.PSSubject;
import com.percussion.services.security.PSJaasUtils;
import com.percussion.services.security.PSPrincipalAttribute;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.utils.tools.PSPatternMatcher;
import com.percussion.security.IPSPrincipalAttribute;
import com.percussion.security.IPSPrincipalAttribute.PrincipalAttributes;
import com.percussion.security.IPSSubjectCataloger;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.PSSecurityCatalogException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;

/**
 * Mock implementation of the {@link IPSSubjectCataloger} for testing purposes.
 * Has two optional properties:
 * <ul>
 * <li>supportsGroups - see {@link #setSupportsGroups(String)}</li>
 * <li>emailAddress - see {@link #setEmailAddress(String)}</li>
 * </ul>
 */
public class PSMockSubjectCataloger implements IPSSubjectCataloger
{
   /**
    * First list of users.
    */
   private static final String[] USERS_1 = {"subCatUser1_1", "subCatUser1_2"};
   
   /**
    * Second list of users.
    */
   private static final String[] USERS_2 = {"subCatUser2_1", "subCatUser2_2"};
   
   /**
    * List of group names.
    */
   private static final String[] GROUPS = {"subCatGroup1", "subCatGroup2"};
   
   /**
    * List of group members, each group in {@link #GROUPS} has members listed
    * from the corresponding <code>USERS_XXX</code> list.
    */
   private static final String[][] GROUPMEMBERS = {USERS_1, USERS_2};
   
   /**
    * List of all users defined in all groups.
    */
   private static String[] ALLUSERS;
   
   static
   {
      List<String> allUsers = new ArrayList<>();
      for (int i = 0; i < GROUPMEMBERS.length; i++)
      {
         for (int j = 0; j < GROUPMEMBERS[i].length; j++)
         {
            allUsers.add(GROUPMEMBERS[i][j]);
         }
      }
      
      ALLUSERS = allUsers.toArray(new String[allUsers.size()]);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#getName()
    */
   public String getName()
   {
      return m_name;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#setName(String)
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      m_name = name;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#getDescription()
    */
   public String getDescription()
   {
      return m_description;
   }

   /* (non-Javadoc)
    * @see IPSSubjectCataloger#setDescription(String)
    */
   public void setDescription(String desc)
   {
      m_description = desc;
   }

   /* (non-Javadoc)
    * @see IPSSubjectCataloger#findUsers(List)
    */
   @SuppressWarnings("unused")
   public List<Subject> findUsers(List<String> names)
      throws PSSecurityCatalogException
   {
      List<Subject> results = new ArrayList<>();
      
      Boolean[] matches = new Boolean[ALLUSERS.length];
      boolean allMatch = false;
      
      if (!(names == null || names.isEmpty()))
      {
         for (String name : names)
         {
            PSPatternMatcher matcher = PSPatternMatcher.SQLPatternMatcher(
               name);
            
            for (int i = 0; i < ALLUSERS.length; i++)
            {
               if (matches[i] == null || !matches[i])
                  matches[i] = matcher.doesMatchPattern(ALLUSERS[i]);
            }
         }
      }
      else
      {
         allMatch = true;
      }

      for (int i = 0; i < ALLUSERS.length; i++)
      {
         if (allMatch || matches[i])
         {
            Subject sub = createSubject(ALLUSERS[i]);
            
            // add email attribute
            List<String> values = new ArrayList<>();
            String addr;
            if (m_emailAddress != null)
               addr = "\"" + ALLUSERS[i] + "\"<" + m_emailAddress + ">";
            else
               addr = ALLUSERS[i] + "@test.percussion.com";
            values.add(addr);
            IPSPrincipalAttribute attr = new PSPrincipalAttribute("mail", 
               PrincipalAttributes.EMAIL_ADDRESS, values);
            sub.getPrincipals().add(attr);            
            results.add(sub);
         }
      }
      
      return results;
   }
   
   /* (non-Javadoc)
    * @see IPSSubjectCataloger#supportsAttributeType(PrincipalAttributes)
    */
   public boolean supportsAttributeType(PrincipalAttributes attrType)
   {
      if (attrType == null)
         throw new IllegalArgumentException("attrType may not be null");
      
      if (attrType.equals(PrincipalAttributes.EMAIL_ADDRESS))
         return true;
      else
         return attrType.equals(PrincipalAttributes.SUBJECT_NAME);
   }   

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#supportsGroups()
    */
   public boolean supportsGroups()
   {
      return m_supportsGroups;
   }
   
   /**
    * Setter for cataloger properties to control group support
    * 
    * @param isSupported "true" if supported, "false" if not.
    */
   public void setSupportsGroups(String isSupported)
   {
      m_supportsGroups = "true".equalsIgnoreCase(isSupported);
   }
   
   /**
    * Setter for cataloger properties to specify an email address to use.  If
    * supplied, returned subjects will specify this address, but will specify
    * the subject name as the alias for the email address 
    * (e.g. "subCatUser1_1" &lt;jay_seletz@percussion.com>).
    * 
    * @param address The address, may be <code>null</code> or empty if not 
    * supplied.
    */
   public void setEmailAddress(String address)
   {
      if (!StringUtils.isBlank(address))
         m_emailAddress = address;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#findGroups(java.lang.String)
    */
   @SuppressWarnings("unused")
   public List<Principal> findGroups(String pattern)
      throws PSSecurityCatalogException
   {
      List<Principal> results = new ArrayList<>();
      if (!m_supportsGroups)
         return results;
      
      boolean allMatch = false;
      Boolean[] matches = new Boolean[GROUPS.length];
      
      if (!StringUtils.isBlank(pattern))
      {
         PSPatternMatcher matcher = PSPatternMatcher.SQLPatternMatcher(
            pattern);
         for (int i = 0; i < GROUPS.length; i++)
         {
            if (matches[i] == null || !matches[i])
               matches[i] =  matcher.doesMatchPattern(GROUPS[i]);
         }
      }
      else
      {
         allMatch = true;
      }

      for (int i = 0; i < GROUPS.length; i++)
      {
         if (allMatch || matches[i])
            results.add(PSTypedPrincipal.createSubject(GROUPS[i]));
      }
      
      return results;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#getGroupMembers(java.util.List)
    */
   public List<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups)
   {
      if (groups == null)
         throw new IllegalArgumentException("groups may not be null");
      
      List<IPSTypedPrincipal> results = new ArrayList<>();
      if (!m_supportsGroups)
         return results;
      
      // copy list for removing during traversal
      List<Principal> testGroups = new ArrayList<>(groups);
      for (Principal group : testGroups)
      {
         for (int i = 0; i < GROUPS.length; i++)
         {
            if (group.getName().equals(GROUPS[i]))
            {
               groups.remove(group);
               for (int j = 0; j < GROUPMEMBERS[i].length; j++)
               {
                  results.add(PSTypedPrincipal.createSubject(
                     GROUPMEMBERS[i][j]));
               }
               break;
            }
         }
      }
      
      return results;
   }

   /* (non-Javadoc)
    * @see com.percussion.security.IPSSubjectCataloger#getUserGroups(java.security.Principal)
    */
   public List<Principal> getUserGroups(Principal user)
   {
      if (user == null)
         throw new IllegalArgumentException("user may not be null");
      
      List<Principal> results = new ArrayList<>();
      if (!m_supportsGroups)
         return results;
      
      for (int i = 0; i < GROUPMEMBERS.length; i++)
      {
         for (int j = 0; j < GROUPMEMBERS[i].length; j++)
         {
            if (user.getName().equals(GROUPMEMBERS[i][j]))
            {
               results.add(PSTypedPrincipal.createGroup(GROUPS[i]));
               break;
            }
          }
      }      
      
      return results;
   }

   /**
    * Create a subject using the supplied name.
    * @param name The name, assumed not <code>null</code> or empty.
    * 
    * @return The subject, never <code>null</code>.
    */
   private Subject createSubject(String name)
   {
      PSSubject sub = PSJaasUtils.principalToSubject(
         PSTypedPrincipal.createSubject(name)); 
      return PSJaasUtils.convertSubject(sub);      
   }
   
   /**
    * Name of this cataloger, see {@link IPSSubjectCataloger#setName(String)}.
    */
   private String m_name;
   
   /**
    * Description of this cataloger, see 
    * {@link IPSSubjectCataloger#getDescription()}.
    */
   private String m_description;
   
   /**
    * Flag to determine if groups are supported.  Set by framework from 
    * registered "supportsGroups" property during initialization.
    */
   private boolean m_supportsGroups = false;
   
   /**
    * The email address to use for subjects, <code>null</code> unless specified
    * in the cataloger properties - see {@link #setEmailAddress(String)}.
    */
   private String m_emailAddress = null;
}

