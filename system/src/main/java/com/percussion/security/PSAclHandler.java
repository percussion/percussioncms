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

package com.percussion.security;

import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The PSAclHandler class uses the defined security providers to
 * verify user credentials and determine the appropriate access level
 * for the user.
 *
 * @author  Tas Giakouminakis
 * @version 1.0
 * @since 1.0
 */
public class PSAclHandler
{
   /**
    * Construct an ACL handler.
    *
    * @param acl The ACL defining the behavior to enforce.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if acl is <code>null</code>.
    */
   public PSAclHandler(PSAcl acl)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");

      /* Go through the ACL and add an entry to the hash. The key of the hash is
       * provider/providerInstance/entryName. The value for each key is an array
       * of objects containing the security.PSEntry subclass entries, such as
       * userEntry, groupEntry, or roleEntry.
       */
      m_aclUserEntries  = new HashMap<>();
      m_aclGroupEntries = new HashMap<>();
      m_aclRoleEntries  = new HashMap<>();

      PSCollection aces = acl.getEntries();
      PSAclEntry   cur;

      for (int i = 0; i < aces.size(); i++)
      {
         cur = (PSAclEntry)aces.get(i);

         if (cur.isUser()) {
            addUserEntry(cur);
         }
         else if (cur.isGroup()) {
            addGroupEntry(cur);
         }
         else if (cur.isRole()) {
            addRoleEntry(cur);
         }
      }

      // store the multi-membership setting
      if (acl.isAccessForMultiMembershipMinimum())
         m_aclMultiMembershipFlag = MULTI_ACE_GETS_MIN;
      else if (acl.isAccessForMultiMembershipMaximum())
         m_aclMultiMembershipFlag = MULTI_ACE_GETS_MAX;
      else if (acl.isAccessForMultiMembershipMergedMinimum())
         m_aclMultiMembershipFlag = MULTI_ACE_GETS_MERGED_MIN;
      else if (acl.isAccessForMultiMembershipMergedMaximum())
         m_aclMultiMembershipFlag = MULTI_ACE_GETS_MERGED_MAX;

   }

   /**
    * Get the access level for the specified user's security token. If
    * the user does not have access, an exception is thrown.
    *
    * @param tok The user's security token.
    *
    * @return the user's access level
    *
    * @throws PSAuthenticationRequiredException if we have not yet tried to
      * authenticate the user, or if they are attempting anonymous access and it
      * is not permitted.
      *
    * @throws PSAuthorizationException if the user has logged in but was
      * denied access to the application
    */
   public int getUserAccessLevel(PSSecurityToken tok)
      throws PSAuthenticationRequiredException, PSAuthorizationException
   {
         PSUserSession sess = tok.getUserSession();

         // Make sure we've tried to authenticate them
         if (sess == null)
             throw new PSAuthenticationRequiredException(
                  tok.getResourceName(), tok.getResourceType());

         return getUserAccessLevel(sess, tok.getResourceType(),
             tok.getResourceName());
    }

   /**
    * Get the access level for the specified user session. If
    * the user does not have access, an exception is thrown.
    *
    * @param sess The user session
    *
    * @return  the user's access level
    *
    * @exception PSAuthenticationRequiredException
    *                             if the user has not logged in and
    *                             anonymous access is not permitted
    *
    * @exception PSAuthorizationException
    *                             if the user has logged in but was
    *                             denied access to the application
    */
   public int getUserAccessLevel(
      PSUserSession sess,
      String resourType,
      String resourName
      )
      throws PSAuthenticationRequiredException, PSAuthorizationException
   {
      PSUserEntry[] users = sess.getAuthenticatedUserEntries();
         int usersLength = (users == null) ? 0 : users.length;
      List<PSUserEntry> usersLoggedIn = new ArrayList<>(usersLength);

      // get all the group entries from the session based on the above
      PSGroupEntry[] groups = new PSGroupEntry[0];
      PSRoleEntry[] roles = new PSRoleEntry[0];
      if (sess != null)
      {
         Collection<PSGroupEntry> groupList = new HashSet<>();
         Collection<PSRoleEntry> roleList = new HashSet<>();
         for ( int i = 0; i < users.length; i++ )
         {
            PSGroupEntry[] groupEntryArray = users[i].getGroups();
            if (groupEntryArray != null)
               groupList.addAll(Arrays.asList(groupEntryArray));
            
            PSRoleEntry[] roleEntryArray = users[i].getRoles();
            if (roleEntryArray != null)
               roleList.addAll(Arrays.asList(roleEntryArray));            
         }

         groups = groupList.toArray(new PSGroupEntry[groupList.size()]);
         roles = roleList.toArray(new PSRoleEntry[roleList.size()]);
      }

      List<PSEntry> groupRoleMatch =
            new ArrayList<>( (groups != null) ? groups.length : 0 );


      String testProvider = null;
      String testName = null;

      // get the logged in users from the user entries
      for (int i = 0; i < users.length; i++)
      {
         testName = users[i].getName();
             testName = testName.toLowerCase();

         PSEntry entryMatch = m_aclUserEntries.get(testName);

         if (entryMatch != null)
            return entryMatch.getAccessLevel();

         usersLoggedIn.add(users[i]);
      }

      // get the logged in groups from group entries
      for (int i = 0; i < groups.length; i++)
      {
         testName = groups[i].getName().toLowerCase();

         PSGroupEntry entryMatch = m_aclGroupEntries.get(testName);

         if (entryMatch != null)
            groupRoleMatch.add(entryMatch);
      }

      // get the role matches
      List<PSRoleEntry> roleMatchEntries = 
         new ArrayList<>();
      for (int i = 0; i < roles.length; i++)
      {
         testName = roles[i].getName().toLowerCase();
         PSRoleEntry entryMatch = m_aclRoleEntries.get(testName);
         if (entryMatch != null)
            groupRoleMatch.add(entryMatch);
      }

      int size = groupRoleMatch.size();
      if (groupRoleMatch.isEmpty())
      {
         // Handle users who have logged in, is default access allowed?
         int totalUsersLoggedIn = usersLoggedIn.size();
         for (int i = 0; i < totalUsersLoggedIn; i++)
         {
            testName = DEFAULT_USER_NAME.toLowerCase();

            PSEntry entryMatch = m_aclUserEntries.get(testName);

            if (entryMatch != null)
               return entryMatch.getAccessLevel();
         }

         String hiddenName = ANONYMOUS_USER_NAME.toLowerCase();
         // Handle users who have or have not logged in, is anonymous access allowed?
         PSEntry entryMatch = m_aclUserEntries.get(hiddenName);
         if (entryMatch != null)
            return entryMatch.getAccessLevel();

         // Since anonymous login is also NOT allowed, throw this message
         if (totalUsersLoggedIn > 0)
         {
            testProvider = "";
            testName     = "";
            for (int i = 0; i < totalUsersLoggedIn; i++)
            {
               PSEntry user = usersLoggedIn.get(i);
               testName += user.getName() + ", ";
            }
            throw new PSAuthorizationException(resourType, resourName,
               testProvider, testName);
         }
         else
            throw new PSAuthenticationRequiredException(resourType, resourName);
      }
      else if (size == 1)
         return ( groupRoleMatch.get(0)).getAccessLevel();
      else if (size > 1)
      {
         int[] matchArray = new int[size];

         for (int i = 0; i < size; i++)
            matchArray[i] = (groupRoleMatch.get(i)).getAccessLevel();

         // shellSortArray(matchArray);   in case we need this
         int retLevel = 0;

         switch (m_aclMultiMembershipFlag)
         {
         case MULTI_ACE_GETS_MIN:                // If use shellSortArray, then
            retLevel = findMinLevel(matchArray); // retLevel = matchArray[0];
            break;

         case MULTI_ACE_GETS_MAX:                // If use shellSortArray, then
            retLevel = findMaxLevel(matchArray); // retLevel = matchArray[size-1];
            break;

         case MULTI_ACE_GETS_MERGED_MIN:
            retLevel = matchArray[0];
            for (int i = 1; i < size; i++)
            {
               retLevel &= matchArray[i];     // seeking intersection
            }
            break;

         // Default in specification is MERGED_MAX!
         default:
         case MULTI_ACE_GETS_MERGED_MAX:
            retLevel = matchArray[0];
            for (int i = 1; i < size; i++)
            {
               retLevel |= matchArray[i]; // seeking union
            }
            break;
         } // end of switch

         return retLevel;
      }

      return 0;
   }

   /**
    * Get the access level for the user making the specified request. If the
    * user does not have access, an exception is thrown.
    * 
    * @param req The request to check, never <code>null</code>.
    * 
    * @return The user's access level. An OR'd collection of access flags which
    * are defined in the {@link PSAclEntry} class. Which flags are present is
    * dependent upon the context of the handler.
    * 
    * @throws PSAuthenticationRequiredException If the user has not logged in
    * and anonymous access is not permitted.
    * 
    * @throws PSAuthenticationFailedException If authentication fails. A
    * PSAuthenticationFailedExException will be thrown instead of
    * PSAuthenticationFailedException if the server allows detailed info about
    * the security providers.
    * 
    * @throws PSAuthorizationException If the user has logged in but was denied
    * access to the application.
    * 
    * @throws IllegalArgumentException If req is <code>null</code>.
    */
   public int getUserAccessLevel(PSRequest req)
      throws PSAuthorizationException, PSAuthenticationFailedException
   {
      if ( null == req )
         throw new IllegalArgumentException( "A request must be supplied" );

      PSUserSession sess = req.getUserSession();

      String resourName;
      String resourType = req.getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE);
      if (resourType == null)
         resourType = "";
      if (resourType.startsWith("design-"))
      {
         resourName = resourType.substring("design-".length());
         resourType = "Designer";
      }
      else if (resourType.startsWith("admin-"))
      {
         resourName = resourType.substring("admin-".length());
         resourType = "Admin";
      }
      else
      {   // must be a data request
         resourName = req.getRequestPage();
         resourType = req.getCgiVariable(IPSCgiVariables.CGI_PS_APP_NAME);
         if (resourType == null)
            resourType = "application";
      }

      return getUserAccessLevel(sess, resourType, resourName);
   }

   /**
    * Gets the Acl entries matching the specified criteria.
    *
    * @param type The type of Acl entry to locate.  Must be one of the
    * <code>PSAclEntry.ACE_TYPE_xxx</code> value.
    *
    * @return An Iterator over <code>0</code> or more <code>PSEntry</code>
    * objects.  The specific type of entry will be determined by the type of Acl
    * entry requested.  Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> does not represent a
    * valid type.
    */
   public Iterator<PSEntry> getAclEntries(int type)
   {
      Map<String, ? extends PSEntry> entryMap = null;
      switch (type)
      {
         case PSAclEntry.ACE_TYPE_USER:
            entryMap = m_aclUserEntries;
            break;
         case PSAclEntry.ACE_TYPE_GROUP:
            entryMap = m_aclGroupEntries;
            break;
         case PSAclEntry.ACE_TYPE_ROLE:
            entryMap = m_aclRoleEntries;
            break;
         default:
            throw new IllegalArgumentException("invalid type: " + type);
      }


      List<PSEntry> matches = new ArrayList<>(entryMap.values());
      
      // no worries, PSEntry objects are immutable
      return matches.iterator();
   }

   /**
    * Find the minimum array element.
    *
    * @param array    the array to be checked
    *
    * @return  the minimum array element
    */
   private int findMinLevel(int[] array)
   {
      int min = array[0];
      for (int i = 1; i < array.length; i++)
      {
         if (min > array[i])
            min = array[i];
      }
      return min;
   }

   /**
    * Find the maximum array element.
    *
    * @param array      the array to be checked
    *
    * @return  the maximum array element
    */
   private int findMaxLevel(int[] array)
   {
      int max = array[0];
      for (int i = 1; i < array.length; i++){
         if (max < array[i])
            max = array[i];
      }
      return max;
   }

   /**
    * Add ACL user entry into the hash, map PSAclEntry to PSUserEntry.
    *
    ** @param  entry          a PSAclEntry object
    */
   private void addUserEntry(PSAclEntry entry)
   {
      String name = entry.getName();
      int accessLevel = entry.getAccessLevel();


      String hashKey = name.toLowerCase();

      PSUserEntry userEntry = new PSUserEntry(name, accessLevel, null, null, 
         null);

      m_aclUserEntries.put(hashKey, userEntry);
   }

   /**
    * Add ACL group entry into the hash, map PSAclEntry to PSGroupEntry.
    *
    * @param entry          a PSAclEntry object
    */
   private void addGroupEntry(PSAclEntry entry)
   {
      String name = entry.getName();
      int accessLevel = entry.getAccessLevel();

      String hashKey = name.toLowerCase();

      PSGroupEntry groupEntry = new PSGroupEntry(name, accessLevel);

      m_aclGroupEntries.put(hashKey, groupEntry);
   }

   /**
    * Add ACL role entry into the hash, map PSAclEntry to PSRoleEntry.
    *
    * @param entry          a PSAclEntry object
    */
   private void addRoleEntry(PSAclEntry entry)
   {
      String name = entry.getName();

      int accessLevel = entry.getAccessLevel();

      String hashKey = name.toLowerCase();

      PSRoleEntry roleEntry = new PSRoleEntry(name, accessLevel);

      m_aclRoleEntries.put(hashKey, roleEntry);
   }


   /**
    * The hashmaps have keys = provider/providerInstance/entryName
    *              values = acl entries
    */
   private Map<String, PSUserEntry> m_aclUserEntries  = null;
   private Map<String, PSGroupEntry> m_aclGroupEntries    = null;
   private Map<String, PSRoleEntry> m_aclRoleEntries  = null;
   private PSRoleManager m_roleManager  = PSRoleManager.getInstance();

   private int m_aclMultiMembershipFlag = 0;

   // The followings are based on the definition in PSAcl.java
   private static final int MULTI_ACE_GETS_MIN = 1;
   private static final int MULTI_ACE_GETS_MAX = 2;
   private static final int MULTI_ACE_GETS_MERGED_MIN = 3;
   private static final int MULTI_ACE_GETS_MERGED_MAX = 4;

   // The followings are based on the definition in PSAclEntry.java
   public static final String ANONYMOUS_USER_NAME = "Anonymous";
   public static final String DEFAULT_USER_NAME = "Default";
}

