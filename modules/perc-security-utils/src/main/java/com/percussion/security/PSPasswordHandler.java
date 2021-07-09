/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Class responsible for handling password related functions,
 * all password handling functions should use this for hashing the password.
 * The password hash is not reversible currently using NIST approved
 *
 */
public class PSPasswordHandler {

    private static final Logger log = LogManager.getLogger(PSPasswordHandler.class);
    private static final int DEFAULT_SALT_SIZE=128;
    private static final int DEFAULT_ITERATIONS = 10000;

    private PSPasswordHandler(){
        //Do nothing
    }

    protected static byte[] getSalt(int size) {
         SecureRandom random = new SecureRandom();
         byte[] salt = new byte[size];
         random.nextBytes(salt);
         return salt;
    }


    protected static byte[] getHashedPasswordBytes(String password, byte[] salt) throws PSEncryptionException {
        if(salt == null || salt.length==0) {
            salt = getSalt(DEFAULT_SALT_SIZE);
        }
         KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, DEFAULT_ITERATIONS,128);
         SecretKeyFactory skf;
         byte[] encoded;

         try {
             skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
         } catch (NoSuchAlgorithmException e) {
             log.error(e.getMessage());
             log.debug(e.getMessage(),e);
             throw new PSEncryptionException(e.getMessage());
         }

        try {
            if(skf != null) {
                encoded = skf.generateSecret(spec).getEncoded();
            }else{
                throw new PSEncryptionException("Unable to initialize SecretKeyFactory!");
            }
         } catch (InvalidKeySpecException | PSEncryptionException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSEncryptionException(e.getMessage());
        }

        return ArrayUtils.addAll(encoded
                ,salt
         );

 }

    /**
     * Returns a slated hashed password suitable for storage in a database or a
     * configuration file.
     *
     * @param password Clear test password to be hashed.
     * @return Base64 encoded byte array holding the hashed password.
     */
 public static String getHashedPassword(String password) throws PSEncryptionException {
     return Base64.getEncoder().encodeToString(getHashedPasswordBytes(password,getSalt(DEFAULT_SALT_SIZE)));
 }

    /**
     * Checks to see
     * @param password An un-hashed clear text password.
     * @param encodedPw A hashed and salted password.
     * @return true if the passwords match, false if they do not.
     */
 public static boolean checkHashedPassword(String password, String encodedPw) throws PSEncryptionException {

     if (password == null || encodedPw == null)
         throw new IllegalArgumentException("All input parameters must have a value.");
     byte[] hashedPwWithSalt = Base64.getDecoder().decode(encodedPw);
     byte[] ogSalt = ArrayUtils.subarray(hashedPwWithSalt, hashedPwWithSalt.length - DEFAULT_SALT_SIZE, hashedPwWithSalt.length);
     byte[] newPwWithSalt = getHashedPasswordBytes(password, ogSalt);

     return MessageDigest.isEqual(newPwWithSalt, hashedPwWithSalt);
 }
}
