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

import com.percussion.util.PSCharSets;
import com.percussion.util.PSXMLDomUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSUserEntry class defines the implementation of a user entry
 * within E2.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSUserEntry extends PSEntry 
{
   /**
    * Convenience ctor that calls 
    * {@link #PSUserEntry(String, int, PSGroupEntry[], PSUserAttributes, String)
    * this(name, accessLevel, groups, null, attributes, signature)}
    */
   public PSUserEntry(String name, int accessLevel, PSGroupEntry[] groups,
      PSUserAttributes attributes, String signature)
   {
      this(name, accessLevel, groups, null, attributes, signature);
   }
   /**
    * Construct a user entry object for the named entry.
    *
    * @param   name               the name of the entry
    *
    * @param   accessLevel         the access level to assign this entry
    *
    * @param   groups            the groups this user is a member of
    * 
    * @param roles The roles this user is a member of, may be <code>null</code>
    * if none are to be supplied. 
    *
    * @param   attributes         the security provider specific attributes
    *                              associated with this user
    *
    * @param   signature         the signature to associate with this entry
    */
   public PSUserEntry(String name, int accessLevel, PSGroupEntry[] groups,
      PSRoleEntry[] roles, PSUserAttributes attributes, String signature)
   {
      super(name, accessLevel);
      
      if (groups != null)
      {
         PSGroupEntry[] grpcopy = new PSGroupEntry[groups.length]; 
         System.arraycopy(groups, 0, grpcopy, 0, groups.length);
         Arrays.sort(grpcopy);
         groups = grpcopy;
      }
      
      if (roles != null)
      {
         PSRoleEntry[] rolecopy = new PSRoleEntry[roles.length]; 
         System.arraycopy(roles, 0, rolecopy, 0, roles.length);
         Arrays.sort(rolecopy);
         roles = rolecopy;
      }

      m_groups = groups;
      m_attributes = attributes;
      m_signature = signature;
      m_roles = roles;
   }

   /**
    * Get the groups this user is a member of.
    *
    * @return the groups, or <code>null</code>. The order of the group entries
    * is undefined.
    */
   public PSGroupEntry[] getGroups()
   {
      return m_groups;
   }
   
   /**
    * Get the roles this user is a member of, only populated during 
    * authentication if a provider determines roles, not used after that.
    * 
    * @return The roles, may be <code>null</code>.  The order of the role 
    * entries is undefined.
    */
   public PSRoleEntry[] getRoles()
   {
      return m_roles;
   }

   /**
    * Get the security provider specific attributes associated with this
    * user.
    *
    * @return               the attributes, or <code>null</code>
    */
   public PSUserAttributes getAttributes()
   {
      return m_attributes;
   }


   /* ********************** PSEntry Implementation ********************** */

   /**
    * Does the specified entry match this one? The entry must be of the
    * same provider type. If one of the entries is a filter, it will use
    * the information defined in the other entry to test for equality.
    *
    *   @param      entry         the entry to check
    *
    * @return                  <code>true</code> if the entry matches;
    *                           <code>false</code> otherwise
    */
   public boolean isMatch(PSEntry entry)
   {
      /* we can only check our own type, or if it's a filter, ask it
       * to perform the check.
       */
      if (this.getClass().isInstance(entry))
         return super.isMatch(entry);   // do the default comparison
      else if (entry.isFilter())
         return entry.isMatch(this);   // let the filter do the check

      return false;
   }

   /**
    * Is this class a filter? Filters can be used to perform checks against
    * other entries based upon attributes, etc.
    *
    * @return                  <code>false</code> is always returned for the
    *                           PSUserEntry class
    */
   public boolean isFilter()
   {
      return false;
   }

   public boolean equals(Object obj)
   {
      if (!(obj instanceof com.percussion.security.PSUserEntry))
         return false;

      // verify name, etc. are the same
      if (!super.equals(obj))
         return false;

      PSUserEntry dest = (PSUserEntry)obj;

      int srcSize = (m_groups == null) ? 0 : m_groups.length;
      int destSize = (dest.m_groups == null) ? 0 : dest.m_groups.length;
      if (srcSize != destSize)
         return false;

      if ((srcSize != 0) && !ArrayUtils.isEquals(m_groups, dest.m_groups))
         return false;

      // also need to consider the user attributes!
      if (m_attributes == null ^ dest.m_attributes == null)
         return false;      
      else if (m_attributes != null && !ArrayUtils.isEquals(m_attributes, 
         dest.m_attributes))
      {
         return false;
      }
      
      if (m_roles == null ^ dest.m_roles == null)
         return false;
      else if (m_roles != null && !ArrayUtils.isEquals(m_roles, 
         dest.m_roles))
      {
         return false;
      }
      
      return true;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode() + (m_groups == null ? 0 : m_groups.hashCode()) + 
      (m_roles == null ? 0 : m_roles.hashCode()) + (m_attributes == null ? 0 : 
         m_attributes.hashCode());
   }

   /**
    * Does the signature of the specified entry match this one?
    *
    *   @param      entry         the entry to check
    *
    * @return                  <code>true</code> if the signatures match;
    *                           <code>false</code> otherwise
    */
   public boolean isSignatureMatch(PSEntry entry)
   {
      /* we can only check our own type */
      if (!(entry instanceof PSUserEntry))
         return false;

      PSUserEntry userEntry = (PSUserEntry)entry;
      if ((m_signature == null) && (userEntry.m_signature == null))
         return true;
      else if ((m_signature != null) && (userEntry.m_signature != null))
         return m_signature.equals(userEntry.m_signature);

      // if only one is null, this is clearly not a match
      return false;
   }

   /**
    * Create the signature to associate with a user entry from the
    * specified text.
    */
   public static String createSignature(String userId, String pw)
   {
      try {
         // we always sign UTF-8

         MessageDigest md = MessageDigest.getInstance("SHA-1");

         md.update((userId + ":" + pw).getBytes(PSCharSets.rxJavaEnc()));
         byte[] digest = md.digest();

         StringBuffer buf = new StringBuffer(digest.length * 2);
         String sTemp;
         for (int i = 0; i < digest.length; i++) {
            sTemp = Integer.toHexString(digest[i]);
            if (sTemp.length() == 0)
               sTemp = "00";
            else if (sTemp.length() == 1)
               sTemp = "0" + sTemp;
            else if (sTemp.length() > 2)
               sTemp = sTemp.substring(sTemp.length() - 2);

            buf.append(sTemp);
         }

         return buf.toString();
      }
      catch (NoSuchAlgorithmException e) {
         /* this should never happen, but we need to deal with it */
         com.percussion.server.PSConsole.printMsg("Algorithm error", e);
      }
      catch (java.io.UnsupportedEncodingException e)
      {
         // should never happen
         com.percussion.server.PSConsole.printMsg("Algorithm error", e.toString());
      }

      return null;
   }

   /**
    * Get the user entry status.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @return the element containing all user entry status information,
    *    never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public Element getUserEntryStatus(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      Element groups = doc.createElement("MemberOfGroups");
      if (m_groups != null)
      {
         for (int i=0; i<m_groups.length; i++)
         {
            PSGroupEntry g = (PSGroupEntry) m_groups[i];
            groups.appendChild(g.getEntryStatus(doc));
         }
      }

      Element attributes = doc.createElement("Attributes");
      if (m_attributes != null)
      {
         Iterator attrs = m_attributes.keySet().iterator();
         while (attrs.hasNext())
         {
            Element attribute = doc.createElement("Attribute");

            String key = (String) attrs.next();

            //make sure that the key is a valid XML name - fixes Rx-02-11-0056
            String xmlKey = PSXMLDomUtil.makeXmlName(key);

            attribute.setAttribute(xmlKey, m_attributes.getString(key));
         }
      }

      Element entry = doc.createElement("UserEntry");
      entry.setAttribute("signature",
         (m_signature == null) ? "null" : m_signature);
      
      String name = getName();
      
      entry.setAttribute("userName",
            (getName() == null) ? "null" : name);
      entry.appendChild(groups);
      entry.appendChild(attributes);

      return entry;
   }

   /**
    * the groups this user is a member of
   */
   private PSGroupEntry[]       m_groups;
   
   /**
    * The roles this user is a member of, may be <code>null</code> or empty.
    */
   private PSRoleEntry[] m_roles;

   /**
    * the security provider specific attributes associated with this user
   */
   private PSUserAttributes   m_attributes;

   /**
    * the signature associated with this entry
   */
   private String               m_signature;
}

