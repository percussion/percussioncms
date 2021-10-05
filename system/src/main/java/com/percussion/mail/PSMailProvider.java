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

package com.percussion.mail;

import com.percussion.error.PSIllegalArgumentException;


/**
 * The PSMailProvider abstract class defines the functionality required by
 * mail providers for E2. Mail providers can only be used to
 * send mail at this time.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSMailProvider
{
   /**
    * Construct a mail provider.
    */
   protected PSMailProvider()
   {
      super();
   }

   /**
    * Get the name of this mail provider. This is the name to use
    * wherever a mail provider name is required.
    *
    * @return      the provider name
    */
   public abstract String getName();

   /**
    * Get the full name of this mail provider. 
    * Many providers use acronyms as their name, so this is often
    * the expanded acronym.
    *
    * @return      the provider's full name
    */
   public abstract String getFullName();

   /**
    * Get a brief the description of this mail provider.
    *
    * @return      the brief description
    */
   public abstract String getDescription();

   /**
    * Get the property definitions for this provider. The key is set to
    * the name of the property and the value is set to the description
    * of the property. These properties must be set to instantiate the
    * provider.
    *
    * @return      the properties required by this provider
    */
   public abstract java.util.Properties getPropertyDefs();

   /**
    * Send a mail message through this provider.
    *
    * @param      msg      the message to send
    *
    * @exception   java.io.IOException
    *                        if an I/O error occurs
    *
    * @exception   PSMailSendException
    *                        if an error occurs sending the message
    */
   public abstract void send(PSMailMessage msg)
      throws java.io.IOException, PSMailSendException;

   /**
    * Break up a name in the form user@domain into an array containing
    * name in entry 0 and domain in entry 1.
    *
    * @param      name      the name to parse
    *
    * @return               name in entry 0 and domain in entry 1
    *
    * @exception   PSIllegalArgumentException
    *                        if name is not of the form user@domain
    */
   public static String[] getNameParts(String name)
      throws PSIllegalArgumentException
   {
      int pos = name.indexOf('@');

      // Three unacceptable forms: noAt, @domain, and user@
      if (pos < 1 || pos >= name.length() - 1) {
         throw new PSIllegalArgumentException(IPSMailErrors.MAIL_ADDRESS_INVALID,
            name + " is in an invalid electronic mail address form");
      }

      String[] parts = new String[2];
      parts[0] = name.substring(0, pos);
      parts[1] = name.substring(pos+1);
      
      return parts;
   }
}
