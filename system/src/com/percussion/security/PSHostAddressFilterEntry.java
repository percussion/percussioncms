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

import com.percussion.error.PSErrorManager;

import java.util.StringTokenizer;


/**
 * The PSHostAddressFilterEntry class implements filtering for
 * the HostAddress security provider.
 * <p>
 * Host address are either an IP address or a host (DNS) name.
 * To filter a host address, use an asterisk (*) in the appropriate
 * position. For example, to specify any machine in the class C
 * address range of 192.1.1.1 through 192.1.1.254, 192.1.1.* can be used.
 * To specify a user in the percussion.com domain, *.percussion.com
 * can be used. Only one asterisk is permitted in the string, as the
 * first or last character.
 * 
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSHostAddressFilterEntry extends PSFilterEntry {

   /**
    * Construct a host address filter. This can be used to compare a
    * host address (IP address) or host name (DNS name).
    * <p>
    * To filter a host address, use an asterisk (*) in the appropriate
    * position. For example, to specify any machine in the class C
    * address range of 192.1.1.1 through 192.1.1.254, 192.1.1.* can be used.
    * To specify a user in the percussion.com domain, *.percussion.com
    * can be used.
    *
    * @param   filter      the filter string
    *
    * @param   accessLevel         the access level to assign this entry
    */
   public PSHostAddressFilterEntry(   java.lang.String filter,
                                    int accessLevel)
      throws IllegalArgumentException
   {
      super(filter, accessLevel);

      if (filter.equals("*")) {
         m_type          = TYPE_ALL;
         m_filterString = "";
      }
      if (filter.startsWith("*")) {
         m_type          = TYPE_FILTER_DOMAIN;
         m_filterString = filter.substring(1);
      }
      else if (filter.endsWith("*")) {
         m_type          = TYPE_FILTER_ADDRESS;
         m_filterString = filter.substring(0, filter.length()-1);
      }
      else {
         throw new IllegalArgumentException(
                        PSErrorManager.createMessage(   NOT_FILTER_ERROR,
                                                      filter));
      }
   }


   /* ******************* PSFilterEntry Implementation ******************* */

   /**
    * Does the specified entry pass the filter condition?
    *
    * @param      entry         the entry to check
    *
    * @return                  <code>true</code> if it does;
    *                           <code>false</code> otherwise
    */
   public boolean passesFilter(PSEntry entry) {
      /* PSFilterEntry.isMatch already validated we're using the same
       * security provider, so now we must check if the entry's name
       * passes our filter condition.
       */
      if (m_type == TYPE_ALL)
         return true;
      else if (m_type == TYPE_FILTER_ADDRESS)
         return isAddressMatch(entry.getName());
      else    // m_type == TYPE_FILTER_DOMAIN
         return isDomainMatch(entry.getName());
   }

   /**
    * Does the host address match our filter string.
    *
    * @param      addr         the host address to check
    *
    * @return                  <code>true</code> if it does;
    *                           <code>false</code> otherwise
    */
   private boolean isAddressMatch(java.lang.String addr)
   {
      // compare up to the wild card
      return addr.startsWith(m_filterString);
   }

   /**
    * Does the host name match our filter string.
    *
    * @param      name         the host name to check
    *
    * @return                  <code>true</code> if it does;
    *                           <code>false</code> otherwise
    */
   private boolean isDomainMatch(java.lang.String name)
   {
      // compare up to the wild card
      return name.endsWith(m_filterString);
   }

   /**
    * Is the specified entry a host address?
    *
    * @param      host         the host name or address
    *
    * @return                  <code>true</code> if it is an address;
    *                           <code>false</code> if it is a name
    */
   public static boolean isAddress(java.lang.String host)
   {
      StringTokenizer tok = new StringTokenizer(host, ".");
      String cur;
      int iToks;

      for (iToks = 0; tok.hasMoreTokens(); iToks++) {
         cur = tok.nextToken();

         if ((cur.length() == 0) || (cur.length() > 3))
            return false;
         else {
            for (int i = 0; i < cur.length(); i++) {
               if (!Character.isDigit(cur.charAt(i)))
                  return false;
            }
         }
      }

      // if we didn't hit a four part address, there's a problem
      if (iToks != 4)
         return false;

      return true;
   }


   private java.lang.String   m_filterString;
   private int                  m_type;

   private static final int   TYPE_ALL               = 1;
   private static final int   TYPE_FILTER_ADDRESS   = 2;
   private static final int   TYPE_FILTER_DOMAIN   = 3;

   private static final int    NOT_FILTER_ERROR      = 9501;
}

