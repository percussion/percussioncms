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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
