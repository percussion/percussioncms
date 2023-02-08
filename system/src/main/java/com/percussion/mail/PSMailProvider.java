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
