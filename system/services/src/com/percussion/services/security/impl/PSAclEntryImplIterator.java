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

package com.percussion.services.security.impl;

import com.percussion.security.PSGroupEntry;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserEntry;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.security.IPSTypedPrincipal;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Iterator to walk an acl entries and get the results filtered for the 
 * specified user.
 */
public class PSAclEntryImplIterator implements Iterator<IPSAclEntry>
{
   /**
    * Construct the iterator.
    * 
    * @param acl The acl whose entries are to be walked, may not be 
    * <code>null</code>.
    * @param userEntry The current user's entry, may not be 
    * <code>null</code>. 
    * @param userCommunities The list of the current user's communities,
    * may not be <code>null</code>.
    */
   public PSAclEntryImplIterator(PSAclImpl acl, 
      PSUserEntry userEntry, Set<String> userCommunities)
   {
      if (acl == null)
         throw new IllegalArgumentException("acl may not be null");
      if (userEntry == null)
         throw new IllegalArgumentException("userEntry may not be null");
      if (userCommunities == null)
         throw new IllegalArgumentException(
            "userCommunities may not be null");
      
      m_entries = acl.getEntries().iterator();
      m_userEntry = userEntry;
      m_userCommunities = userCommunities;
      m_next = getNext();
   }

   public boolean hasNext()
   {
      return (m_next != null);
   }

   public IPSAclEntry next()
   {
      if (m_next == null)
      {
         throw new NoSuchElementException();
      }
      
      IPSAclEntry next = m_next;
      m_next = getNext();
      
      return next;
   }

   /**
    * Get the next matching entry.
    * 
    * @return The entry, or <code>null</code> if no more entries are available.
    */
   private IPSAclEntry getNext()
   {
      IPSAclEntry result = null;
      while (m_entries.hasNext() && result == null)
      {
         IPSAclEntry entry = m_entries.next();
         IPSTypedPrincipal p = (IPSTypedPrincipal) entry.getPrincipal();
         if (entry.isUser())
         {
            if (p.isSystemEntry())
            {
               m_defaultUserEntry = entry;
               continue;
            }
            else if (p.getName().equals(m_userEntry.getName()))
            {
               m_matchedDefault = true;
               result = entry;
            }
         }
         else if (entry.isGroup() && m_userEntry.getGroups() != null)
         {
            for (PSGroupEntry groupEntry : m_userEntry.getGroups())
            {
               if (p.getName().equals(groupEntry.getName()))
               {
                  result = entry;
                  m_matchedDefault = true;
                  break;
               }
            }
         }
         else if (entry.isRole() && m_userEntry.getRoles() != null)
         {
            for (PSRoleEntry roleEntry : m_userEntry.getRoles())
            {
               if (p.getName().equals(roleEntry.getName()))
               {
                  result = entry;
                  m_matchedDefault = true;
                  break;
               }
            }
         }
         else if (entry.isCommunity())
         {
            if (p.isSystemCommunity())
            {
               m_anyCommunityEntry = entry;
               continue;
            }
            
            if (m_userCommunities.contains(p.getName()))
            {
               m_matchedCommunity = true;
               result = entry;
            }
         }            
      }
      
      if (result == null)
      {
         // handle "Default" and "AnyCommunity" entries.
         if (!m_matchedDefault && m_defaultUserEntry != null)
         {
            result = m_defaultUserEntry;
            m_defaultUserEntry = null;
         }
         else if (!m_matchedCommunity && m_anyCommunityEntry != null)
         {
            result = m_anyCommunityEntry;
            m_anyCommunityEntry = null;
         }
      }
      
      return result;
   }

   public void remove()
   {
      throw new UnsupportedOperationException("remove not supported");
   }
   
   /**
    * The iterator that this class is backed with, initialized during 
    * constuction, never <code>null</code>.
    */
   private Iterator<IPSAclEntry> m_entries;
   
   /**
    * The user entry supplied during construction, immutable.
    */
   private PSUserEntry m_userEntry;
   
   /**
    * The list of user communities supplied during construction, immutable.
    */
   private Set<String> m_userCommunities;
   
   /**
    * Determines if the user has matched an entry with a type that is covered by
    * the default entry.  If all entries have been processed and this value is 
    * <code>true</code>, then the {@link #m_defaultUserEntry} entry is ignored, 
    * otherwise it is included if it has been found. 
    */
   private boolean m_matchedDefault = false;
   
   /**
    * Determines if the user has matched an entry with the community type .  If 
    * all entries have been processed and this value is <code>true</code>, then 
    * the {@link #m_anyCommunityEntry} entry is ignored, otherwise it is 
    * included if it has been found. 
    */
   private boolean m_matchedCommunity = false;
   
   /**
    * The entry with user type and the name
    * {@link com.percussion.services.security.PSTypedPrincipal#DEFAULT_USER_ENTRY}.
    */
   private IPSAclEntry m_defaultUserEntry = null;
   
   /**
    * The entry with the community type and the name
    * {@link com.percussion.services.security.PSTypedPrincipal#ANY_COMMUNITY_ENTRY}.
    */
   private IPSAclEntry m_anyCommunityEntry = null;

   /**
    * The entry to be returned by the next call to {@link #next()}.  Will be
    * <code>null</code> only if no more entries can be returned.
    */
   private IPSAclEntry m_next;

}
