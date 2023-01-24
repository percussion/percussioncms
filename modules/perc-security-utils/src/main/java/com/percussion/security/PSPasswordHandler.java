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

import com.percussion.error.PSExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
    public static final String ALGORITHM = "PBKDF2WithHmacSHA512";


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
             skf = SecretKeyFactory.getInstance(ALGORITHM);
         } catch (NoSuchAlgorithmException e) {
             log.error(e.getMessage());
             log.debug(PSExceptionUtils.getDebugMessageForLog(e));
             throw new PSEncryptionException(e);
         }

        try {
            encoded = skf.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            log.error(e.getMessage());
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new PSEncryptionException(e);
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

     return Base64.getEncoder().encodeToString(newPwWithSalt).equals(
             encodedPw);
 }
}
