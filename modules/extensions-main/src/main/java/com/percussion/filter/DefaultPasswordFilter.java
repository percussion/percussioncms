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
package com.percussion.filter;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


/**
 *    This class will use a default encrypting scheme to
 *    encrypt passwords for various Percussion security providers.
 */
public class DefaultPasswordFilter implements IPSPasswordFilter
{
   public static final Logger log = LogManager.getLogger(DefaultPasswordFilter.class);
   
   /* Main method used to test or encrypt from command line */
   public static void main(String[] args)
   {
      DefaultPasswordFilter filter = new DefaultPasswordFilter();

      for (String arg : args) {
         log.info("{} --> {}" ,arg , filter.encrypt(arg));
      }
   }

   /* IPSPasswordFilter implementation */

   /**
    * This method is called by the Rhythmyx security provider before
    * authenticating a user. The password submitted in the request is
    * run through this filter, then checked against the stored password
    * character-for-character.
    *
    * @param password The clear-text password to be encrypted. Never
    * <CODE>null</CODE>, but may be <code>empty</code>.
    *
    * @return A string containing the encrypted password. Never
    * <CODE>null</CODE>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public String encrypt(String password)
   {
      if(StringUtils.isBlank(password))
      {
         return StringUtils.EMPTY; 
      }
      try {
         return PSPasswordHandler.getHashedPassword(password.trim());
      } catch (PSEncryptionException e) {
         throw new IllegalArgumentException(e);
      }
   }

   @Override
   public String getAlgorithm() {
      return PSPasswordHandler.ALGORITHM;
   }

   public void init(IPSExtensionDef def, File f)
   {
   }


   /***
    * Will encrypt the password using the hashing / encryption
    * routine used in the previous version of the software.
    *
    * This is to allow Security Providers to re-encrypt passwords
    * on login after a security update.
    *
    * @param password
    * @return
    */
   @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_SHA1")
   @Override
   @Deprecated
   public String legacyEncrypt(String password) {
      if(StringUtils.isBlank(password))
      {
         return StringUtils.EMPTY;
      }
      return DigestUtils.shaHex(password.trim());
   }

   @Override
   public String getLegacyAlgorithm() {
      return "SHA-1";
   }
}
