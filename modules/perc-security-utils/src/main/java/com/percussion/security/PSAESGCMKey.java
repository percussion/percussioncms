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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSAESGCMKey implements IPSSecretKey {

    private static final Logger log = LogManager.getLogger(PSAESGCMKey.class);
    private static final int DEFAULT_KEY_SIZE=256;
    private SecretKey key = null;


    /**
     * Generate an IPSEncryptor object which can make use of this key.
     *
     * @return the associated encryptor
     */
    @Override
    public IPSEncryptor getEncryptor() {
        try {
            if(key==null){
                key = generateKey();
            }
            return new PSAESGCMEncryptor(this);
        } catch (IllegalArgumentException e)   {
            // only throws on null key, which clearly can't happen here
            return null;
        }
    }

    /**
     * Generate an IPSDecryptor object which can make use of this key.
     *
     * @return the associated decryptor
     */
    @Override
    public IPSDecryptor getDecryptor() {
        try {
            return new PSAESGCMDecryptor(this);
        } catch (IllegalArgumentException e)   {
            // only throws on null key, which clearly can't happen here
            return null;
        }
    }

    /**
     * Returns a byte array containing the secret key
     *
     * @return
     */
    @Override
    public byte[] getSecret() {
        return key.getEncoded();
    }

    public SecretKey getSecretKey(){
        return key;
    }

    /**
     * Get the number of bits required for this secret key.
     *
     * @return the number of bits to use in setSecret
     */
    @Override
    public int getSecretSizeInBits() {
        return DEFAULT_KEY_SIZE;
    }

    /**
     * Set the secret to the specified byte array. It must have the
     * appropriate number of bytes to match the bit count returned by
     * getSecretSizeInBits.
     *
     * @param secret the secret to use to generate the key
     * @throws IllegalArgumentException if the secret is invalid for this object
     */
    @Override
    public void setSecret(byte[] secret) throws IllegalArgumentException {
        key = new SecretKeySpec(secret, "AES");
    }

    /**
     * Generates a new key
     *
     * @return a new SecretKey or null
     */
    @Override
    public SecretKey generateKey(){

        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(DEFAULT_KEY_SIZE, SecureStringUtils.getSecureRandom());
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            log.error("Unable to initialize AES encryption.",e);
        }
        return null;
    }


}
