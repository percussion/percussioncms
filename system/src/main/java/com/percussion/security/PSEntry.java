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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSEntry abstract class is implemented by the various security
 * providers supported within E2. Within each ACL, a list of entries is
 * stored. E2 uses the IPSEntry implementation to check ACLs for
 * matching entries. Each security provider is given the request context
 * and asked to provide the user and group information associated with the
 * request. If the security provider did not authenticate the user,
 * <code>null</code> is returned. For providers which returned valid
 * information, their entry table is checked in the ACL. Each entry in the
 * security provider's entry table is then asked to compare the credentials
 * to the entries definition. Since entries may contain wild cards, etc.
 * a sequential approach must be taken rather than a ConcurrentHashMap search, etc.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public abstract class PSEntry implements Comparable<PSEntry>
{

   /**
    * Construct an entry object for the named entry.
    *
    * @param   name               the name of the entry
    * @param   accessLevel         the access level to assign this entry
    */
   protected PSEntry(String name, int accessLevel)
   {
      super();
      m_name                = name;
      m_accessLevel         = accessLevel;
   }

   /**
    * Get the name associated with this entry.
    *
    * @return               the entry name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Does the specified entry match this one? The entry must be of the
    * same provider type. If one of the entries is a filter, it will use
    * the information defined in the other entry to test for equality.
    * <p>
    * This implementation merely checks that the providers are the same
    * (see the {@link #isSameProvider isSameProvider} method for details)
    * and that the entry names are the same. Sub-classes should override
    * this class to verify types, etc.
    *
    *   @param      entry         the entry to check
    *
    * @return                  <code>true</code> if the entry matches;
    *                           <code>false</code> otherwise
    */
   public boolean isMatch(PSEntry entry)
   {
      return this.m_name.equals(entry.m_name);
   }

   /**
    * Get the access level associated with this entry. This should usually
    * be called after a call to the {@link #isMatch isMatch} method
    * returns <code>true</code>.
    *
    * @return                  the access level assigned to this entry
    */
   public int getAccessLevel()
   {
      return m_accessLevel;
   }

   /**
    * Is this class a filter? Filters can be used to perform checks against
    * other entries based upon attributes, etc.
    *
    * @return                  <code>true</code> if this object is a filter;
    *                                             <code>false</code> otherwise
    */
   public abstract boolean isFilter();


   public boolean equals(Object obj)
   {
      if (!(obj instanceof com.percussion.security.PSEntry))
         return false;

      PSEntry dest = (PSEntry)obj;

      if (((m_name == dest.m_name) ||
         ((m_name != null) && m_name.equals(dest.m_name))) &&
         (m_accessLevel == dest.m_accessLevel))
      {
         return true;
      }

      return false;
   }

   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return m_name == null ? 0 : m_name.hashCode() + m_accessLevel;
   }

   public int compareTo(PSEntry o)
   {
      return m_name.compareTo(o.m_name);
   }

   /**
    * Get the entry status.
    *
    * @param doc the document for which to create the status element, not
    *    <code>null</code>.
    * @return the element containing all entry status information,
    *    never <code>null</code>.
    * @throws IllegalArgumentException if the provided document is
    *    <code>null</code>.
    */
   public Element getEntryStatus(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("the document cannot be null");

      Element name = doc.createElement("Name");
      name.appendChild(doc.createTextNode(getName()));

      Element entry = doc.createElement("Entry");
      entry.setAttribute("accessLevel", Integer.toString(getAccessLevel()));
      entry.appendChild(name);

      return entry;
   }

   private java.lang.String m_name;
   private int m_accessLevel;
}

