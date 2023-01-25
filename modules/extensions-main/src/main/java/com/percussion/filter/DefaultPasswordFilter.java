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
