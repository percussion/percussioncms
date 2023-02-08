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
package com.percussion.delivery.metadata.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.Validate;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Responsible for calculating a hash over a value. It uses SHA-1 by default
 * and UTF-8 to convert the string value.
 * 
 * @author miltonpividori
 *
 */
@SuppressFBWarnings("WEAK_MESSAGE_DIGEST_SHA1") //Is used to create a hash of a CLOB/long string for indexing not for security
public class PSHashCalculator
{
    private static final String HEXES = "0123456789ABCDEF";
    private static final String HASH_ALGORITHM = "SHA-1";
    private static final String CONTENT_ENCODING = StandardCharsets.UTF_8.name();

    private MessageDigest digest;
    
    /**
     * Creates a new PSHashCalculator instance using the algorithm specified in
     * {@link #HASH_ALGORITHM} value. It may throw an exception if the algorithm
     * is not specified.
     */
    public PSHashCalculator()
    {
        try
        {
            digest = MessageDigest.getInstance(HASH_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Calculates a hash of the given value. The hash algorithm used is specified
     * by {@link #HASH_ALGORITHM}. The value encoding is specified by
     * {@link #CONTENT_ENCODING}.
     * <p>
     * This method is thread-safe.
     * 
     * @param value The content to generate a hash value of. Cannot be
     * <code>null</code>, maybe empty.
     * @return A hash value according to the hash algorithm specified in
     * {@link #HASH_ALGORITHM}.
     */
    public synchronized String calculateHash(String value)
    {
        Validate.notNull(value, "Value cannot be null");
        
        digest.reset();
        byte[] hashResult = null;
        
        try
        {
            hashResult = digest.digest(value.getBytes(CONTENT_ENCODING));
            return getHex(hashResult);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private String getHex(byte[] raw)
    {
        if (raw == null)
            return null;
        
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        
        for (final byte b : raw)
        {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        
        return hex.toString();
    }
}
