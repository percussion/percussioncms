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

import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * The PSHostAddressProvider class is used to access the
 * CGI variables <code>REMOTE_HOST</code> and <code>REMOTE_ADDR</code>
 * to authenticate the user. These variables are used to store the IP
 * address and/or DNS name of the user issuing the request. E2 allows
 * IP address and domain names to be used as an authentication mechanism
 * by checking the remote user's info with the specified info. Wild card
 * characters are permitted in the IP address and domain name, where the
 * wild card can be used at the end of the IP address or the beginning
 * of the domain name, as follows:
 * <ul>
 *   <li>*.percussion.com - all users in the percussion.com domain</li>
 *   <li>38.164.160.* - all users in the specified address range</li>
 *   </ul>
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSHostAddressProvider extends Object
{

   /**
    * Construct an instance of this provider.
    */
   public PSHostAddressProvider(Properties props, String providerInstance)
   {
      if (providerInstance == null)
         throw new IllegalArgumentException("providerInstance cannot be null");
         
      providerInstance = providerInstance.trim();
      if (providerInstance.length() == 0)
         throw new IllegalArgumentException("providerInstance cannot be empty");
         
      m_spInstance = providerInstance;
   }

   /**
    * Get the authenticated user(s) associated with the specified request.
    *
    * @param      req           the request context
    *
    * @return                  an array of user entries, or <code>null</code>
    *                           if this security provider was not used to
    *                           authenticate the user
    */
   public PSUserEntry[] getAuthenticatedUserEntries(PSRequest req)
   {
      List entries = new ArrayList();

      /* we need to get the user's host name and address. Furthermore, we
       * need to support filters, so we store the user entry in the
       * using * for each piece as well.
       */
      ServletRequest sreq = req.getServletRequest();
      String name = sreq.getRemoteHost();
      String addr = sreq.getRemoteAddr();

      if ((addr != null) && (name != null)) 
      {
         if (name.equals(addr))
            name = null;
      }
      else if ((name == null) && (addr == null))
         return null;

      PSUserSession sess = req.getUserSession();
      PSUserEntry entry = null;
      int pos;
      if (addr != null) 
      {
         entry = new PSUserEntry(addr, 0, null, null, null);
         entries.add(entry);

         // if this doesn't already exist in the user session, add it
         if (!isUserEntryDefined(req, entry))
            sess.addAuthenticatedUserEntry(entry);

         for (   pos = addr.indexOf('.');
               pos != -1;
               pos = addr.indexOf('.', pos+1))
         {
            entry = new PSUserEntry(
               addr.substring(0, pos) + ".*", 0, null, null, null);
            entries.add(entry);

            // if this doesn't already exist in the user session, add it
            if (!isUserEntryDefined(req, entry))
               sess.addAuthenticatedUserEntry(entry);
         }
      }

      if (name != null) 
      {
         entry = new PSUserEntry(name, 0, null, null, null);
         entries.add(entry);

         // if this doesn't already exist in the user session, add it
         if (!isUserEntryDefined(req, entry))
            sess.addAuthenticatedUserEntry(entry);

         for (   pos = name.lastIndexOf('.');
               pos != -1;
               pos = name.lastIndexOf('.', pos-1))
         {
            entry = new PSUserEntry(
               "*." + name.substring(pos+1), 0, null, null, null);
            entries.add(entry);

            // if this doesn't already exist in the user session, add it
            if (!isUserEntryDefined(req, entry))
               sess.addAuthenticatedUserEntry(entry);
         }
      }

      // now copy the array list over to the array for return
      PSUserEntry[] ret = new PSUserEntry[entries.size()];
      entries.toArray(ret);

      return ret;
   }

   /**
    * Get the meta data associated with this instance.
    *
    * @return         the instance meta data
    */
   public PSHostAddressProviderMetaData getMetaData()
   {
      return new PSHostAddressProviderMetaData(this);
   }

   /**
    * See if the specified user entries already exists in the user session.
    *
    * @param      req         the request context
    *
    * @param      entry       the entry to check
    *
    * @return                 <code>true</code> if it does
    */
   protected boolean isUserEntryDefined(PSRequest req, PSUserEntry entry)
   {
      if (req == null)
         return false;

      PSUserSession sess = req.getUserSession();
      if (sess == null)
         return false;

      PSUserEntry[] users = sess.getAuthenticatedUserEntries();
      int size = (users == null) ? 0 : users.length;
      for (int i = 0; i < size; i++) 
      {
         // is match checks SP info and name only, so this will match
         // even though we skipped group/attribute info
         if (users[i].isMatch(entry))
            return true;
      }

      return false;
   }

   /**
    * The name of this security provider.
    */
   public static final String SP_NAME = "HostAddress";

   /**
    * The class name of this security provider.
    */
   public static final String SP_CLASSNAME = 
      PSHostAddressProvider.class.getName();

   /**
    * The provider instance string. Initialized in constructor. Never
    * changed after that, never <code>null</code> or empty.
    */
   protected String m_spInstance = null;
}

